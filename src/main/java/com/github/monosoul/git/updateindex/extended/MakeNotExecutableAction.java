package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_NOT_EXECUTABLE;

public class MakeNotExecutableAction extends AbstractExtendedUpdateIndexAction {

    @Override
    protected ExtendedUpdateIndexCommand updateIndexCommand() {
        return MAKE_NOT_EXECUTABLE;
    }
}
