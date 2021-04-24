package com.github.monosoul.git.updateindex.extended

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ToggleSkippedFilesAction : ToggleAction() {
    override fun isSelected(e: AnActionEvent): Boolean {
        return false
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {

    }
}