package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.NO_SKIP_WORKTREE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class NoSkipWorkTreeActionTest {

    @Test
    void should_have_no_skip_worktree_update_index_command() {
        assertThat(new NoSkipWorkTreeAction().updateIndexCommand()).isEqualByComparingTo(NO_SKIP_WORKTREE);
    }
}