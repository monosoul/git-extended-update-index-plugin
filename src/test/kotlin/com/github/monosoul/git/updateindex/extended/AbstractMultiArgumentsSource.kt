package com.github.monosoul.git.updateindex.extended

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream
import java.util.stream.Stream.generate

abstract class AbstractMultiArgumentsSource(private vararg val generators: () -> Any?) : ArgumentsProvider {
    open val amount = LIMIT

    final override fun provideArguments(context: ExtensionContext?): Stream<Arguments> =
            generate {
                Arguments.of(*generators.map { it() }.toTypedArray())
            }.limit(amount)
}