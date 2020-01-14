package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_EXECUTABLE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MakeExecutableActionTest {

    @Test
    void should_have_make_executable_update_index_command() {
        assertThat(new MakeExecutableAction().updateIndexCommand()).isEqualByComparingTo(MAKE_EXECUTABLE);
    }
}