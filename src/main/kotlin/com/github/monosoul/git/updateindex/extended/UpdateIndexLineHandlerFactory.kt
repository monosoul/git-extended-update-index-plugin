package com.github.monosoul.git.updateindex.extended

import com.intellij.externalProcessAuthHelper.AuthenticationMode.NONE
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitCommand.UPDATE_INDEX
import git4idea.commands.GitLineHandler

@Service
class UpdateIndexLineHandlerFactory(private val project: Project) {

    private val logger = logger<UpdateIndexLineHandlerFactory>()

    operator fun invoke(
        command: ExtendedUpdateIndexCommand,
        vcsRoot: VirtualFile,
        files: List<VirtualFile>
    ): GitLineHandler {
        logger.debug {
            "Building git line handler. Command=$command; VCS Root=$vcsRoot, Files=$files"
        }

        return GitLineHandler(project, vcsRoot, UPDATE_INDEX).apply {
            addParameters(command.value)
            addRelativeFiles(files)
            ignoreAuthenticationMode = NONE
        }
    }
}
