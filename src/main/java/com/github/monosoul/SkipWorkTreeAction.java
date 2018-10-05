package com.github.monosoul;

import static com.github.monosoul.SkipWorkTreeCommands.SKIP_WORKTREE;
import static git4idea.commands.GitCommand.UPDATE_INDEX;
import static java.util.Arrays.stream;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import git4idea.commands.Git;
import git4idea.commands.GitLineHandler;
import org.jetbrains.annotations.NotNull;

public class SkipWorkTreeAction extends AbstractVcsAction {

    @Override
    protected void update(@NotNull final VcsContext vcsContext, @NotNull final Presentation presentation) {

    }

    @Override
    protected void actionPerformed(@NotNull final VcsContext e) {
        if (e.getProject() == null) {
            return;
        }
        stream(e.getSelectedFilePaths()).map(FilePath::getPath).forEach(path -> {
            final GitLineHandler handler = new GitLineHandler(e.getProject(), e.getProject().getBaseDir(), UPDATE_INDEX);
            handler.addParameters(SKIP_WORKTREE, path);
            Git.getInstance().runCommandWithoutCollectingOutput(handler);
        });
    }
}