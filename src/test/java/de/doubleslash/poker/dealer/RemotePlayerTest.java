package de.doubleslash.poker.dealer;


import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RemotePlayerTest {

    @Test
    void shouldThrowExceptionWithIllegalUrl() {
        assertThatThrownBy(() -> new RemotePlayer("99\"4343**ä#"))
                  .isInstanceOf(IllegalArgumentException.class);
    }

}