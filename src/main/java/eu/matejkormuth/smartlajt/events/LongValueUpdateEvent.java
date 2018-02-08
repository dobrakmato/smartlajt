package eu.matejkormuth.smartlajt.events;

import eu.matejkormuth.smartlajt.Event;
import lombok.Data;

@Data
public class LongValueUpdateEvent implements Event {
    private final long value;
}
