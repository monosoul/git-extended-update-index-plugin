package com.github.monosoul.git.updateindex.extended

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler

class GitLineHandlerFactoryImpl(private val project: Project) : GitLineHandlerFactory {

    override fun invoke(command: ExtendedUpdateIndexCommand, vcsRoot: VirtualFile, files: List<VirtualFile>) =
            GitLineHandler(project, vcsRoot, GitCommand.UPDATE_INDEX).apply {
                addParameters(command.command)
                addRelativeFiles(files)
            }
}