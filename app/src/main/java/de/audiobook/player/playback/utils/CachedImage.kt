package de.audiobook.player.playback.utils

import android.graphics.Bitmap
import de.audiobook.player.data.Book

/**
 * A cache entry for a bitmap
 */
data class CachedImage(val bookId: Long, val cover: Bitmap) {

  fun matches(book: Book) = book.id == bookId
}
