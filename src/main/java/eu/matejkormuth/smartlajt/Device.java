package eu.matejkormuth.smartlajt;

import com.eclipsesource.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;

public interface Device extends Identifiable {

    DeviceType getType();

    List<Command> getCommands();

    void setup(@Nonnull JsonObject params) throws Exception;

}
