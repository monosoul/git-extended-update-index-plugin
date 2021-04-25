package com.github.monosoul.git.updateindex.extended.changes.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.IgnoredViewDialog
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode.Tag
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserSpecificFilePathsNode

class ChangesBrowserSkippedWorktreeNode(
    project: Project,
    files: Collection<FilePath>
) : ChangesBrowserSpecificFilePathsNode<Tag>(
    TagImpl("Skipped Worktree"),
    files,
    { if (!project.isDisposed) IgnoredViewDialog(project).show() }
) {
    override fun getTextPresentation() = getUserObject().toString()
}