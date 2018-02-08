package eu.matejkormuth.smartlajt;

import eu.matejkormuth.smartlajt.filters.TimesFilter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
public final class Subscription<E extends Event> implements Consumer<E> {

    private final Deque<Filter<E>> filterChain = new ArrayDeque<>(4);
    private final List<Command> triggers = new ArrayList<>(4);

    @Override
    public void accept(E event) {
        for (val filter : this.filterChain) {
            if (!filter.test(event)) {
                return;
            }
        }

        runTriggers();
    }

    private void runTriggers() {
        for (Command command : triggers) {
            try {
                command.execute();
            } catch (Exception e) {
                log.error("Error occurred while executing trigger "
                        + String.valueOf(command) + " of subscription " + String.valueOf(this), e);
            }
        }
    }

    public Subscription<E> filter(Filter<E> filter) {
        this.filterChain.push(filter);
        return this;
    }

    // filter-modifier
    public Subscription<E> times(int times) {
        // Pop last added filter and wrap it in TimesFilter. TimesFilter will
        // then use last filter as filter source and will apply it's own
        // filtering logic on top of the existing filter.
        val lastFilter = filterChain.pop();
        filterChain.push(new TimesFilter<>(lastFilter, times));

        return this;
    }

    public void trigger(Command... commands) {
        this.triggers.addAll(Arrays.asList(commands));
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "filterChain=" + filterChain +
                ", triggers=" + triggers +
                '}';
    }
}
