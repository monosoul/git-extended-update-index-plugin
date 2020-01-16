package com.github.monosoul.git.updateindex.extended

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.application.ApplicationManager.setApplication
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.config.GitExecutableManager
import git4idea.config.GitVersion
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomUtils.nextInt
import java.util.stream.Collectors.toList
import java.util.stream.Stream.generate
import kotlin.collections.Map.Entry

internal class GitLineHandlerFactoryImplSpec : BehaviorSpec() {

    private companion object {
        const val LIMIT = 10
        val CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND = GitVersion(1, 7, 1, 0)
    }

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject
    private lateinit var updateIndexCommand: ExtendedUpdateIndexCommand

    @MockK
    private lateinit var gitExecutableManager: GitExecutableManager
    @MockK
    private lateinit var vcsManager: ProjectLevelVcsManager
    @MockK
    private lateinit var gitVcs: GitVcs

    override fun beforeTest(testCase: TestCase) {
        init(this, relaxUnitFun = true)

        parent = TestDisposable()

        application = MockApplication(parent)
        setApplication(application, parent)
        application.registerService(gitExecutableManager, parent)

        project = MockProject(null, parent)
        project.registerService(vcsManager, parent)

        updateIndexCommand = enumValues<ExtendedUpdateIndexCommand>().random()

        every { gitVcs.version } returns CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND
        every { vcsManager.findVcsByName(GitVcs.NAME) } returns gitVcs
        every { gitExecutableManager.getPathToGit(any()) } returns randomAlphabetic(LIMIT)
        every { gitExecutableManager.tryGetVersion(any()) } returns CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        dispose(parent)
    }

    init {
        generate { rootVcsToFileListEntry() }.limit(LIMIT.toLong()).forEach {
            given("VCS root to file list entry $it") {
                `when`("invoke method is called") {
                    val actual = GitLineHandlerFactoryImpl(project).invoke(updateIndexCommand, it.key, it.value)
                    val expected = buildExpected(updateIndexCommand.command, it.value)

                    then("should build a proper command") {
                        actual.printableCommandLine().shouldBe(expected)
                    }
                }
            }
        }
    }

    private fun buildExpected(commandString: String, files: List<VirtualFile>): String {
        return "git update-index $commandString " + files.joinToString(" ", transform = VirtualFile::getName)
    }

    private fun rootVcsToFileListEntry() = mockk<Entry<VirtualFile, List<VirtualFile>>> {
        val root = MockVirtualFile(true, randomAlphabetic(LIMIT))

        every { key } returns root
        every { value } returns mockVirtualFiles().onEach { it.parent = root }
    }

    private fun mockVirtualFiles() = generate { MockVirtualFile(randomAlphabetic(LIMIT)) }
            .limit(nextInt(1, LIMIT).toLong())
            .collect(toList())
}