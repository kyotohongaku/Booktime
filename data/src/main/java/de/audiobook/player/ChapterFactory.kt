package de.audiobook.player

import android.support.v4.util.SparseArrayCompat
import de.audiobook.player.common.sparseArray.emptySparseArray
import de.audiobook.player.data.Chapter
import java.io.File

object ChapterFactory {

  fun create(file: String = "First.mp3",
             parent: String = "/root/",
             duration: Int = 100,
             lastModified: Long = 12345L,
             marks: SparseArrayCompat<String> = emptySparseArray()
  ) = Chapter(file = File(parent, file), name = file, duration = duration, fileLastModified = lastModified, marks = marks)
}
