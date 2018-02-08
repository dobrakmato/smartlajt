package eu.matejkormuth.smartlajt.sensors;

import eu.matejkormuth.smartlajt.Event;
import eu.matejkormuth.smartlajt.Sensor;
import eu.matejkormuth.smartlajt.SensorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSensor<E extends Event> implements Sensor<E> {

    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    @Nullable
    private final String description;
    @Getter
    private final SensorType type;
    @Getter
    private E lastEvent = null;
    @Getter
    private long updateInterval;

    private final List<Consumer<E>> subscriptions = new ArrayList<>();

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void subscribe(@Nonnull Consumer<E> consumer) {
        subscriptions.add(consumer);
    }

    @Override
    public void unsubscribe(@Nonnull Consumer<E> consumer) {
        subscriptions.remove(consumer);
    }

    @Override
    public void unsubscribeAll() {
        subscriptions.clear();
    }

    @Override
    public void fire(@Nonnull E event) {
        this.lastEvent = event;

        for (val consumer : subscriptions) {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                log.error("Error while processing subscription "
                        + String.valueOf(consumer) + " for event " + String.valueOf(event), e);
            }
        }
    }
}
