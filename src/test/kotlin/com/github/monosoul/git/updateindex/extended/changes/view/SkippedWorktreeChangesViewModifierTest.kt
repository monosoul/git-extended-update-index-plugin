package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.AbstractMultiArgumentsSource
import com.github.monosoul.git.updateindex.extended.LIMIT
import com.github.monosoul.git.updateindex.extended.TestDisposable
import com.github.monosoul.git.updateindex.extended.changes.view.Constants.PROPERTY
import com.github.monosoul.git.updateindex.extended.registerService
import com.intellij.ide.util.AppPropertyService
import com.intellij.ide.util.PropertiesComponent
import com.intellij.mock.MockApplication
import com.intellij.mock.MockLocalFileSystem
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.LocalFilePath
import com.intellij.openapi.vcs.changes.ui.ChangesViewModelBuilder
import com.intellij.openapi.vcs.changes.ui.NoneChangesGroupingFactory
import com.intellij.openapi.vcs.changes.ui.TreeModelBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.map
import kotlin.random.Random
import kotlin.random.nextInt

@Suppress("UnstableApiUsage")
@ExtendWith(MockKExtension::class)
internal class SkippedWorktreeChangesViewModifierTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject
    private lateinit var propertiesComponent: AppPropertyService
    private lateinit var localFileSystem: MockLocalFileSystem

    @MockK(relaxUnitFun = true)
    private lateinit var progressManager: ProgressManager

    @MockK(relaxUnitFun = true)
    private lateinit var virtualFileManager: VirtualFileManager

    private lateinit var modifier: SkippedWorktreeChangesViewModifier

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        ApplicationManager.setApplication(application, parent)

        project = MockProject(null, parent)

        propertiesComponent = AppPropertyService()
        application.registerService<PropertiesComponent>(propertiesComponent, parent)

        application.registerService(progressManager, parent)

        localFileSystem = MockLocalFileSystem()
        application.registerService<LocalFileSystem>(localFileSystem, parent)

        application.registerService(virtualFileManager, parent)
        every { virtualFileManager.getFileSystem(any()) } returns localFileSystem

        modifier = SkippedWorktreeChangesViewModifier(project)
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @Test
    fun `should do nothing if there is no value for the property`(
        @MockK modelBuilder: ChangesViewModelBuilder
    ) {
        modifier.modifyTreeModelBuilder(modelBuilder)

        verify {
            progressManager wasNot Called
        }
    }

    @Test
    fun `should do nothing if the property value is false`(
        @MockK modelBuilder: ChangesViewModelBuilder
    ) {
        propertiesComponent.setValue(PROPERTY, false)

        modifier.modifyTreeModelBuilder(modelBuilder)

        verify {
            progressManager wasNot Called
        }
    }

    @ParameterizedTest
    @ArgumentsSource(FilesArgumentSource::class)
    fun `should add a new root node with skipped files`(files: List<FilePath>) {
        every { progressManager.run(any<GetSkippedWorktreeFilesTask>()) } returns files

        propertiesComponent.setValue(PROPERTY, true)
        val builder = TreeModelBuilder(project, NoneChangesGroupingFactory)

        modifier.modifyTreeModelBuilder(builder)
        val model = builder.build()

        expectThat(model) {
            get { root }.isNotNull()
            get { getChildCount(root) } isEqualTo 1
            get { getChild(root, 0) }.isA<ChangesBrowserSkippedWorktreeNode>() and {
                get { allFilesUnder }
                    .hasSize(files.size)
                    .map { it.path }
                    .containsExactlyInAnyOrder(files.map { "/${it.path}" })
            }
        }
    }

    private class FilesArgumentSource : AbstractMultiArgumentsSource({
        List<FilePath>(Random.nextInt(1..10)) {
            LocalFilePath(RandomStringUtils.randomAlphabetic(LIMIT), false)
        }
    })
}
