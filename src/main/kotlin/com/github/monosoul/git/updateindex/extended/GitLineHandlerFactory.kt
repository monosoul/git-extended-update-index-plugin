package com.github.monosoul.git.updateindex.extended

import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitLineHandler

interface GitLineHandlerFactory : (ExtendedUpdateIndexCommand, VirtualFile, List<VirtualFile>) -> GitLineHandler {
    override fun invoke(
            command: ExtendedUpdateIndexCommand,
            vcsRoot: VirtualFile,
            files: List<VirtualFile>
    ): GitLineHandler
}