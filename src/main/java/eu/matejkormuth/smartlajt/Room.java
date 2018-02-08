package eu.matejkormuth.smartlajt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class Room implements Identifiable {

    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    @Nullable
    private final String description;

    private final List<Device> devices = new ArrayList<>();
    private final List<Sensor<? extends Event>> sensors = new ArrayList<>();


    public UUID getUUID() {
        return uuid;
    }

    public void addDevice(Device device) {
        this.devices.add(device);
    }

    public void addSensor(Sensor sensor) {
        this.sensors.add(sensor);
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(this.devices);
    }

    public List<Sensor<? extends Event>> getSensors() {
        return Collections.unmodifiableList(this.sensors);
    }

}
