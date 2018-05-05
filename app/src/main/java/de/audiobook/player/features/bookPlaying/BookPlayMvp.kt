package de.audiobook.player.features.bookPlaying

import de.audiobook.player.data.Book
import java.io.File

/**
 * MVP for the book detail screen
 */
interface BookPlayMvp {

  interface View {
    fun render(book: Book)
    fun finish()
    fun showPlaying(playing: Boolean)
    fun showLeftSleepTime(ms: Int)
    fun openSleepTimeDialog()
    fun showBookmarkAdded()
  }

  abstract class Presenter : de.audiobook.player.mvp.Presenter<View>() {
    abstract fun playPause()
    abstract fun rewind()
    abstract fun fastForward()
    abstract fun next()
    abstract fun previous()
    abstract fun seekTo(position: Int, file: File? = null)
    abstract fun toggleSleepTimer()
    abstract fun addBookmark()
  }
}
