package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_NOT_EXECUTABLE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MakeNotExecutableActionTest {

    @Test
    void updateIndexCommand() {
        assertThat(new MakeNotExecutableAction().updateIndexCommand()).isEqualByComparingTo(MAKE_NOT_EXECUTABLE);
    }
}