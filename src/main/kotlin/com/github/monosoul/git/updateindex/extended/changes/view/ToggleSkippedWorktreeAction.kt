package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.changes.view.Constants.PROPERTY
import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.vcs.changes.ChangesViewManager

class ToggleSkippedWorktreeAction : ToggleAction() {

    private val logger by Slf4j

    override fun isSelected(e: AnActionEvent): Boolean {
        return PropertiesComponent.getInstance().getBoolean(PROPERTY, false).also {
            logger.debug("Get property value: {}", it)
        }
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        PropertiesComponent.getInstance().setValue(PROPERTY, state)
        logger.debug("Set property value: {}", state)
        val project = e.project ?: return

        ChangesViewManager.getInstanceEx(project).refreshImmediately()
    }
}