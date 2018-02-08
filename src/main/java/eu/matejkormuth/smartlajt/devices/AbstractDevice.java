package eu.matejkormuth.smartlajt.devices;

import eu.matejkormuth.smartlajt.Device;
import eu.matejkormuth.smartlajt.DeviceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.UUID;

@ToString
@RequiredArgsConstructor
public abstract class AbstractDevice implements Device {
    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    @Nullable
    private final String description;
    @Getter
    private final DeviceType type;

    public UUID getUUID() {
        return uuid;
    }
}
