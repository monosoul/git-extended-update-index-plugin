package com.github.monosoul.gitskipworktree;

import static com.github.monosoul.gitskipworktree.SkipWorkTreeCommand.SKIP_WORKTREE;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class SkipWorkTreeActionTest {

    @Test
    void skipWorkTreeCommand() {
        assertThat(new SkipWorkTreeAction().skipWorkTreeCommand()).isEqualByComparingTo(SKIP_WORKTREE);
    }
}