package eu.matejkormuth.smartlajt.devices;

import com.eclipsesource.json.JsonObject;
import eu.matejkormuth.smartlajt.Command;
import eu.matejkormuth.smartlajt.DeviceType;
import eu.matejkormuth.smartlajt.commands.ProcessCommand;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public final class MyLedLight extends AbstractDevice {

    public MyLedLight(UUID uuid, String name, String description) {
        super(uuid, name, description, DeviceType.LIGHT);
    }

    @Override
    public void setup(@Nonnull JsonObject params) throws Exception {

    }

    @Override
    public List<Command> getCommands() {
        final String command = "/var/www/html/smarthome/led_ctrl";

        return Arrays.asList(
                new ProcessCommand(UUID.randomUUID(), "On", null, command, "LED_ON"),
                new ProcessCommand(UUID.randomUUID(), "Off", null, command, "LED_OFF"),
                new ProcessCommand(UUID.randomUUID(), "Intensity +", null, command, "LED_IP"),
                new ProcessCommand(UUID.randomUUID(), "Intensity -", null, command, "LED_IM"),
                new ProcessCommand(UUID.randomUUID(), "Color: White", null, command, "LED_WHITE"),
                new ProcessCommand(UUID.randomUUID(), "Color: Red", null, command, "LED_RED"),
                new ProcessCommand(UUID.randomUUID(), "Color: Green", null, command, "LED_GREEN"),
                new ProcessCommand(UUID.randomUUID(), "Color: Blue", null, command, "LED_BLUE")
        );
    }
}
