package de.audiobook.player.data.repo

import android.support.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import de.audiobook.player.BookFactory
import de.audiobook.player.data.repo.internals.BookStorage
import de.audiobook.player.data.repo.internals.InternalDb
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BookRepositoryTest {

  private lateinit var repo: BookRepository

  @Rule
  @JvmField
  val clearAppDbRule = ClearDbRule()

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getTargetContext()
    val internalDb = InternalDb(context)
    val moshi = Moshi.Builder().build()
    val internalBookRegister = BookStorage(internalDb, moshi)
    repo = BookRepository(internalBookRegister)
  }

  @Test
  fun inOut() {
    val dummy = BookFactory.create()
    repo.addBook(dummy)
    val firstBook = repo.activeBooks.first()
    val dummyWithUpdatedId = dummy.copy(id = firstBook.id)

    assertThat(dummyWithUpdatedId).isEqualTo(firstBook)
  }
}
