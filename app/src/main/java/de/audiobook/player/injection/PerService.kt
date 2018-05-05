package de.audiobook.player.injection

import javax.inject.Scope

/**
 * Custom scope for a service lifetime
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerService
