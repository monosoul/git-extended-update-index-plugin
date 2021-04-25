package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.TestDisposable
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer.dispose
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
internal class ChangesBrowserSkippedWorktreeNodeTest {

    private lateinit var parent: TestDisposable
    private lateinit var application: MockApplication
    private lateinit var project: MockProject

    @BeforeEach
    fun setUp() {
        parent = TestDisposable()

        application = MockApplication(parent)
        ApplicationManager.setApplication(application, parent)

        project = MockProject(null, parent)
    }

    @AfterEach
    fun tearDown() {
        dispose(parent)
    }

    @Test
    fun `getTextPresentation method should delegate the call to string representation of user object`() {
        val actual = ChangesBrowserSkippedWorktreeNode(project, emptyList()).textPresentation

        expectThat(actual) isEqualTo "Skipped Worktree"
    }
}