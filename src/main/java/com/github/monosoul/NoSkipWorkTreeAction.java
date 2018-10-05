package com.github.monosoul;

import static com.github.monosoul.SkipWorkTreeCommand.NO_SKIP_WORKTREE;

public class NoSkipWorkTreeAction extends AbstractWorkTreeAction {

    @Override
    protected SkipWorkTreeCommand skipWorkTreeCommand() {
        return NO_SKIP_WORKTREE;
    }
}