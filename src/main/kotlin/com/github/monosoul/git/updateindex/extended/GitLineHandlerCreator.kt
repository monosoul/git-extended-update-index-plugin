package com.github.monosoul.git.updateindex.extended

import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitLineHandler
import kotlin.collections.Map.Entry

interface GitLineHandlerCreator : (Entry<VirtualFile, List<VirtualFile>>) -> GitLineHandler {
    override fun invoke(vcsRootToFilesPair: Entry<VirtualFile, List<VirtualFile>>): GitLineHandler
}