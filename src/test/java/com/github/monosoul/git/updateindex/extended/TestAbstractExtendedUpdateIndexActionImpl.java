package com.github.monosoul.git.updateindex.extended;

import org.jetbrains.annotations.NotNull;

class TestAbstractExtendedUpdateIndexActionImpl extends AbstractExtendedUpdateIndexAction {

    private final ExtendedUpdateIndexCommand updateIndexCommand;

    TestAbstractExtendedUpdateIndexActionImpl(@NotNull final ExtendedUpdateIndexCommand updateIndexCommand) {
        this.updateIndexCommand = updateIndexCommand;
    }

    @Override
    protected ExtendedUpdateIndexCommand updateIndexCommand() {
        return updateIndexCommand;
    }
}
