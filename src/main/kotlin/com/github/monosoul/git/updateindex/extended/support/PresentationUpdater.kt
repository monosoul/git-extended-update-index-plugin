package com.github.monosoul.git.updateindex.extended.support

import com.intellij.openapi.actionSystem.Presentation

interface PresentationUpdater : (Presentation) -> Unit {
    override fun invoke(presentation: Presentation)
}