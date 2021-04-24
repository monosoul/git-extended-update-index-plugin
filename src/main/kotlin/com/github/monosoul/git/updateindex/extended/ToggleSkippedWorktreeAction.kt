package com.github.monosoul.git.updateindex.extended

import com.github.monosoul.git.updateindex.extended.logging.Slf4j
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.openapi.vcs.changes.IgnoredViewDialog
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserSpecificFilePathsNode
import com.intellij.openapi.vcs.changes.ui.ChangesListView
import com.intellij.openapi.vcs.changes.ui.NoneChangesGroupingFactory
import com.intellij.openapi.vcs.changes.ui.TreeModelBuilder
import com.intellij.vcsUtil.VcsUtil
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler

class ToggleSkippedWorktreeAction : ToggleAction() {

    private val logger by Slf4j

    private companion object {
        const val PROPERTY = "com.github.monosoul.git-extended-update-index-plugin.showSkipped"
        const val SKIPPED_FILE = "S"
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return PropertiesComponent.getInstance().getBoolean(PROPERTY, false).also {
            logger.debug("Get property value: {}", it)
        }
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        PropertiesComponent.getInstance().setValue(PROPERTY, state)
        logger.debug("Set property value: {}", state)
        val project = e.project ?: return

        val vcsManager = ProjectLevelVcsManager.getInstance(project) ?: return

        val changeListView = e.getData(ChangesListView.DATA_KEY) ?: return

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Getting files list") {
                override fun run(indicator: ProgressIndicator) {

                    val skippedFiles = vcsManager.run {
                        allVcsRoots.map(VcsRoot::getPath).mapNotNull { vcsRoot ->
                            GitLineHandler(project, vcsRoot, GitCommand.LS_FILES).apply {
                                addParameters("-v")
                            }.let(Git.getInstance()::runCommand).takeIf { it.success() }?.let { result ->
                                result.output.filter {
                                    it.startsWith(SKIPPED_FILE)
                                }.map {
                                    it.removePrefix("$SKIPPED_FILE ")
                                }.map {
                                    VcsUtil.getFilePath(vcsRoot, GitUtil.unescapePath(it))
                                }
                            }
                        }
                    }.flatten()


                    logger.info(skippedFiles.joinToString(", ") { it.path })
                    val rootNode = ChangesBrowserSkippedFilesNode(project, skippedFiles)
                    val modelBuilder = TreeModelBuilder(project, NoneChangesGroupingFactory)
                        .insertSubtreeRoot(rootNode)
                    skippedFiles.forEach { filePath ->
                        modelBuilder.insertChangeNode(filePath, rootNode, ChangesBrowserNode.createFilePath(filePath, FileStatus.IGNORED))
                    }
                    changeListView.updateModel(
                        modelBuilder.build()
                    )
                }

            }
        )
    }

    class ChangesBrowserSkippedFilesNode(
        project: Project,
        files: Collection<FilePath>
    ) : ChangesBrowserSpecificFilePathsNode<ChangesBrowserNode.Tag>(
        TagImpl("Skipped Worktree"),
        files,
        { if (!project.isDisposed) IgnoredViewDialog(project).show() }
    ) {
        override fun getTextPresentation() = getUserObject().toString()
    }
}