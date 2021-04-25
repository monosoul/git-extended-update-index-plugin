package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.changes.view.Constants.PROPERTY
import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.github.monosoul.git.updateindex.extended.support.PresentationUpdater
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangesViewManager

class ToggleSkippedWorktreeAction : ToggleAction() {

    private val logger by Slf4j

    override fun update(event: AnActionEvent) {
        super.update(event)
        event.run {
            project?.updatePresentation(presentation)
        }
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        return PropertiesComponent.getInstance().getBoolean(PROPERTY, false).also {
            logger.debug("Get property value: {}", it)
        }
    }

    override fun setSelected(event: AnActionEvent, state: Boolean) {
        PropertiesComponent.getInstance().setValue(PROPERTY, state)
        logger.debug("Set property value: {}", state)
        val project = event.project ?: return

        ChangesViewManager.getInstanceEx(project).refreshImmediately()
    }

    private fun Project.updatePresentation(presentation: Presentation) {
        logger.debug("Updating presentation")
        getService(PresentationUpdater::class.java)?.invoke(presentation)
    }
}