package com.github.monosoul.gitskipworktree;

import static com.github.monosoul.gitskipworktree.SkipWorkTreeCommand.NO_SKIP_WORKTREE;

public class NoSkipWorkTreeAction extends AbstractWorkTreeAction {

    @Override
    protected SkipWorkTreeCommand skipWorkTreeCommand() {
        return NO_SKIP_WORKTREE;
    }
}