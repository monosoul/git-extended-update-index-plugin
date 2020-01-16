package com.github.monosoul.git.updateindex.extended

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitCommand.UPDATE_INDEX
import git4idea.commands.GitLineHandler
import kotlin.collections.Map.Entry

class GitLineHandlerCreatorImpl(
        private val project: Project,
        private val updateIndexCommand: ExtendedUpdateIndexCommand
) : GitLineHandlerCreator {

    override fun invoke(vcsRootToFilesPair: Entry<VirtualFile, List<VirtualFile>>) =
            vcsRootToFilesPair.let { (vscRoot, files) ->
                GitLineHandler(project, vscRoot, UPDATE_INDEX).apply {
                    addParameters(updateIndexCommand.command)
                    addRelativeFiles(files)
                }
            }
}