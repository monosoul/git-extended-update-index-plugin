package com.github.monosoul.git.updateindex.extended.support

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand
import com.github.monosoul.git.updateindex.extended.GitLineHandlerFactory
import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil.getVcsRootFor
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler

@Service
class CommandInvoker(private val project: Project) {

    private val logger by Slf4j

    operator fun invoke(selectedFiles: Iterable<VirtualFile>, command: ExtendedUpdateIndexCommand) {
        logger.debug("Calling {} against {}", command, selectedFiles)

        project.run {
            selectedFiles
                    .mapNotNull { fileToVcsRoot(it) }
                    .groupBy({ (_, vcsRoot) -> vcsRoot }, { (file, _) -> file })
                    .onEach { (vcsRoot, files) ->
                        gitLineHandlerFactory(command, vcsRoot, files).runAndLog()
                    }
                    .values.flatten().forEach(vcsDirtyScopeManager::fileDirty)
        }
    }

    private fun GitLineHandler.runAndLog() = run(Git.getInstance()::runCommand)
            .takeUnless(GitCommandResult::success)
            ?.let(GitCommandResult::getErrorOutput)
            ?.forEach(logger::error)

    private val Project.gitLineHandlerFactory: GitLineHandlerFactory
        get() = getService(GitLineHandlerFactory::class.java)

    private val Project.vcsDirtyScopeManager: VcsDirtyScopeManager
        get() = VcsDirtyScopeManager.getInstance(this)

    private fun Project.fileToVcsRoot(file: VirtualFile) = getVcsRootFor(this, file)?.let { file to it }
}