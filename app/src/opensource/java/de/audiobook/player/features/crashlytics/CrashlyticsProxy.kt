package de.audiobook.player.features.crashlytics

import android.app.Application
import de.audiobook.player.misc.ErrorReporter

/**
 * No-Op proxy for crashlytics
 */
@Suppress("UNUSED_PARAMETER")
object CrashlyticsProxy : ErrorReporter {

  override fun log(message: String) {}

  override fun logException(throwable: Throwable) {}

  fun init(app: Application) {}
}
