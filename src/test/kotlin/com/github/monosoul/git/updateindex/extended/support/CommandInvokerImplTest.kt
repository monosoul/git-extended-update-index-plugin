package com.github.monosoul.git.updateindex.extended.support

import com.github.monosoul.git.updateindex.extended.*
import com.github.monosoul.git.updateindex.extended.registerService
import com.github.monosoul.git.updateindex.extended.support.CommandInvokerImplTest.FilesAndCommandArgumentsSource.NoVcsRoot
import com.github.monosoul.git.updateindex.extended.support.CommandInvokerImplTest.FilesAndCommandArgumentsSource.WithVcsRoot
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager.setApplication
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomUtils.nextInt
import org.apache.log4j.Appender
import org.apache.log4j.Level.ERROR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream.generate
import kotlin.streams.toList

@ExtendWith(MockKExtension::class)
internal class CommandInvokerImplTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject

    private lateinit var appender: Appender

    @MockK
    private lateinit var git: Git
    @MockK
    private lateinit var vcsManager: ProjectLevelVcsManager
    @MockK(relaxUnitFun = true)
    private lateinit var dirtyScopeManager: VcsDirtyScopeManager
    @MockK
    private lateinit var gitLineHandlerFactory: GitLineHandlerFactory
    @MockK
    private lateinit var gitLineHandler: GitLineHandler
    @MockK
    private lateinit var gitCommandResult: GitCommandResult

    private lateinit var invoker: CommandInvokerImpl

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        setApplication(application, parent)
        application.registerService(git, parent)

        project = MockProject(null, parent)
        project.registerService(vcsManager, parent)
        project.registerService(dirtyScopeManager, parent)
        project.registerService(gitLineHandlerFactory, parent)

        appender = mockedAppender<CommandInvokerImpl>()

        every { gitLineHandlerFactory.invoke(any(), any(), any()) } returns gitLineHandler
        every { git.runCommand(any<GitLineHandler>()) } returns gitCommandResult

        invoker = CommandInvokerImpl(project)
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @ParameterizedTest
    @ArgumentsSource(NoVcsRoot::class)
    fun `should do nothing if a file has no VCS root`(files: Array<VirtualFile>, command: ExtendedUpdateIndexCommand) {
        every { vcsManager.getVcsRootFor(any<VirtualFile>()) } returns null

        invoker.invoke(files, command)

        verifyOrder {
            vcsManager.getVcsRootFor(files[0])
            dirtyScopeManager.filesDirty(withArg {
                assertThat(it).isEmpty()
            }, null)
        }
        verifyAll {
            git wasNot Called
            gitLineHandlerFactory wasNot Called
            gitLineHandler wasNot Called
        }
    }

    @ParameterizedTest
    @ArgumentsSource(WithVcsRoot::class)
    internal fun `should execute a command and log nothing if it was successful`(
            files: Array<VirtualFile>,
            command: ExtendedUpdateIndexCommand
    ) {
        val root = mockk<VirtualFile>()
        every { vcsManager.getVcsRootFor(any<VirtualFile>()) } returns root

        every { gitCommandResult.success() } returns true

        invoker.invoke(files, command)

        verify(exactly = files.size) {
            vcsManager.getVcsRootFor(any<VirtualFile>())
        }
        verifyOrder {
            gitLineHandlerFactory.invoke(command, root, withArg {
                assertThat(it).containsExactlyInAnyOrder(*files)
            })
            git.runCommand(gitLineHandler)
            gitCommandResult.success()
            dirtyScopeManager.filesDirty(withArg {
                assertThat(it).containsExactlyInAnyOrder(*files)
            }, null)
        }
        verify(inverse = true) {
            appender.doAppend(any())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(WithVcsRoot::class)
    internal fun `should execute a command and log errors if it was unsuccessful`(
            files: Array<VirtualFile>,
            command: ExtendedUpdateIndexCommand
    ) {
        val root = mockk<VirtualFile>()
        every { vcsManager.getVcsRootFor(any<VirtualFile>()) } returns root

        every { gitCommandResult.success() } returns false
        val error = randomAlphabetic(LIMIT)
        every { gitCommandResult.errorOutput } returns listOf(error)

        invoker.invoke(files, command)

        verify(exactly = files.size) {
            vcsManager.getVcsRootFor(any<VirtualFile>())
        }
        verifyOrder {
            gitLineHandlerFactory.invoke(command, root, withArg {
                assertThat(it).containsExactlyInAnyOrder(*files)
            })
            git.runCommand(gitLineHandler)
            gitCommandResult.success()
            gitCommandResult.errorOutput
            appender.doAppend(withArg {
                assertThat(it.message).isEqualTo(error)
                assertThat(it.getLevel()).isEqualTo(ERROR)
            })
            dirtyScopeManager.filesDirty(withArg {
                assertThat(it).containsExactlyInAnyOrder(*files)
            }, null)
        }
    }

    private sealed class FilesAndCommandArgumentsSource(
            vararg generators: () -> Any?
    ) : AbstractMultiArgumentsSource(*generators) {

        class NoVcsRoot : FilesAndCommandArgumentsSource(
                { arrayOf(mockk<VirtualFile>()) },
                { randomEnum<ExtendedUpdateIndexCommand>() }
        )

        class WithVcsRoot : FilesAndCommandArgumentsSource(
                { virtualFileArray() },
                { randomEnum<ExtendedUpdateIndexCommand>() }
        )

        companion object {
            private fun virtualFileArray() = generate { mockk<VirtualFile>() }
                    .limit(nextInt(1, LIMIT))
                    .toList()
                    .toTypedArray()
        }
    }
}