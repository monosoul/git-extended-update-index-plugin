package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.AbstractMultiArgumentsSource
import com.github.monosoul.git.updateindex.extended.LIMIT
import com.github.monosoul.git.updateindex.extended.TestDisposable
import com.github.monosoul.git.updateindex.extended.registerService
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.openapi.vcs.actions.VcsContextFactory
import com.intellij.openapi.vcs.changes.committed.MockAbstractVcs
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.peer.impl.VcsContextFactoryImpl
import git4idea.GitVcs
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.config.GitExecutableManager
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verifyAll
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ArgumentsSources
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.map
import strikt.assertions.message
import kotlin.random.Random
import kotlin.random.nextInt

@ExtendWith(MockKExtension::class)
internal class GetSkippedWorktreeFilesTaskTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject
    private lateinit var vcs: MockAbstractVcs
    private lateinit var vcsRoot: VcsRoot
    private lateinit var vcsContextFactory: VcsContextFactoryImpl

    @MockK
    private lateinit var vcsManager: ProjectLevelVcsManager

    @MockK
    private lateinit var gitVcs: GitVcs

    @MockK(relaxed = true)
    private lateinit var gitExecutableManager: GitExecutableManager

    @MockK(relaxed = true)
    private lateinit var virtualFileManager: VirtualFileManager

    @MockK
    private lateinit var git: Git

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        ApplicationManager.setApplication(application, parent)
        application.registerService(git, parent)
        application.registerService(virtualFileManager, parent)

        project = MockProject(null, parent)

        every { vcsManager.findVcsByName(GitVcs.NAME) } returns gitVcs
        project.registerService(vcsManager, parent)

        application.registerService(gitExecutableManager, parent)

        vcsContextFactory = VcsContextFactoryImpl()
        application.registerService<VcsContextFactory>(vcsContextFactory, parent)

        vcs = MockAbstractVcs(project)
        vcsRoot = VcsRoot(vcs, MockVirtualFile(true, "vcsRoot"))

        every { vcsManager.allVcsRoots } returns arrayOf(vcsRoot)
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @ParameterizedTest
    @ArgumentsSources(
        ArgumentsSource(TestArguments.EmptyResult::class),
        ArgumentsSource(TestArguments.DoesntHaveSkippedFiles::class),
    )
    fun `should do nothing if the result doesn't contain skipped files`(result: GitCommandResult) {
        every { git.runCommand(any<GitLineHandler>()) } returns result

        val actual = runBlocking {
            getSkippedWorktreeFiles(project)
        }

        expectThat(actual).isEmpty()

        verifyGitCall()
    }

    @ParameterizedTest
    @ArgumentsSource(TestArguments.HaveSkippedFiles::class)
    fun `should return a list of skipped files`(result: GitCommandResult) {
        every { git.runCommand(any<GitLineHandler>()) } returns result

        val actual = runBlocking {
            getSkippedWorktreeFiles(project)
        }

        expectThat(actual)
            .hasSize(result.output.size)
            .map { it.path }
            .containsExactlyInAnyOrder(result.output.map { it.replace("S ", "MOCK_ROOT:/vcsRoot/") })

        verifyGitCall()
    }

    @ParameterizedTest
    @ArgumentsSource(TestArguments.Errors::class)
    fun `should throw an exception in case of an error`(result: GitCommandResult) {
        every { git.runCommand(any<GitLineHandler>()) } returns result

        expectThrows<VcsException> {
            runBlocking {
                getSkippedWorktreeFiles(project)
            }
        }.message isEqualTo result.errorOutputAsJoinedString

        verifyGitCall()
    }

    private fun verifyGitCall() {
        val captor = slot<GitLineHandler>()
        verifyAll {
            git.runCommand(capture(captor))
        }

        expectThat(captor.captured) {
            get { printableCommandLine() } isEqualTo "\"\" ls-files -v"
        }
    }

    private class TestArguments {
        class EmptyResult : AbstractMultiArgumentsSource(
            {
                GitCommandResult(false, 0, emptyList(), emptyList())
            }
        )

        class DoesntHaveSkippedFiles : AbstractMultiArgumentsSource(
            {
                val files = List(Random.nextInt(1..10)) {
                    "$it " + RandomStringUtils.randomAlphabetic(LIMIT)
                }
                GitCommandResult(false, 0, emptyList(), files)
            }
        )

        class HaveSkippedFiles : AbstractMultiArgumentsSource(
            {
                val files = List(Random.nextInt(1..10)) {
                    "S " + RandomStringUtils.randomAlphabetic(LIMIT)
                }
                GitCommandResult(false, 0, emptyList(), files)
            }
        )

        class Errors : AbstractMultiArgumentsSource(
            {
                val errors = listOf(RandomStringUtils.randomAlphabetic(LIMIT))
                GitCommandResult(false, Random.nextInt(1..10), errors, emptyList())
            }
        )
    }
}
