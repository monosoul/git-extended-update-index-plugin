package com.github.monosoul.git.updateindex.extended;

enum ExtendedUpdateIndexCommand {

    SKIP_WORKTREE("--skip-worktree"),
    NO_SKIP_WORKTREE("--no-skip-worktree");

    private final String command;

    ExtendedUpdateIndexCommand(final String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
