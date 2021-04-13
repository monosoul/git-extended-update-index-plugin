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

@Service
class CommandInvoker(private val project: Project) {

    private val logger by Slf4j

    operator fun invoke(selectedFiles: Iterable<VirtualFile>, command: ExtendedUpdateIndexCommand) {
        logger.debug("Calling {} against {}", command, selectedFiles)

        project.run {
            selectedFiles
                    .mapNotNull { fileToVcsRoot(it) }
                    .groupBy({ it.second }, { it.first })
                    .apply {
                        map { (vcsRoot, files) -> gitLineHandlerFactory(command, vcsRoot, files) }
                                .map(Git.getInstance()::runCommand)
                                .filterNot(GitCommandResult::success)
                                .flatMap(GitCommandResult::getErrorOutput)
                                .forEach(logger::error)
                    }
                    .values.flatten().forEach(vcsDirtyScopeManager::fileDirty)
        }
    }

    private val Project.gitLineHandlerFactory: GitLineHandlerFactory
        get() = getService(GitLineHandlerFactory::class.java)

    private val Project.vcsDirtyScopeManager: VcsDirtyScopeManager
        get() = VcsDirtyScopeManager.getInstance(this)

    private fun Project.fileToVcsRoot(file: VirtualFile) = getVcsRootFor(this, file)?.let { file to it }
}