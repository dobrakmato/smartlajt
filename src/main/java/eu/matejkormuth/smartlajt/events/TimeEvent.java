package eu.matejkormuth.smartlajt.events;

import eu.matejkormuth.smartlajt.Event;
import lombok.Data;

import java.time.Instant;

@Data
public class TimeEvent implements Event {
    private final Instant now = Instant.now();
}
