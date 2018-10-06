package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.NO_SKIP_WORKTREE;

public final class NoSkipWorkTreeAction extends AbstractExtendedUpdateIndexAction {

    @Override
    protected ExtendedUpdateIndexCommand updateIndexCommand() {
        return NO_SKIP_WORKTREE;
    }
}