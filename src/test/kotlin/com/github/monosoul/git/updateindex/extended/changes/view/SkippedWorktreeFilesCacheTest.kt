package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.AbstractMultiArgumentsSource
import com.github.monosoul.git.updateindex.extended.LIMIT
import com.github.monosoul.git.updateindex.extended.TestDisposable
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer.dispose
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.LocalFilePath
import io.mockk.junit5.MockKExtension
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.map
import kotlin.random.Random
import kotlin.random.nextInt

@ExtendWith(MockKExtension::class)
internal class SkippedWorktreeFilesCacheTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject
    private lateinit var cache: SkippedWorktreeFilesCache

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        ApplicationManager.setApplication(application, parent)

        project = MockProject(null, parent)

        cache = SkippedWorktreeFilesCache(project)
        project.registerService(SkippedWorktreeFilesCache::class.java, cache, parent)
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @Test
    fun `getOrLoad should return null when cache is empty`() {
        expectThat(cache.getOrLoad()).isNull()
    }

    @ParameterizedTest
    @ArgumentsSource(FilesArgumentSource::class)
    fun `getOrLoad should return cached files when cache is populated`(files: List<FilePath>) {
        cache.setCachedFiles(files)

        val result = cache.getOrLoad()

        expectThat(result).isNotNull().and {
            get { size } isEqualTo files.size
            get { map { it.path } } containsExactlyInAnyOrder files.map { it.path }
        }
    }

    @Test
    fun `getOrLoad should return the same cached files on multiple calls`() {
        val files = generateFiles(5)
        cache.setCachedFiles(files)

        val firstCall = cache.getOrLoad()
        val secondCall = cache.getOrLoad()
        val thirdCall = cache.getOrLoad()

        expect {
            that(firstCall).isNotNull()
            that(secondCall).isNotNull()
            that(thirdCall).isNotNull()
            that(firstCall).isEqualTo(secondCall)
            that(secondCall).isEqualTo(thirdCall)
        }
    }

    @Test
    fun `clear should remove cached files`() {
        val files = generateFiles(3)
        cache.setCachedFiles(files)

        expectThat(cache.getOrLoad()).isNotNull()

        cache.clear()

        expectThat(cache.getOrLoad()).isNull()
    }

    @Test
    fun `clear should allow repopulating cache`() {
        val firstFiles = generateFiles(2)
        val secondFiles = generateFiles(3)

        cache.setCachedFiles(firstFiles)
        expectThat(cache.getOrLoad()).isNotNull().and {
            get { size } isEqualTo firstFiles.size
        }

        cache.clear()

        cache.setCachedFiles(secondFiles)
        expectThat(cache.getOrLoad()).isNotNull().and {
            get { size } isEqualTo secondFiles.size
            get { map { it.path } } containsExactlyInAnyOrder secondFiles.map { it.path }
        }
    }

    @Test
    fun `getOrLoad should return null immediately after clear even if files were cached`() {
        val files = generateFiles(5)
        cache.setCachedFiles(files)

        expectThat(cache.getOrLoad()).isNotNull()

        cache.clear()

        expectThat(cache.getOrLoad()).isNull()
    }

    @Test
    fun `getInstance should return the same instance for the same project`() {
        val instance1 = SkippedWorktreeFilesCache.getInstance(project)
        val instance2 = SkippedWorktreeFilesCache.getInstance(project)

        expectThat(instance1).isSameInstanceAs(instance2)
    }

    @Test
    fun `cache should handle empty list`() {
        cache.setCachedFiles(emptyList())

        val result = cache.getOrLoad()

        expectThat(result).isNotNull().and {
            get { isEmpty() } isEqualTo true
        }
    }

    private fun generateFiles(count: Int): List<FilePath> {
        return List(count) {
            LocalFilePath(RandomStringUtils.randomAlphabetic(LIMIT), false)
        }
    }

    private class FilesArgumentSource : AbstractMultiArgumentsSource({
        List<FilePath>(Random.nextInt(1..10)) {
            LocalFilePath(RandomStringUtils.randomAlphabetic(LIMIT), false)
        }
    })
}
