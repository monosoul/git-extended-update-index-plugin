package com.github.monosoul.git.updateindex.extended;

import static git4idea.commands.GitCommand.UPDATE_INDEX;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitLineHandler;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.val;
import org.jetbrains.annotations.NotNull;

final class GitLineHandlerCreator implements Function<Entry<VirtualFile, List<VirtualFile>>, GitLineHandler> {

    private final Project project;
    private final ExtendedUpdateIndexCommand updateIndexCommand;

    GitLineHandlerCreator(@NotNull final Project project, @NotNull final ExtendedUpdateIndexCommand updateIndexCommand) {
        this.project = project;
        this.updateIndexCommand = updateIndexCommand;
    }

    @Override
    public GitLineHandler apply(@NotNull final Entry<VirtualFile, List<VirtualFile>> rootToFilesPair) {
        val handler = new GitLineHandler(project, rootToFilesPair.getKey(), UPDATE_INDEX);
        handler.addParameters(updateIndexCommand.getCommand());
        handler.addRelativeFiles(rootToFilesPair.getValue());

        return handler;
    }
}
