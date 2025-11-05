package com.github.monosoul.git.updateindex.extended.changes.view

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.ChangesViewManager
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
class SkippedWorktreeFilesCache(private val project: Project) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val cachedFiles = AtomicReference<List<FilePath>?>(null)

    init {
        Disposer.register(project) { scope.cancel() }
    }

    fun getOrLoad(): List<FilePath>? {
        val cached = cachedFiles.get()
        if (cached != null) {
            return cached
        }

        scope.launch {
            try {
                val files = withBackgroundProgress(project, "Getting Skipped Files", cancellable = false) {
                    getSkippedWorktreeFiles(project)
                }
                cachedFiles.set(files)
                ChangesViewManager.getInstanceEx(project).scheduleRefresh()
            } catch (e: Exception) {
                logger.warn("Failed to load skipped worktree files", e)
            }
        }

        return null
    }

    fun clear() {
        cachedFiles.set(null)
    }

    /**
     * For testing purposes: set cached files directly without async loading
     */
    internal fun setCachedFiles(files: List<FilePath>) {
        cachedFiles.set(files)
    }

    companion object {
        private val logger = logger<SkippedWorktreeFilesCache>()

        fun getInstance(project: Project): SkippedWorktreeFilesCache = project.service()
    }
}
