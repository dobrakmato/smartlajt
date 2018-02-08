package eu.matejkormuth.smartlajt.commands;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class ProcessCommand extends AbstractCommand {

    private final String command;
    private final String[] arguments;

    public ProcessCommand(UUID uuid, String name, @Nullable String description,
                          String command, String... arguments) {
        super(uuid, name, description);
        this.command = command;
        this.arguments = arguments;
    }

    @Override
    public void execute() {
        val cmd = command + String.join(" ", arguments);
        
        try {
            Runtime.getRuntime().exec(cmd).waitFor();
            log.info("Command {} executed successfully", cmd);
        } catch (InterruptedException | IOException e) {
            log.error("Error while executing command " + cmd, e);
        }
    }
}
