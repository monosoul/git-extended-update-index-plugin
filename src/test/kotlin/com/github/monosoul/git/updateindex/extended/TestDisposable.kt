package com.github.monosoul.git.updateindex.extended

import com.intellij.openapi.Disposable

internal class TestDisposable(@Volatile private var myDisposed: Boolean = false) : Disposable {
    override fun dispose() {
        myDisposed = true
    }
}