package app.mushaf.core.common

import javax.inject.Qualifier

/** Explicit dispatcher qualifiers — never inject `Dispatchers.IO` directly. */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher
