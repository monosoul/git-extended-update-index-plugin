package com.github.monosoul.git.updateindex.extended.support

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexTask
import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

@Service
class CommandInvoker(private val project: Project) {

    private val logger by Slf4j

    operator fun invoke(selectedFiles: Iterable<VirtualFile>, command: ExtendedUpdateIndexCommand) {
        logger.debug("Calling {} against {}", command, selectedFiles)

        ProgressManager.getInstance().run(
            ExtendedUpdateIndexTask(project, selectedFiles, command)
        )
    }
}