package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_EXECUTABLE;

public class MakeExecutableAction extends AbstractExtendedUpdateIndexAction {

    @Override
    protected ExtendedUpdateIndexCommand updateIndexCommand() {
        return MAKE_EXECUTABLE;
    }
}
