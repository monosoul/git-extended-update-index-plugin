package com.github.monosoul.git.updateindex.extended

enum class ExtendedUpdateIndexCommand(val value: String) {
    SKIP_WORKTREE("--skip-worktree"),
    NO_SKIP_WORKTREE("--no-skip-worktree"),
    MAKE_EXECUTABLE("--chmod=+x"),
    MAKE_NOT_EXECUTABLE("--chmod=-x")
}