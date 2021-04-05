package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_EXECUTABLE
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_NOT_EXECUTABLE
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.NO_SKIP_WORKTREE
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.SKIP_WORKTREE
import com.github.monosoul.git.updateindex.extended.support.CommandInvoker
import com.github.monosoul.git.updateindex.extended.support.PresentationUpdater
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.actions.VcsContextUtil
import com.intellij.openapi.vfs.VirtualFile

sealed class ExtendedUpdateIndexAction(private val command: ExtendedUpdateIndexCommand) : DumbAwareAction() {
    override fun update(event: AnActionEvent) {
        event.run {
            project?.updatePresentation(presentation)
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.invokeCommand(VcsContextUtil.selectedFiles(event.dataContext))
    }

    private fun Project.invokeCommand(selectedFiles: Iterable<VirtualFile>) {
        getService(CommandInvoker::class.java)?.invoke(selectedFiles, command)
    }

    private fun Project.updatePresentation(presentation: Presentation) {
        getService(PresentationUpdater::class.java)?.invoke(presentation)
    }
}

class MakeExecutableAction : ExtendedUpdateIndexAction(MAKE_EXECUTABLE)

class MakeNotExecutableAction : ExtendedUpdateIndexAction(MAKE_NOT_EXECUTABLE)

class NoSkipWorkTreeAction : ExtendedUpdateIndexAction(NO_SKIP_WORKTREE)

class SkipWorkTreeAction : ExtendedUpdateIndexAction(SKIP_WORKTREE)