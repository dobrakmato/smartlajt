package eu.matejkormuth.smartlajt.events;

import eu.matejkormuth.smartlajt.Event;
import lombok.Data;

@Data
public class DoubleValueUpdateEvent implements Event {
    private final double value;
}
