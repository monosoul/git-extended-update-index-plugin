package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.SKIP_WORKTREE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class SkipWorkTreeActionTest {

    @Test
    void should_have_skip_worktree_update_index_command() {
        assertThat(new SkipWorkTreeAction().updateIndexCommand()).isEqualByComparingTo(SKIP_WORKTREE);
    }
}