package eu.matejkormuth.smartlajt;

import eu.matejkormuth.smartlajt.events.BooleanValueUpdateEvent;
import eu.matejkormuth.smartlajt.events.TimeEvent;
import eu.matejkormuth.smartlajt.filters.Filters;
import eu.matejkormuth.smartlajt.sensors.MAX44009;
import lombok.val;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bootstrap {

    private final List<Room> rooms = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private final List<Sensor> sensors = new ArrayList<>();

    private final List<EventEmitter> emitters = new ArrayList<>();

    private final Map<UUID, Command> commands = new HashMap<>();
    private final Map<UUID, EventEmitter> emmitersById = new HashMap<>();

    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) {
        Command turnOnLedLight = null;
        Command turnOffLedLight = null;
        Sensor<BooleanValueUpdateEvent> mobilePresence = null;
        MAX44009 lightSensor = new MAX44009(UUID.randomUUID(), "Light", null);

        // when MY PHONE is PRESENT for 3 TIMES trigger ON LED LIGHT
        on(mobilePresence)
                .filter(Filters::isTrue)
                .times(3)
                .trigger(turnOnLedLight);

        // when MY PHONE is (PRESENT for 3 TIMES) and (LIGHT LEVEL is LESS THAN 20) trigger ON LED LIGHT
        on(mobilePresence)
                .filter(Filters::isTrue)
                .times(3)
                .filter(Filters.device(lightSensor, Filters.lessThan(20.0)))
                .trigger(turnOnLedLight);

        // when TIME is 9:00 AM trigger OFF LED LIGHT
        at("09:00")
                .trigger(turnOffLedLight);


        // when ({EVENT_SOURCE_UUID_OR_NAME} is {EVENT_SOURCE_DATA_FILTER}|{TIME_EXPRESSION}) (for {TIMES} times)? trigger [({COMMAND_UUID_OR_NAME} {DEVICE_UUID_OR_NAME})...]


        // Sensor is EventSource
        // MobilePresenceDetector is EventSource
        // Time is EventSource
        // Application is EventSource

        // on(EventEmitter) ->Subscription;
        // filter(Predicate) ->FilterChain;
        // times( int) ->FilterChain;
        // trigger(Command...) ->void ;

        // --------------------------

        // CompoundCommand(builder) is Command;

        // CompoundCommand.sync(cmd1, cmd2, cmd3)
        // CompoundCommand.async(cmd1, cmd2)
    }

    public static <E extends Event> Subscription<E> on(EventEmitter<E> emitter) {
        val subscription = new Subscription<E>();
        emitter.subscribe(subscription);
        return subscription;
    }

    public static Subscription<TimeEvent> at(String timeExpression) {
        val subscription = new Subscription<TimeEvent>();
        subscription.accept(null);
        return subscription;
    }
}
