package com.github.monosoul.git.updateindex.extended.support

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifySequence
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PresentationUpdaterTest {

    @MockK
    private lateinit var project: Project
    @MockK
    private lateinit var vcsManager: ProjectLevelVcsManager
    @MockK(relaxUnitFun = true)
    private lateinit var presentation: Presentation
    @InjectMockKs
    private lateinit var updater: PresentationUpdater

    @Test
    fun `should make the presentation disabled and invisible when VCS manager is null`() {
        every { project.getService(ProjectLevelVcsManager::class.java) } returns null

        updater.invoke(presentation)

        verifySequence {
            presentation.isEnabledAndVisible = false
        }
    }

    @Test
    fun `should make the presentation visible and disabled when background VCS operation is running`() {
        every { project.getService(ProjectLevelVcsManager::class.java) } returns vcsManager
        every { vcsManager.isBackgroundVcsOperationRunning } returns true

        updater.invoke(presentation)

        verifySequence {
            presentation.isVisible = true
            presentation.isEnabled = false
        }
    }

    @Test
    fun `should make the presentation visible and enabled when VCS manager is not null and no background operation`() {
        every { project.getService(ProjectLevelVcsManager::class.java) } returns vcsManager
        every { vcsManager.isBackgroundVcsOperationRunning } returns false

        updater.invoke(presentation)

        verifySequence {
            presentation.isVisible = true
            presentation.isEnabled = true
        }
    }
}