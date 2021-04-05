package com.github.monosoul.git.updateindex.extended

import com.intellij.mock.MockComponentManager
import com.intellij.openapi.Disposable
import io.mockk.mockk
import org.apache.log4j.Appender
import org.apache.log4j.Level.DEBUG
import org.apache.log4j.Logger
import java.util.stream.Stream

internal const val LIMIT = 10

internal inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()

internal fun <T> Stream<T>.limit(maxSize: Int) = limit(maxSize.toLong())

internal inline fun <reified T> mockedAppender() = mockk<Appender>(relaxed = true).also {
    T::class.java.let(Logger::getLogger).apply {
        addAppender(it)
        level = DEBUG
    }
}

internal inline fun <reified T : Any> MockComponentManager.registerService(
        serviceImplementation: T,
        parentDisposable: Disposable
) = registerService(T::class.java, serviceImplementation, parentDisposable)