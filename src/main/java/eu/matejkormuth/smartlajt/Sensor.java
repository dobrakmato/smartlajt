package eu.matejkormuth.smartlajt;

import com.eclipsesource.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Sensor<E extends Event> extends EventEmitter<E> {

    SensorType getType();

    @Nullable
    E getLastEvent();

    long getUpdateInterval();

    void poll();

    void setup(@Nonnull JsonObject params) throws Exception;

}
