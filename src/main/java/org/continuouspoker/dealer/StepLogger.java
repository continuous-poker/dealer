package org.continuouspoker.dealer;

@FunctionalInterface
public interface StepLogger {

    void log(String msg);

    default void log(final String msg, final Object... values) {
        log(String.format(msg, values));
    }
}
