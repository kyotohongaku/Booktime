package de.audiobook.player.features.crashlytics

import timber.log.Timber

/**
 * Timber tree adds logs to crashes
 */
class CrashLoggingTree : Timber.Tree() {

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    //CrashlyticsProxy.log(message)
  }
}
