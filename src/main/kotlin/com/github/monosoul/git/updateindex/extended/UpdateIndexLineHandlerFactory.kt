package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitAuthenticationMode.NONE
import git4idea.commands.GitCommand.UPDATE_INDEX
import git4idea.commands.GitLineHandler

@Service
class UpdateIndexLineHandlerFactory(private val project: Project) {

    private val logger by Slf4j

    operator fun invoke(
        command: ExtendedUpdateIndexCommand,
        vcsRoot: VirtualFile,
        files: List<VirtualFile>
    ): GitLineHandler {
        logger.debug("Building git line handler. Command={}; VCS Root={}, Files={}", command, vcsRoot, files)

        return GitLineHandler(project, vcsRoot, UPDATE_INDEX).apply {
            addParameters(command.value)
            addRelativeFiles(files)
            ignoreAuthenticationMode = NONE
        }
    }
}