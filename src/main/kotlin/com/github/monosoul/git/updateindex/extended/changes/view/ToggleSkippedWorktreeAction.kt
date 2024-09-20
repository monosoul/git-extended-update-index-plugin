package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.changes.view.Constants.PROPERTY
import com.github.monosoul.git.updateindex.extended.support.PresentationUpdater
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangesViewManager

class ToggleSkippedWorktreeAction : ToggleAction() {

    private val logger = logger<ToggleSkippedWorktreeAction>()

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        super.update(event)
        event.run {
            project?.updatePresentation(presentation)
        }
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        return PropertiesComponent.getInstance().getBoolean(PROPERTY, false).also {
            logger.debug { "Get property value: $it" }
        }
    }

    override fun setSelected(event: AnActionEvent, state: Boolean) {
        PropertiesComponent.getInstance().setValue(PROPERTY, state)
        logger.debug("Set property value: {}", state)
        val project = event.project ?: return

        ChangesViewManager.getInstanceEx(project).scheduleRefresh()
    }

    private fun Project.updatePresentation(presentation: Presentation) {
        logger.debug("Updating presentation")
        getService(PresentationUpdater::class.java)?.invoke(presentation)
    }
}
