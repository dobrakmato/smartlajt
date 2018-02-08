package eu.matejkormuth.smartlajt;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Identifiable {

    UUID getUUID();

    String getName();

    @Nullable
    String getDescription();
}
