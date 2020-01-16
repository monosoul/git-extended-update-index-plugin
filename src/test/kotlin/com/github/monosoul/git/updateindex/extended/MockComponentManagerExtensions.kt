package com.github.monosoul.git.updateindex.extended

import com.intellij.mock.MockComponentManager
import com.intellij.openapi.Disposable

inline fun <reified T> MockComponentManager.registerService(
        serviceImplementation: T,
        parentDisposable: Disposable
) = registerService(T::class.java, serviceImplementation, parentDisposable)