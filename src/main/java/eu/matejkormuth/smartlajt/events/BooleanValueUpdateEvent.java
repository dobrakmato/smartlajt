package eu.matejkormuth.smartlajt.events;

import eu.matejkormuth.smartlajt.Event;
import lombok.Data;

@Data
public class BooleanValueUpdateEvent implements Event {
    private final boolean value;
}
