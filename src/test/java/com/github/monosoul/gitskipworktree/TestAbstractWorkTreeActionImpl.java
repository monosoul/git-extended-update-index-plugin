package com.github.monosoul.gitskipworktree;

import org.jetbrains.annotations.NotNull;

class TestAbstractWorkTreeActionImpl extends AbstractWorkTreeAction {

    private final SkipWorkTreeCommand skipWorkTreeCommand;

    TestAbstractWorkTreeActionImpl(@NotNull final SkipWorkTreeCommand skipWorkTreeCommand) {
        this.skipWorkTreeCommand = skipWorkTreeCommand;
    }

    @Override
    protected SkipWorkTreeCommand skipWorkTreeCommand() {
        return skipWorkTreeCommand;
    }
}
