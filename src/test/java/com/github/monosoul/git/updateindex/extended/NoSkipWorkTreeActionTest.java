package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.NO_SKIP_WORKTREE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class NoSkipWorkTreeActionTest {

    @Test
    void skipWorkTreeCommand() {
        assertThat(new NoSkipWorkTreeAction().updateIndexCommand()).isEqualByComparingTo(NO_SKIP_WORKTREE);
    }
}