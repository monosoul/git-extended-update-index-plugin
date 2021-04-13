package com.github.monosoul.git.updateindex.extended.support

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexCommand
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexTask
import com.github.monosoul.git.updateindex.extended.TestDisposable
import com.github.monosoul.git.updateindex.extended.registerService
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager.setApplication
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verifyAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class CommandInvokerTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject

    @MockK(relaxUnitFun = true)
    private lateinit var progressManager: ProgressManager

    private lateinit var commandInvoker: CommandInvoker

    private val taskCaptor = slot<Task>()

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        setApplication(application, parent)
        application.registerService(progressManager, parent)

        project = MockProject(null, parent)

        commandInvoker = CommandInvoker(project)
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @Test
    fun `should create a task and run it with progress manager`(
        @MockK selectedFiles: Iterable<VirtualFile>,
        @MockK command: ExtendedUpdateIndexCommand
    ) {
        commandInvoker.invoke(selectedFiles, command)

        verifyAll {
            progressManager.run(capture(taskCaptor))
        }

        val task = taskCaptor.captured

        expectThat(task).isA<ExtendedUpdateIndexTask>() and {
            get { this.project } isSameInstanceAs project
            get { this.selectedFiles } isSameInstanceAs selectedFiles
            get { this.command } isSameInstanceAs command
        }
    }
}