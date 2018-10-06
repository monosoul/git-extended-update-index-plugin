package com.github.monosoul.gitskipworktree;

import static git4idea.commands.GitCommand.UPDATE_INDEX;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitLineHandler;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.val;
import org.jetbrains.annotations.NotNull;

class GitLineHandlerCreator implements Function<Entry<VirtualFile, List<VirtualFile>>, GitLineHandler> {

    private final Project project;
    private final SkipWorkTreeCommand skipWorkTreeCommand;

    GitLineHandlerCreator(@NotNull final Project project, @NotNull final SkipWorkTreeCommand skipWorkTreeCommand) {
        this.project = project;
        this.skipWorkTreeCommand = skipWorkTreeCommand;
    }

    @Override
    public GitLineHandler apply(@NotNull final Entry<VirtualFile, List<VirtualFile>> rootToFilesPair) {
        val handler = new GitLineHandler(project, rootToFilesPair.getKey(), UPDATE_INDEX);
        handler.addParameters(skipWorkTreeCommand.getCommand());
        handler.addRelativeFiles(rootToFilesPair.getValue());

        return handler;
    }
}
