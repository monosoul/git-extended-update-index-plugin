package com.github.monosoul.git.updateindex.extended.changes.view

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.ChangesViewManager
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class SkippedWorktreeFilesCache(private val project: Project) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var cachedFiles: List<FilePath>? = null
    private var isLoading = false

    init {
        // Cancel the scope when the project is disposed
        Disposer.register(project, object : Disposable {
            override fun dispose() {
                scope.cancel()
            }
        })
    }

    fun getOrLoad(): List<FilePath>? {
        if (cachedFiles != null) {
            return cachedFiles
        }

        if (!isLoading) {
            isLoading = true
            scope.launch {
                try {
                    val files = withBackgroundProgress(project, "Getting Skipped Files", cancellable = false) {
                        getSkippedWorktreeFiles(project)
                    }
                    cachedFiles = files
                    ApplicationManager.getApplication().invokeLater {
                        ChangesViewManager.getInstanceEx(project).scheduleRefresh()
                    }
                } catch (e: Exception) {
                    // Log error but don't crash
                    logger.warn("Failed to load skipped worktree files", e)
                } finally {
                    isLoading = false
                }
            }
        }

        return null
    }

    fun clear() {
        cachedFiles = null
    }

    /**
     * For testing purposes: set cached files directly without async loading
     */
    internal fun setCachedFiles(files: List<FilePath>) {
        cachedFiles = files
    }

    companion object {
        private val logger = logger<SkippedWorktreeFilesCache>()

        fun getInstance(project: Project): SkippedWorktreeFilesCache = project.service()
    }
}
