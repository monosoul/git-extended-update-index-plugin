package com.github.monosoul.gitskipworktree;

import static com.github.monosoul.gitskipworktree.SkipWorkTreeCommand.NO_SKIP_WORKTREE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class NoSkipWorkTreeActionTest {

    @Test
    void skipWorkTreeCommand() {
        assertThat(new NoSkipWorkTreeAction().skipWorkTreeCommand()).isEqualByComparingTo(NO_SKIP_WORKTREE);
    }
}