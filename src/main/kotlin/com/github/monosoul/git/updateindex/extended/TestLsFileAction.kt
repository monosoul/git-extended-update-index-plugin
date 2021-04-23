package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.vcsUtil.VcsUtil
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler

class TestLsFileAction : DumbAwareAction() {
    private val logger by Slf4j

    companion object {
        const val SKIPPED_FILE = "S"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val vcsManager: ProjectLevelVcsManager? = ProjectLevelVcsManager.getInstance(project)

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Getting files list") {
                override fun run(indicator: ProgressIndicator) {

                    vcsManager?.run {
                        allVcsRoots.map { vcsRoot ->
                            GitLineHandler(project, vcsRoot.path, GitCommand.LS_FILES).apply {
                                addParameters("-v")
                            }.let(Git.getInstance()::runCommand).let { result ->
                                if (result.success()) {
                                    result.output.filter {
                                        it.startsWith(SKIPPED_FILE)
                                    }.map {
                                        it.removePrefix("$SKIPPED_FILE ")
                                    }.map {
                                        VcsUtil.getFilePath(vcsRoot.path, GitUtil.unescapePath(it))
                                    }.forEach {
                                        logger.info(it.toString())
                                    }
                                }
                            }
                        }
                    }
                }

            }
        )
    }
}