package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.support.CommandInvoker
import com.github.monosoul.git.updateindex.extended.support.PresentationUpdater
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifySequence
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
    private lateinit var presentation: Presentation

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var dataContext: DataContext

    @MockK
    private lateinit var event: AnActionEvent

    @BeforeEach
    internal fun setUp() {
        every { event.presentation } returns presentation
        every { event.project } returns project
        every { event.dataContext } returns dataContext
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should do nothing if project is null`(action: ExtendedUpdateIndexAction) {
        every { event.project } returns null

        action.update(event)
        action.actionPerformed(event)

        verify {
            presentationUpdater wasNot Called
            presentation wasNot Called
            commandInvoker wasNot Called
        }
        verify(inverse = true) {
            event.dataContext
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should do nothing if presentation updater is null`(action: ExtendedUpdateIndexAction) {
        every { project.getService(PresentationUpdater::class.java) } returns null

        action.update(event)

        verifySequence {
            event.project
            event.presentation
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
        every { project.getService(PresentationUpdater::class.java) } returns presentationUpdater

        action.update(event)

        verifySequence {
            event.project
            event.presentation
            project.getService(PresentationUpdater::class.java)
            presentationUpdater.invoke(presentation)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should do nothing if command invoker is null`(action: ExtendedUpdateIndexAction) {
        every { project.getService(CommandInvoker::class.java) } returns null

        val selectedFiles = emptyList<VirtualFile>()
        every {
            VcsDataKeys.VIRTUAL_FILES.getData(dataContext)
        } returns selectedFiles

        action.actionPerformed(event)

        verifyAll {
            event.project
            event.dataContext
            project.getService(CommandInvoker::class.java)
            commandInvoker wasNot Called
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExtendedUpdateIndexActionArgumentsSource::class)
    fun `should call command invoker when project is not null and actionPerformed is called`(
            action: ExtendedUpdateIndexAction
    ) {
        every { project.getService(CommandInvoker::class.java) } returns commandInvoker

        val selectedFiles = emptyList<VirtualFile>()
        every {
            VcsDataKeys.VIRTUAL_FILES.getData(dataContext)
        } returns selectedFiles

        action.actionPerformed(event)

        verifyAll {
            event.project
            event.dataContext
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