package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.support.CommandInvoker
import com.github.monosoul.git.updateindex.extended.support.PresentationUpdater
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.actions.VcsContextWrapper
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

@ExtendWith(MockKExtension::class)
internal class ExtendedUpdateIndexActionTest {

    @MockK(relaxUnitFun = true)
    private lateinit var commandInvoker: CommandInvoker
    @MockK(relaxUnitFun = true)
    private lateinit var presentationUpdater: PresentationUpdater
    @MockK
    private lateinit var context: VcsContextWrapper
    @MockK
    private lateinit var presentation: Presentation
    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var event: AnActionEvent

    @BeforeEach
    internal fun setUp() {
        mockkStatic(VcsContextWrapper::class)

        every { VcsContextWrapper.createInstanceOn(any()) } returns context
        every { VcsContextWrapper.createCachedInstanceOn(any()) } returns context
        every { event.presentation } returns presentation
    }

    @AfterEach
    internal fun tearDown() {
        clearStaticMockk(VcsContextWrapper::class)
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should do nothing if project is null`(action: ExtendedUpdateIndexAction) {
        every { context.project } returns null

        action.update(event)
        action.actionPerformed(event)

        verify {
            presentationUpdater wasNot Called
            presentation wasNot Called
            commandInvoker wasNot Called
        }
        verify(inverse = true) {
            context.selectedFiles
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should do nothing if presentation updater is null`(action: ExtendedUpdateIndexAction) {
        every { context.project } returns project
        every { project.getService(PresentationUpdater::class.java) } returns null

        action.update(event)

        verifySequence {
            context.project
            project.getService(PresentationUpdater::class.java)
            presentationUpdater wasNot Called
            presentation wasNot Called
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should call presentation updater when project is not null and update is called`(
            action: ExtendedUpdateIndexAction
    ) {
        every { context.project } returns project
        every { project.getService(PresentationUpdater::class.java) } returns presentationUpdater

        action.update(event)

        verifySequence {
            context.project
            project.getService(PresentationUpdater::class.java)
            presentationUpdater.invoke(presentation)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should do nothing if command invoker is null`(action: ExtendedUpdateIndexAction) {
        every { context.project } returns project
        every { project.getService(CommandInvoker::class.java) } returns null

        val selectedFiles = emptyArray<VirtualFile>()
        every { context.selectedFiles } returns selectedFiles

        action.actionPerformed(event)

        verify {
            context.project
            context.selectedFiles
            project.getService(CommandInvoker::class.java)
            commandInvoker wasNot Called
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should call command invoker when project is not null and actionPerformed is called`(
            action: ExtendedUpdateIndexAction
    ) {
        every { context.project } returns project
        every { project.getService(CommandInvoker::class.java) } returns commandInvoker

        val selectedFiles = emptyArray<VirtualFile>()
        every { context.selectedFiles } returns selectedFiles

        action.actionPerformed(event)

        verifySequence {
            context.project
            context.selectedFiles
            project.getService(CommandInvoker::class.java)
            commandInvoker.invoke(selectedFiles, any())
        }
    }

    private class ExtendedUpdateIndexActionArgumentsSource : AbstractMultiArgumentsSource({
        listOf(
                MakeExecutableAction(),
                MakeNotExecutableAction(),
                NoSkipWorkTreeAction(),
                SkipWorkTreeAction()
        ).random()
    })
}