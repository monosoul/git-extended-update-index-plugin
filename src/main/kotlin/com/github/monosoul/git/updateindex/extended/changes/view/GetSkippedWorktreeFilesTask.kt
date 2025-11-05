package com.github.monosoul.git.updateindex.extended.changes.view

import com.github.monosoul.git.updateindex.extended.changes.view.Constants.SKIPPED_FILE
import com.intellij.externalProcessAuthHelper.AuthenticationMode.NONE
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.vcsUtil.VcsUtil
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand.LS_FILES
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler

fun getSkippedWorktreeFiles(project: Project): List<FilePath> {
    val vcsManager = ProjectLevelVcsManager.getInstance(project) ?: return emptyList()

    return vcsManager.allVcsRoots.map(VcsRoot::getPath).map { vcsRoot ->
        GitLineHandler(project, vcsRoot, LS_FILES).apply {
            addParameters("-v")
            ignoreAuthenticationMode = NONE
        }.let(Git.getInstance()::runCommand).mapOrThrow { result ->
            result.filter {
                it.startsWith(SKIPPED_FILE)
            }.map {
                it.removePrefix("$SKIPPED_FILE ")
            }.map {
                VcsUtil.getFilePath(vcsRoot, GitUtil.unescapePath(it))
            }
        }
    }.flatten()
}

@Throws(VcsException::class)
private fun <T> GitCommandResult.mapOrThrow(mapper: (List<String>) -> T): T {
    throwOnError()

    return mapper(output)
}
