package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.TestDisposable
import com.github.monosoul.git.updateindex.extended.changes.view.Constants.PROPERTY
import com.github.monosoul.git.updateindex.extended.registerService
import com.github.monosoul.git.updateindex.extended.support.PresentationUpdater
import com.intellij.ide.util.AppPropertyService
import com.intellij.ide.util.PropertiesComponent
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.changes.ChangesViewEx
import com.intellij.openapi.vcs.changes.ChangesViewI
import com.intellij.openapi.vcs.changes.ChangesViewManager
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifyAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse

@Suppress("UnstableApiUsage")
@ExtendWith(MockKExtension::class)
internal class ToggleSkippedWorktreeActionTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject
    private lateinit var propertiesComponent: AppPropertyService

    @MockK(relaxUnitFun = true)
    private lateinit var presentationUpdater: PresentationUpdater

    @MockK(relaxUnitFun = true)
    private lateinit var changesViewManager: ChangesViewManager

    private lateinit var action: ToggleSkippedWorktreeAction

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        ApplicationManager.setApplication(application, parent)

        project = MockProject(null, parent)

        propertiesComponent = AppPropertyService()
        application.registerService<PropertiesComponent>(propertiesComponent, parent)

        project.registerService<ChangesViewI>(changesViewManager, parent)
        project.registerService<ChangesViewEx>(changesViewManager, parent)

        project.registerService(presentationUpdater, parent)


        action = ToggleSkippedWorktreeAction()
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @Test
    fun `isSelected should return false when the property has not been set yet`(@MockK event: AnActionEvent) {
        val actual = action.isSelected(event)

        expectThat(actual).isFalse()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `isSelected should return property value when it is set`(
        isSelected: Boolean,
        @MockK event: AnActionEvent,
    ) {
        propertiesComponent.setValue(PROPERTY, isSelected)

        val actual = action.isSelected(event)

        expectThat(actual) isEqualTo isSelected
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `setSelected should set property value and trigger changes view refresh`(
        isSelected: Boolean,
        @MockK event: AnActionEvent,
    ) {
        every { event.project } returns project
        action.setSelected(event, isSelected)

        expectThat(propertiesComponent.getBoolean(PROPERTY)) isEqualTo isSelected

        verifyAll {
            changesViewManager.refreshImmediately()
        }
    }

    @Test
    fun `setSelected should do nothing if project is null`(@MockK event: AnActionEvent) {
        every { event.project } returns null

        expectThat(propertiesComponent.isValueSet(PROPERTY)).isFalse()

        verifyAll {
            changesViewManager wasNot Called
        }
    }

    @Test
    fun `update should call presentation updater`(
        @MockK(relaxed = true) event: AnActionEvent,
        @MockK(relaxed = true) presentation: Presentation
    ) {
        every { event.project } returns project
        every { event.presentation } returns presentation

        action.update(event)

        verifyAll {
            presentationUpdater.invoke(presentation)
        }
    }
}
