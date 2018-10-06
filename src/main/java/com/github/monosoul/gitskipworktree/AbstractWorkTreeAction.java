package com.github.monosoul.gitskipworktree;

import static com.intellij.vcsUtil.VcsUtil.getVcsRootFor;
import static java.util.stream.Collectors.groupingBy;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitLineHandler;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

@Slf4j
abstract class AbstractWorkTreeAction extends AbstractVcsAction {

    protected abstract SkipWorkTreeCommand skipWorkTreeCommand();

    @Override
    protected final void update(@NotNull final VcsContext vcsContext, @NotNull final Presentation presentation) {
        val project = vcsContext.getProject();

        if (project == null || !ProjectLevelVcsManager.getInstance(project).hasActiveVcss()) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        presentation.setEnabled(!ProjectLevelVcsManager.getInstance(project).isBackgroundVcsOperationRunning());
        presentation.setVisible(true);
    }

    @Override
    protected final void actionPerformed(@NotNull final VcsContext e) {
        val project = e.getProject();
        if (project == null) {
            return;
        }

        val map = e.getSelectedFilesStream().collect(groupingBy(file -> Optional.ofNullable(getVcsRootFor(project, file))));
        map.entrySet().stream()
           .filter(entry -> entry.getKey().isPresent())
           .map(entry -> new SimpleImmutableEntry<>(entry.getKey().get(), entry.getValue()))
           .map(gitLineHandlerCreator(project))
           .map(Git.getInstance()::runCommand)
           .filter(r -> !r.success())
           .flatMap(r -> r.getErrorOutput().stream())
           .forEach(log::error);

        e.getSelectedFilesStream().forEach(VcsDirtyScopeManager.getInstance(project)::fileDirty);
    }

    Function<Entry<VirtualFile, List<VirtualFile>>, GitLineHandler> gitLineHandlerCreator(@NotNull final Project project) {
        return new GitLineHandlerCreator(project, skipWorkTreeCommand());
    }
}
