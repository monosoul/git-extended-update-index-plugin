package com.github.monosoul.git.updateindex.extended.support

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand
import com.intellij.openapi.vfs.VirtualFile

interface CommandInvoker : (Array<VirtualFile>, ExtendedUpdateIndexCommand) -> Unit {
    override fun invoke(selectedFiles: Array<VirtualFile>, command: ExtendedUpdateIndexCommand)
}