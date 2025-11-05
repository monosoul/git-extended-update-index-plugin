package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.changes.view.SkippedWorktreeFilesCache
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangesViewManager
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler

class ExtendedUpdateIndexTask(
    project: Project,
    val selectedFiles: Iterable<VirtualFile>,
    val command: ExtendedUpdateIndexCommand
) : Task.Backgroundable(project, "Running git update-index") {

    private val logger = logger<ExtendedUpdateIndexTask>()

    override fun run(indicator: ProgressIndicator) {
        project.run {
            selectedFiles
                .mapNotNull { fileToVcsRoot(it) }
                .groupBy({ (_, vcsRoot) -> vcsRoot }, { (file, _) -> file })
                .onEach { (vcsRoot, files) ->
                    updateIndexLineHandlerFactory(command, vcsRoot, files).runAndLog()
                    files.forEach(vcsDirtyScopeManager::fileDirty)
                }
        }

        logger.debug("Git update index command executed, refreshing Changes view")

        SkippedWorktreeFilesCache.getInstance(project).clear()
        ChangesViewManager.getInstanceEx(project).scheduleRefresh()
    }

    private fun GitLineHandler.runAndLog() = run(Git.getInstance()::runCommand)
        .takeUnless(GitCommandResult::success)
        ?.let(GitCommandResult::getErrorOutput)
        ?.forEach(logger::warn)

    private val Project.updateIndexLineHandlerFactory: UpdateIndexLineHandlerFactory
        get() = getService(UpdateIndexLineHandlerFactory::class.java)

    private val Project.vcsDirtyScopeManager: VcsDirtyScopeManager
        get() = VcsDirtyScopeManager.getInstance(this)

    private fun Project.fileToVcsRoot(file: VirtualFile) = VcsUtil.getVcsRootFor(this, file)?.let { file to it }
}
