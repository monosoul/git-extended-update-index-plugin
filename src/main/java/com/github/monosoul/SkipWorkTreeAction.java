package com.github.monosoul;

import static com.github.monosoul.SkipWorkTreeCommand.SKIP_WORKTREE;

public class SkipWorkTreeAction extends AbstractWorkTreeAction {

    @Override
    protected SkipWorkTreeCommand skipWorkTreeCommand() {
        return SKIP_WORKTREE;
    }
}