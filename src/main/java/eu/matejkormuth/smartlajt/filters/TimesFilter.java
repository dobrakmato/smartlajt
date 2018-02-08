package eu.matejkormuth.smartlajt.filters;

import eu.matejkormuth.smartlajt.Event;
import eu.matejkormuth.smartlajt.Filter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class TimesFilter<E extends Event> implements Filter<E> {

    private final Filter<E> wrapped;
    private final int times;

    private int count = 0;
    private boolean enabled = true;

    @Override
    public boolean test(E e) {
        val testPassed = wrapped.test(e);

        if (testPassed) {
            if (enabled) {
                count++;
            }

            if (count >= times) {
                enabled = false;
                count = 0;
                return true;
            }
        } else {
            enabled = true;
        }

        return false;
    }
}
