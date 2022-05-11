package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexTaskTest.FilesAndCommandArgumentsSource.NoVcsRoot
import com.github.monosoul.git.updateindex.extended.ExtendedUpdateIndexTaskTest.FilesAndCommandArgumentsSource.WithVcsRoot
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager.setApplication
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifyOrder
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils.nextInt
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder

@ExtendWith(MockKExtension::class)
internal class ExtendedUpdateIndexTaskTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject

    @MockK
    private lateinit var git: Git

    @MockK
    private lateinit var vcsManager: ProjectLevelVcsManager

    @MockK(relaxUnitFun = true)
    private lateinit var dirtyScopeManager: VcsDirtyScopeManager

    @MockK
    private lateinit var updateIndexLineHandlerFactory: UpdateIndexLineHandlerFactory

    @MockK
    private lateinit var gitLineHandler: GitLineHandler

    @MockK
    private lateinit var gitCommandResult: GitCommandResult

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        setApplication(application, parent)
        application.registerService(git, parent)

        project = MockProject(null, parent)
        project.registerService(vcsManager, parent)
        project.registerService(dirtyScopeManager, parent)
        project.registerService(updateIndexLineHandlerFactory, parent)

        every { updateIndexLineHandlerFactory.invoke(any(), any(), any()) } returns gitLineHandler
        every { git.runCommand(any<GitLineHandler>()) } returns gitCommandResult
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @ParameterizedTest
    @ArgumentsSource(NoVcsRoot::class)
    fun `should do nothing if a file has no VCS root`(
        files: List<VirtualFile>,
        command: ExtendedUpdateIndexCommand,
        @MockK indicator: ProgressIndicator
    ) {
        every { vcsManager.getVcsRootFor(any<VirtualFile>()) } returns null

        ExtendedUpdateIndexTask(project, files, command).run(indicator)

        verifyOrder {
            vcsManager.getVcsRootFor(files[0])
        }
        verifyAll {
            git wasNot Called
            updateIndexLineHandlerFactory wasNot Called
            gitLineHandler wasNot Called
            dirtyScopeManager wasNot Called
        }
    }

    @ParameterizedTest
    @ArgumentsSource(WithVcsRoot::class)
    internal fun `should execute a command and log nothing if it was successful`(
        files: List<VirtualFile>,
        command: ExtendedUpdateIndexCommand,
        @MockK indicator: ProgressIndicator
    ) {
        val root = mockk<VirtualFile>()
        every { vcsManager.getVcsRootFor(any<VirtualFile>()) } returns root

        every { gitCommandResult.success() } returns true

        ExtendedUpdateIndexTask(project, files, command).run(indicator)

        verify(exactly = files.size) {
            vcsManager.getVcsRootFor(any<VirtualFile>())
        }
        verifyOrder {
            updateIndexLineHandlerFactory.invoke(command, root, withArg {
                expectThat(it) containsExactlyInAnyOrder files
            })
            git.runCommand(gitLineHandler)
            gitCommandResult.success()
        }
        verify(exactly = files.size) {
            dirtyScopeManager.fileDirty(any<VirtualFile>())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(WithVcsRoot::class)
    internal fun `should execute a command and log errors if it was unsuccessful`(
        files: List<VirtualFile>,
        command: ExtendedUpdateIndexCommand,
        @MockK indicator: ProgressIndicator
    ) {
        val root = mockk<VirtualFile>()
        every { vcsManager.getVcsRootFor(any<VirtualFile>()) } returns root

        every { gitCommandResult.success() } returns false
        val error = RandomStringUtils.randomAlphabetic(LIMIT)
        every { gitCommandResult.errorOutput } returns listOf(error)

        ExtendedUpdateIndexTask(project, files, command).run(indicator)

        verify(exactly = files.size) {
            vcsManager.getVcsRootFor(any<VirtualFile>())
        }
        verifyOrder {
            updateIndexLineHandlerFactory.invoke(command, root, withArg {
                expectThat(it) containsExactlyInAnyOrder files
            })
            git.runCommand(gitLineHandler)
            gitCommandResult.success()
            gitCommandResult.errorOutput
        }
        verify(exactly = files.size) {
            dirtyScopeManager.fileDirty(any<VirtualFile>())
        }
    }

    private sealed class FilesAndCommandArgumentsSource(
        vararg generators: () -> Any?
    ) : AbstractMultiArgumentsSource(*generators) {

        class NoVcsRoot : FilesAndCommandArgumentsSource(
            { listOf(mockk<VirtualFile>()) },
            { randomEnum<ExtendedUpdateIndexCommand>() }
        )

        class WithVcsRoot : FilesAndCommandArgumentsSource(
            { virtualFileList() },
            { randomEnum<ExtendedUpdateIndexCommand>() }
        )

        companion object {
            private fun virtualFileList() = generateSequence { mockk<VirtualFile>() }
                .take(nextInt(1, LIMIT))
                .toList()
        }
    }
}
