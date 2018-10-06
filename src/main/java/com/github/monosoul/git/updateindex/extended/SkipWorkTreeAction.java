package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.SKIP_WORKTREE;

public final class SkipWorkTreeAction extends AbstractExtendedUpdateIndexAction {

    @Override
    protected ExtendedUpdateIndexCommand updateIndexCommand() {
        return SKIP_WORKTREE;
    }
}