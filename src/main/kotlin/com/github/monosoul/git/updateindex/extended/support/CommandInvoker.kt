package com.github.monosoul.git.updateindex.extended.support

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand
import com.github.monosoul.git.updateindex.extended.GitLineHandlerFactory
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil.getVcsRootFor
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import org.slf4j.LoggerFactory

@Service
class CommandInvoker(private val project: Project) {

    private val logger = javaClass.let(LoggerFactory::getLogger)

    operator fun invoke(selectedFiles: Array<VirtualFile>, command: ExtendedUpdateIndexCommand) {
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
                    .apply {
                        vcsDirtyScopeManager.filesDirty(values.flatten(), null)
                    }
        }
    }

    private val Project.gitLineHandlerFactory: GitLineHandlerFactory
        get() = getService(GitLineHandlerFactory::class.java)

    private val Project.vcsDirtyScopeManager: VcsDirtyScopeManager
        get() = let(VcsDirtyScopeManager::getInstance)

    private fun Project.fileToVcsRoot(file: VirtualFile) = getVcsRootFor(this, file)?.let { file to it }
}