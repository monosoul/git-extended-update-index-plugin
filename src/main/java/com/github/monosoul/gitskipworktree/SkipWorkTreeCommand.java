package com.github.monosoul.gitskipworktree;

enum SkipWorkTreeCommand {

    SKIP_WORKTREE("--skip-worktree"),
    NO_SKIP_WORKTREE("--no-skip-worktree");

    private final String command;

    SkipWorkTreeCommand(final String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
