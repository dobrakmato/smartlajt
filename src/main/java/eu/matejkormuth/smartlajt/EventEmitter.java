package eu.matejkormuth.smartlajt;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public interface EventEmitter<E extends Event> extends Identifiable {

    void subscribe(@Nonnull Consumer<E> consumer);

    void unsubscribe(@Nonnull Consumer<E> consumer);

    void unsubscribeAll();

    void fire(@Nonnull E event);

}
