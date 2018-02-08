package eu.matejkormuth.smartlajt.filters;

import eu.matejkormuth.smartlajt.Event;
import eu.matejkormuth.smartlajt.Filter;
import eu.matejkormuth.smartlajt.Sensor;
import eu.matejkormuth.smartlajt.events.BooleanValueUpdateEvent;
import eu.matejkormuth.smartlajt.events.DoubleValueUpdateEvent;
import eu.matejkormuth.smartlajt.events.LongValueUpdateEvent;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class Filters {

    public Filter<DoubleValueUpdateEvent> greaterThan(double d) {
        return e -> e.getValue() > d;
    }

    public Filter<LongValueUpdateEvent> greaterThan(long l) {
        return e -> e.getValue() > l;
    }

    public Filter<DoubleValueUpdateEvent> lessThan(double d) {
        return e -> e.getValue() < d;
    }

    public Filter<LongValueUpdateEvent> lessThan(long l) {
        return e -> e.getValue() < l;
    }

    public Filter<DoubleValueUpdateEvent> equal(double d, double epsilon) {
        return e -> Math.abs(e.getValue() - d) < epsilon;
    }

    public Filter<LongValueUpdateEvent> equal(long l) {
        return e -> e.getValue() < l;
    }

    public <E extends Event, SE extends Event> Filter<E> device(@Nonnull Sensor<SE> sensor, @Nonnull Filter<SE> filter) {
        return e -> filter.test(sensor.getLastEvent());
    }

    public static boolean isTrue(BooleanValueUpdateEvent e) {
        return e.isValue();
    }

    public static boolean isFalse(BooleanValueUpdateEvent e) {
        return !e.isValue();
    }
}
