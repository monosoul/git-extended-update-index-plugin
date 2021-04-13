package com.github.monosoul.git.updateindex.extended

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.application.ApplicationManager.setApplication
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.commands.GitAuthenticationMode.NONE
import git4idea.config.GitExecutable
import git4idea.config.GitExecutableManager
import git4idea.config.GitVersion
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomUtils.nextInt
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream.generate
import kotlin.streams.toList

@ExtendWith(MockKExtension::class)
internal class GitLineHandlerFactoryTest {

    private companion object {
        val CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND = GitVersion(1, 7, 1, 0)
    }

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject
    private lateinit var updateIndexCommand: ExtendedUpdateIndexCommand
    private lateinit var exePath: String

    @MockK
    private lateinit var gitExecutableManager: GitExecutableManager

    @MockK
    private lateinit var vcsManager: ProjectLevelVcsManager

    @MockK
    private lateinit var gitExecutable: GitExecutable

    @MockK
    private lateinit var gitVcs: GitVcs

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        setApplication(application, parent)
        application.registerService(gitExecutableManager, parent)

        project = MockProject(null, parent)
        project.registerService(vcsManager, parent)

        updateIndexCommand = randomEnum()
        exePath = randomAlphabetic(LIMIT)

        every { gitVcs.version } returns CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND
        every { vcsManager.findVcsByName(GitVcs.NAME) } returns gitVcs
        every { gitExecutableManager.getPathToGit(any()) } returns randomAlphabetic(LIMIT)
        every { gitExecutableManager.tryGetVersion(any()) } returns CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND
        every { gitExecutableManager.getExecutable(project) } returns gitExecutable
        every { gitExecutable.exePath } returns exePath
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @RepeatedTest(LIMIT)
    fun `should build a GitLineHandler with supplied command and files`() {
        val (vcsRoot, files) = vcsRootToFileListPair()

        val actual = GitLineHandlerFactory(project).invoke(updateIndexCommand, vcsRoot, files)
        val expected = buildExpected(updateIndexCommand, files)

        expectThat(actual) {
            get { printableCommandLine() } isEqualTo expected
            get { ignoreAuthenticationMode } isEqualTo NONE
        }
    }

    private fun buildExpected(command: ExtendedUpdateIndexCommand, files: List<VirtualFile>) =
            "git update-index ${command.value} " + files.joinToString(" ", transform = VirtualFile::getName)

    private fun vcsRootToFileListPair() = MockVirtualFile(true, randomAlphabetic(LIMIT)).let { root ->
        root to mockVirtualFiles().onEach { it.parent = root }
    }

    private fun mockVirtualFiles() = generate { MockVirtualFile(randomAlphabetic(LIMIT)) }
            .limit(nextInt(1, LIMIT))
            .toList()
}