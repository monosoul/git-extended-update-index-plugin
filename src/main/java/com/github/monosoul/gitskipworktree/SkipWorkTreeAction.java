package com.github.monosoul.gitskipworktree;

import static com.github.monosoul.gitskipworktree.SkipWorkTreeCommand.SKIP_WORKTREE;

public final class SkipWorkTreeAction extends AbstractWorkTreeAction {

    @Override
    protected SkipWorkTreeCommand skipWorkTreeCommand() {
        return SKIP_WORKTREE;
    }
}