package eu.matejkormuth.smartlajt;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import eu.matejkormuth.smartlajt.events.TimeEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class Application {

    private static final PathMatcher JSON_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.json");
    private static final PathMatcher RULES_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.rules");
    private static final int ONE_DAY = 1000 * 60 * 60 * 24; // one day in ms

    private final List<Room> rooms = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private final List<Sensor<? extends Event>> sensors = new ArrayList<>();

    private final List<EventEmitter<? extends Event>> emitters = new ArrayList<>();

    private final Map<UUID, Command> commands = new HashMap<>();
    private final Map<UUID, EventEmitter<? extends Event>> emittersByUuid = new HashMap<>();

    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(4);

    public Application() {
        log.info("Loading configurations...");
        this.loadConfigurations();
        this.populateCollections();
        this.setupSensorPolling();
    }

    private void setupSensorPolling() {
        log.info("Setting up-sensor polling.");
        for (val sensor : sensors) {
            val interval = sensor.getUpdateInterval();
            log.info(" Polling sensor {} ({}) for first time.", sensor.getName(), sensor.getUUID());
            timer.scheduleAtFixedRate(sensor::poll, 0L, interval, TimeUnit.MILLISECONDS);
        }
    }

    private void populateCollections() {
        for (val room : rooms) {
            devices.addAll(room.getDevices());
            sensors.addAll(room.getSensors());

            room.getDevices().forEach(d -> d.getCommands().forEach(c -> commands.put(c.getUUID(), c)));
        }

        emitters.addAll(sensors);
        emitters.forEach(e -> emittersByUuid.put(e.getUUID(), e));

        log.info("Loaded {} rooms with {} sensors ({} emitters), {} devices which provide {} commands!",
                rooms.size(), sensors.size(), emitters.size(), devices.size(), commands.size());
    }

    private void loadConfigurations() {
        try {
            Files.list(Paths.get(".")).forEach(this::loadConfiguration);
        } catch (IOException e) {
            log.error("You I/O is broken", e);
        }
    }

    private void loadConfiguration(Path path) {
        if (JSON_MATCHER.matches(path)) {
            this.loadRoom(path);
        } else if (RULES_MATCHER.matches(path)) {
            this.loadRules(path);
        }
    }

    private void loadRules(Path path) {
        log.info("Loading rules from file {}...", path);
    }

    public <E extends Event> Subscription<E> on(UUID uuid) {
        EventEmitter<E> emitter = (EventEmitter<E>) emittersByUuid.get(uuid);
        return on(emitter);
    }

    public <E extends Event> Subscription<E> on(EventEmitter<E> emitter) {
        val subscription = new Subscription<E>();
        emitter.subscribe(subscription);
        return subscription;
    }

    public Subscription<TimeEvent> at(LocalTime time) {
        val subscription = new Subscription<TimeEvent>();

        long delay = ChronoUnit.MILLIS.between(LocalTime.now(), time);
        timer.scheduleAtFixedRate(() -> subscription.accept(new TimeEvent()),
                delay, ONE_DAY, TimeUnit.MILLISECONDS);

        return subscription;
    }

    private void loadRoom(Path path) {
        log.info("Loading room from file {}...", path);

        JsonObject json;
        try {
            json = Json.parse(new String(Files.readAllBytes(path), "UTF-8")).asObject();
        } catch (IOException e) {
            log.error("Can't load room " + path, e);
            return;
        }

        if (json.getString("uuid", null) == null) {
            throw new RuntimeException("Invalid room JSON! (Room is missing UUID)");
        }

        val roomUUID = UUID.fromString(json.getString("uuid", null));
        val roomName = json.getString("name", json.getString("uuid", null));
        val roomDescription = json.getString("description", json.getString("description", null));

        val room = new Room(roomUUID, roomName, roomDescription);

        val devices = json.get("devices");
        val sensors = json.get("sensor");

        if (devices != null) {
            JsonArray devicesArray = devices.asArray();

            for (val device : devicesArray) {
                instantiate(room, device, Device.class);
            }
        }

        if (sensors != null) {
            JsonArray sensorsArray = sensors.asArray();

            for (val sensor : sensorsArray) {
                instantiate(room, sensor, Sensor.class);
            }
        }

        this.rooms.add(room);
        log.info("Loaded room {} with {} sensors and {} devices.", room.getName(),
                room.getSensors().size(), room.getDevices().size());
    }

    private <T extends Identifiable> void instantiate(Room room, JsonValue configurationJson, Class<T> type) {
        val json = configurationJson.asObject();
        val typeString = type == Sensor.class ? "Sensor" : "Device";

        if (json.getString("uuid", null) == null) {
            throw new RuntimeException("Invalid room JSON! (" + typeString + " is missing UUID)");
        }

        if (json.getString("class", null) == null) {
            throw new RuntimeException("Invalid room JSON! (" + typeString + " is missing Class)");
        }

        val uuid = UUID.fromString(json.getString("uuid", null));
        val name = json.getString("name", json.getString("uuid", null));
        val description = json.getString("description", null);
        val klass = json.getString("class", null);
        val params = json.get("params") == null ?
                new JsonObject() : json.get("params").asObject();

        try {
            Class<?> clazz = Class.forName(klass);

            if (type.isAssignableFrom(clazz)) {
                throw new UnsupportedOperationException("Can't instantiate class " + klass
                        + " as " + typeString + " because it does not implement Sensor interface.");
            }

            Constructor<? extends T> ctr = ((Class<? extends T>) clazz)
                    .getConstructor(UUID.class, String.class, String.class);

            try {
                T thing = ctr.newInstance(uuid, name, description);
                log.info("Setting up " + typeString + " {} ({})", thing.getName(), thing.getUUID());
                try {

                    if (thing instanceof Sensor) {
                        ((Sensor) thing).setup(params);
                        room.addSensor((Sensor) thing);
                    } else if (thing instanceof Device) {
                        ((Device) thing).setup(params);
                        room.addDevice((Device) thing);
                    } else {
                        log.error("Can't setup " + typeString + " " + name + " (" + uuid.toString() +
                                "). Instantiated thing is not Device nor Sensor: " + klass);
                        return;
                    }
                    log.info(" " + typeString + " {} ({}) set-up successfully.", thing.getName(), thing.getUUID());
                } catch (Exception e) {
                    log.error("Can't setup " + typeString + " " + name + " (" + uuid.toString() +
                            "). Call to setup() method thrown an exception.", e);
                }
            } catch (Exception e) {
                log.error("Can't instantiate " + typeString + " " + name + " (" + uuid.toString() +
                        "). Constructor invocation failed. Class: " + klass, e);
            }
        } catch (ClassNotFoundException e) {
            log.error("Can't instantiate " + typeString + " " + name + " (" + uuid.toString() +
                    "). Device class not found.", e);
        } catch (NoSuchMethodException e) {
            log.error("Can't instantiate " + typeString + " " + name + " (" + uuid.toString() +
                    "). Constructor(UUID, String, String) is not present on class " + klass, e);
        } catch (UnsupportedOperationException e) {
            log.error("Can't instantiate " + typeString + " " + name + " (" + uuid.toString() +
                    ") " + klass, e);
        }
    }
}
