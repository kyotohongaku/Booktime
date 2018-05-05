package de.audiobook.player.playback.utils.audioFocus

interface AudioFocusRequester {

  fun request(): Int
  fun abandon()
}
