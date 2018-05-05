package de.audiobook.player.features.bookmarks

import de.audiobook.player.data.Bookmark
import de.audiobook.player.data.Chapter

/**
 * View of the bookmarks
 */
interface BookmarkView {

  fun render(bookmarks: List<Bookmark>, chapters: List<Chapter>)
  fun showBookmarkAdded(bookmark: Bookmark)
  fun finish()
}
