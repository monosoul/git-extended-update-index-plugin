package com.github.monosoul;

import static com.intellij.vcsUtil.VcsUtil.getVcsRootFor;
import static git4idea.commands.GitCommand.UPDATE_INDEX;
import static java.util.stream.Collectors.groupingBy;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitLineHandler;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

@Slf4j
abstract class AbstractWorkTreeAction extends AbstractVcsAction {

    protected abstract SkipWorkTreeCommand skipWorkTreeCommand();

    @Override
    protected void update(@NotNull final VcsContext vcsContext, @NotNull final Presentation presentation) {

    }

    @Override
    protected void actionPerformed(@NotNull final VcsContext e) {
        val project = e.getProject();
        if (project == null) {
            return;
        }

        val map = e.getSelectedFilesStream().collect(groupingBy(file -> getVcsRootFor(project, file)));
        map.entrySet().stream()
           .map(new GitLineHandlerCreator(project, skipWorkTreeCommand()))
           .map(Git.getInstance()::runCommand)
           .filter(r -> !r.success())
           .flatMap(r -> r.getErrorOutput().stream())
           .forEach(log::error);

        e.getSelectedFilesStream().forEach(VcsDirtyScopeManager.getInstance(project)::fileDirty);
    }

    private static class GitLineHandlerCreator implements Function<Entry<VirtualFile, List<VirtualFile>>, GitLineHandler> {

        private final Project project;
        private final SkipWorkTreeCommand skipWorkTreeCommand;

        private GitLineHandlerCreator(@NotNull final Project project, @NotNull final SkipWorkTreeCommand skipWorkTreeCommand) {
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
}
