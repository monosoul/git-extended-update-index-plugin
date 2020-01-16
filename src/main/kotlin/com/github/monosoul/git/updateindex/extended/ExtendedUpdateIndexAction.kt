package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_EXECUTABLE
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.MAKE_NOT_EXECUTABLE
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.NO_SKIP_WORKTREE
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand.SKIP_WORKTREE
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.actions.AbstractVcsAction
import com.intellij.openapi.vcs.actions.VcsContext
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil.getVcsRootFor
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import org.slf4j.LoggerFactory
import kotlin.collections.Map.Entry

sealed class ExtendedUpdateIndexAction(private val command: ExtendedUpdateIndexCommand) : AbstractVcsAction() {

    private val logger = javaClass.let(LoggerFactory::getLogger)

    override fun update(context: VcsContext, presentation: Presentation) {
        val vcsManager = context.project?.let(ProjectLevelVcsManager::getInstance)
        presentation.apply {
            if (vcsManager == null) {
                isEnabledAndVisible = false
            } else {
                isVisible = true
                isEnabled = !vcsManager.isBackgroundVcsOperationRunning
            }
        }
    }

    override fun actionPerformed(context: VcsContext) {
        context.apply {
            project?.run {
                selectedFiles
                        .mapNotNull { fileToVcsRoot(it) }
                        .groupBy({ it.second }, { it.first })
                        .apply {
                            map { gitLineHandler(it) }
                                    .map(Git.getInstance()::runCommand)
                                    .filterNot(GitCommandResult::success)
                                    .flatMap(GitCommandResult::getErrorOutput)
                                    .forEach(logger::error)
                        }
                        .apply {
                            vcsDirtyScopeManager.filesDirty(values.flatten(), null)
                        }
            }
        }
    }

    private fun Project.gitLineHandler(entry: Entry<VirtualFile, List<VirtualFile>>) =
            entry.let { (vcsRoot, files) ->
                getComponent(GitLineHandlerFactory::class.java).invoke(command, vcsRoot, files)
            }

    private val Project.vcsDirtyScopeManager: VcsDirtyScopeManager
        get() = let(VcsDirtyScopeManager::getInstance)

    private fun Project.fileToVcsRoot(file: VirtualFile) = getVcsRootFor(this, file)?.let { file to it }
}

class MakeExecutableAction : ExtendedUpdateIndexAction(MAKE_EXECUTABLE)

class MakeNotExecutableAction : ExtendedUpdateIndexAction(MAKE_NOT_EXECUTABLE)

class NoSkipWorkTreeAction : ExtendedUpdateIndexAction(NO_SKIP_WORKTREE)

class SkipWorkTreeAction : ExtendedUpdateIndexAction(SKIP_WORKTREE)