package eu.matejkormuth.smartlajt.commands;

import eu.matejkormuth.smartlajt.Command;
import lombok.val;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CompoundCommand extends AbstractCommand {

    private final List<Command> commands;
    private final boolean async;

    private CompoundCommand(UUID uuid, String name, String description, List<Command> commands, boolean async) {
        super(uuid, name, description);
        this.commands = commands;
        this.async = async;
    }

    public static CompoundCommand sync(UUID uuid, String name, @Nullable String description, Command... sequence) {
        return new CompoundCommand(uuid, name, description, Arrays.asList(sequence), false);
    }

    public static CompoundCommand async(UUID uuid, String name, @Nullable String description, Command... commands) {
        return new CompoundCommand(uuid, name, description, Arrays.asList(commands), true);
    }

    @Override
    public void execute() {
        if (async) {
            async();
        } else {
            sync();
        }
    }

    private void sync() {
        for (val command : commands) {
            command.execute();
        }
    }

    private void async() {
        throw new UnsupportedOperationException("Async compound commands are not yet supported");
    }
}
