package de.audiobook.player.features.bookSearch

import android.annotation.SuppressLint
import android.provider.MediaStore
import com.google.common.truth.Truth.assertThat
import de.audiobook.player.MemoryPref
import de.audiobook.player.common.sparseArray.emptySparseArray
import de.audiobook.player.data.Book
import de.audiobook.player.data.Chapter
import de.audiobook.player.data.repo.BookRepository
import de.audiobook.player.given
import de.audiobook.player.persistence.pref.Pref
import de.audiobook.player.playback.PlayerController
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations
import java.io.File

/**
 * A test case to easily test the voice search functionality for Android auto (and OK google commands)
 */
@SuppressLint("SdCardPath")
class BookSearchHandlerTest {

  lateinit var searchHandler: BookSearchHandler

  @Mock lateinit var repo: BookRepository
  @Mock lateinit var player: PlayerController

  private lateinit var currentBookIdPref: Pref<Long>

  private val anotherBookChapter1 = Chapter(File("/sdcard/AnotherBook/chapter1.mp3"), "anotherBookChapter1", 5000, 0, emptySparseArray())
  private val anotherBookChapter2 = Chapter(File("/sdcard/AnotherBook/chapter2.mp3"), "anotherBookChapter2", 10000, 0, emptySparseArray())
  private val anotherBook = Book(
      id = 2,
      type = Book.Type.SINGLE_FOLDER,
      author = "AnotherBookAuthor",
      currentFile = anotherBookChapter1.file,
      positionInChapter = 3000,
      name = "AnotherBook",
      chapters = listOf(anotherBookChapter1, anotherBookChapter2),
      playbackSpeed = 1F,
      root = "/sdcard/AnotherBook",
      loudnessGain = 0
  )

  private val bookToFindChapter1 = Chapter(File("/sdcard/Book1/chapter1.mp3"), "bookToFindChapter1", 5000, 0, emptySparseArray())
  private val bookToFindChapter2 = Chapter(File("/sdcard/Book1/chapter2.mp3"), "bookToFindChapter2", 10000, 0, emptySparseArray())
  private val bookToFind = Book(
      id = 1,
      type = Book.Type.SINGLE_FOLDER,
      author = "Book1Author",
      currentFile = bookToFindChapter2.file,
      positionInChapter = 3000,
      name = "Book1",
      chapters = listOf(bookToFindChapter1, bookToFindChapter2),
      playbackSpeed = 1F,
      root = "/sdcard/Book1",
      loudnessGain = 0
  )

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    given { repo.activeBooks }.thenReturn(listOf(anotherBook, bookToFind))
    currentBookIdPref = MemoryPref(-1)

    searchHandler = BookSearchHandler(repo, player, currentBookIdPref)
  }

  @Test
  fun unstructuredSearchByBook() {
    val bookSearch = BookSearch(query = bookToFind.name)
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()
    verifyNoMoreInteractions(player)
  }

  @Test
  fun unstructuredSearchByArtist() {
    val bookSearch = BookSearch(query = bookToFind.author)
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()
    verifyNoMoreInteractions(player)
  }

  @Test
  fun unstructuredSearchByChapter() {
    val bookSearch = BookSearch(query = bookToFindChapter1.name)
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()
    verifyNoMoreInteractions(player)
  }

  @Test
  fun mediaFocusAnyNoneFoundButPlayed() {
    val bookSearch = BookSearch(mediaFocus = "vnd.android.cursor.item/*")
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(anotherBook.id)
    verify(player).play()
  }

  @Test
  fun mediaFocusArtist() {
    val bookSearch = BookSearch(
        mediaFocus = MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE,
        artist = bookToFind.author
    )
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()

    verifyNoMoreInteractions(player)
  }

  @Test
  fun mediaFocusArtistInTitleNoArtistInBook() {
    val bookToFind = this.bookToFind.copy(author = null, name = "The book of Tim")
    given { repo.activeBooks }.thenReturn(listOf(bookToFind))

    val bookSearch = BookSearch(
        mediaFocus = MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE,
        query = "Tim",
        artist = "Tim"
    )
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()
    verifyNoMoreInteractions(player)
  }

  @Test
  fun mediaFocusAlbum() {
    val bookSearch = BookSearch(
        mediaFocus = MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE,
        artist = bookToFind.author,
        album = bookToFind.name
    )
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()
    verifyNoMoreInteractions(player)
  }

  @Test
  fun mediaFocusPlaylist() {
    val bookSearch = BookSearch(
        mediaFocus = MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE,
        artist = bookToFind.author,
        playList = bookToFind.name,
        album = bookToFind.name
    )
    searchHandler.handle(bookSearch)

    assertThat(currentBookIdPref.value).isEqualTo(bookToFind.id)
    verify(player).play()
    verifyNoMoreInteractions(player)
  }
}
