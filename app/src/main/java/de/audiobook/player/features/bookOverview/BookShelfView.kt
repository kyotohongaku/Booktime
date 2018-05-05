package de.audiobook.player.features.bookOverview

import de.audiobook.player.data.Book

interface BookShelfView {
  /** Display a new set of books */
  fun displayNewBooks(books: List<Book>)

  /** The book marked as current was changed. Updates the adapter and fab accordingly. */
  fun updateCurrentBook(currentBook: Book?)

  /** Sets the fab icon correctly accordingly to the new play state. */
  fun showPlaying(playing: Boolean)

  /** Show a warning that no audiobook folder was chosen */
  fun showNoFolderWarning()

  fun showLoading(loading: Boolean)
  fun bookCoverChanged(bookId: Long)
}
