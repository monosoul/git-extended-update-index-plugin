package com.github.monosoul.git.updateindex.extended

import com.intellij.mock.MockComponentManager
import com.intellij.openapi.Disposable
import java.util.stream.Stream

internal const val LIMIT = 10

internal inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()

internal fun <T> Stream<T>.limit(maxSize: Int) = limit(maxSize.toLong())

//internal inline fun <reified T> mockedAppender() = mockk<Appender>(relaxed = true).also {
//    Logger.getInstance(T::class.java).isDebugEnabled
//    T::class.java.let(Logger::getLogger).apply {
//        addAppender(it)
//        level = DEBUG
//    }
//}

internal inline fun <reified T : Any> MockComponentManager.registerService(
        serviceImplementation: T,
        parentDisposable: Disposable
) = registerService(T::class.java, serviceImplementation, parentDisposable)
