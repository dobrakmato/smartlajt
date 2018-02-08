package eu.matejkormuth.smartlajt.sensors;

import com.eclipsesource.json.JsonObject;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import eu.matejkormuth.smartlajt.SensorType;
import eu.matejkormuth.smartlajt.events.DoubleValueUpdateEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

/**
 * Class representing MAX44009 light sensor.
 * <p>
 * Based on https://github.com/ControlEverythingCommunity/MAX44009
 */
@Slf4j
public final class MAX44009 extends AbstractSensor<DoubleValueUpdateEvent> {

    public static final int DEVICE_DEFAULT_ADDRESS = 0x4A;

    /* Addresses, enums and useful constants for communication with MAX44009 */
    public static final int CONFIGURATION_REGISTER_ADDRESS = 0x02;
    public static final byte REG_CONFIG_CONTMODE_CONTIN = 0x02;
    public static final byte REG_CONFIG_INTRTIMER_800 = 0x00;
    public static final byte REG_CONFIG_INTRTIMER_400 = 0x01;
    public static final byte REG_CONFIG_INTRTIMER_200 = 0x02;
    public static final byte REG_CONFIG_INTRTIMER_100 = 0x03;

    private I2CDevice device;

    public MAX44009(UUID uuid, String name, String description) {
        super(uuid, name, description, SensorType.LIGHT);
    }

    @Override
    public void setup(@Nonnull JsonObject params) throws Exception {
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = bus.getDevice(params.getInt("address", DEVICE_DEFAULT_ADDRESS));

        // Select configuration register, 0x02(02)
        // Continuous mode, Integration time = 800 ms
        device.write(CONFIGURATION_REGISTER_ADDRESS, (byte) 0x40);
    }

    @Override
    public void poll() {
        try {
            // Read 2 bytes of data from address 0x03(03)
            // luminance MSB, luminance LSB
            byte[] data = new byte[2];
            device.read(0x03, data, 0, 2);

            // Convert the data to lux
            int exponent = (data[0] & 0xF0) >> 4;
            int mantissa = ((data[0] & 0x0F) << 4) | (data[1] & 0x0F);
            double luminance = Math.pow(2, exponent) * mantissa * 0.045;

            // todo: remove
            log.debug("Luminance: {}", luminance);

            this.fire(new DoubleValueUpdateEvent(luminance));
        } catch (IOException e) {
            throw new RuntimeException("Can't read data from sensor", e);
        }
    }
}
