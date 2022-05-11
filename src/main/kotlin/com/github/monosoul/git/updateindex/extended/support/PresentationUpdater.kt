package com.github.monosoul.git.updateindex.extended.support

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager

@Service
class PresentationUpdater(private val project: Project) {

    private val logger = logger<PresentationUpdater>()

    operator fun invoke(presentation: Presentation) {
        val vcsManager: ProjectLevelVcsManager? = ProjectLevelVcsManager.getInstance(project)

        presentation.apply {
            if (vcsManager == null) {
                logger.debug("No VCS manager available. Hiding the plugin menus.")
                isEnabledAndVisible = false
            } else {
                logger.debug("VCS manager is available. Showing the plugin menus.")
                isVisible = true
                isEnabled = !vcsManager.isBackgroundVcsOperationRunning
            }
        }
    }
}
