package com.github.monosoul;

import static com.intellij.vcsUtil.VcsUtil.getVcsRootFor;
import static git4idea.commands.GitCommand.UPDATE_INDEX;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import git4idea.commands.Git;
import git4idea.commands.GitLineHandler;
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

        val map = stream(e.getSelectedFiles()).collect(groupingBy(file -> getVcsRootFor(project, file)));
        map.entrySet().stream()
           .map(entry -> {
               val handler = new GitLineHandler(project, entry.getKey(), UPDATE_INDEX);
               handler.addParameters(skipWorkTreeCommand().getCommand());
               handler.addRelativeFiles(entry.getValue());

               return handler;
           })
           .map(Git.getInstance()::runCommand)
           .filter(r -> !r.success())
           .flatMap(r -> r.getErrorOutput().stream())
           .forEach(log::error);
    }
}
