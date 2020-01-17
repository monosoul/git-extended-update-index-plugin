package com.github.monosoul.git.updateindex.extended.support

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager

@Service
class PresentationUpdater(private val project: Project) {
    operator fun invoke(presentation: Presentation) {
        val vcsManager: ProjectLevelVcsManager? = project.let(ProjectLevelVcsManager::getInstance)

        presentation.apply {
            if (vcsManager == null) {
                isEnabledAndVisible = false
            } else {
                isVisible = true
                isEnabled = !vcsManager.isBackgroundVcsOperationRunning
            }
        }
    }
}