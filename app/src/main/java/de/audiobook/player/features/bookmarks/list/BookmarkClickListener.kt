package de.audiobook.player.features.bookmarks.list

import de.audiobook.player.data.Bookmark

interface BookmarkClickListener {

  fun onOptionsMenuClicked(bookmark: Bookmark, v: android.view.View)
  fun onBookmarkClicked(bookmark: Bookmark)
}
