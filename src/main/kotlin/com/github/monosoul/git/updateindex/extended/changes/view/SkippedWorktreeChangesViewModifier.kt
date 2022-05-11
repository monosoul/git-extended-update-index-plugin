package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.changes.view.Constants.PROPERTY
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangesViewModifier
import com.intellij.openapi.vcs.changes.ui.ChangesViewModelBuilder

class SkippedWorktreeChangesViewModifier(private val project: Project) : ChangesViewModifier {

    private val logger = logger<SkippedWorktreeChangesViewModifier>()

    override fun modifyTreeModelBuilder(modelBuilder: ChangesViewModelBuilder) {
        val showSkippedTree = PropertiesComponent.getInstance().getBoolean(PROPERTY, false)
        if (!showSkippedTree) {
            logger.debug { "Show skipped files is turned off. Doing nothing." }
            return
        }

        val skippedFiles = ProgressManager.getInstance().run(GetSkippedWorktreeFilesTask(project))

        logger.debug { "Skipped files: $skippedFiles" }

        val rootNode = ChangesBrowserSkippedWorktreeNode(project, skippedFiles)
        modelBuilder.insertSubtreeRoot(rootNode)
        modelBuilder.insertFilesIntoNode(skippedFiles.mapNotNull { it.virtualFile }, rootNode)
    }
}
