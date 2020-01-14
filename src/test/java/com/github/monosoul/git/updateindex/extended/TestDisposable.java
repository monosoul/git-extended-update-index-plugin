package com.github.monosoul.git.updateindex.extended;

import com.intellij.openapi.Disposable;

public final class TestDisposable implements Disposable {

    private volatile boolean myDisposed = false;

    @Override
    public void dispose() {
        myDisposed = true;
    }

    public boolean isDisposed() {
        return myDisposed;
    }
}
