package de.doubleslash.poker.dealer.data;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Status {
    ACTIVE,
    FOLDED,
    OUT;

    @JsonCreator
    public static Status forName(final String name) {
        return Arrays.stream(Status.values())
                     .filter(s -> s.name().equalsIgnoreCase(name))
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}
