package de.audiobook.player.misc

interface ErrorReporter {

  fun log(message: String)
  fun logException(throwable: Throwable)
}
