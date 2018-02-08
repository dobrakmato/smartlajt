package eu.matejkormuth.smartlajt.commands;

import eu.matejkormuth.smartlajt.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.UUID;

@ToString
@RequiredArgsConstructor
public abstract class AbstractCommand implements Command {
    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    @Nullable
    private final String description;

    public UUID getUUID() {
        return uuid;
    }


}
