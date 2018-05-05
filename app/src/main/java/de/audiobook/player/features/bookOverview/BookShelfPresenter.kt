package de.audiobook.player.features.bookOverview

import de.audiobook.player.data.repo.BookRepository
import de.audiobook.player.features.BookAdder
import de.audiobook.player.injection.PrefKeys
import de.audiobook.player.misc.Observables
import de.audiobook.player.mvp.Presenter
import de.audiobook.player.persistence.pref.Pref
import de.audiobook.player.playback.PlayStateManager
import de.audiobook.player.playback.PlayStateManager.PlayState
import de.audiobook.player.playback.PlayerController
import de.audiobook.player.uitools.CoverFromDiscCollector
import javax.inject.Inject
import javax.inject.Named

class BookShelfPresenter
@Inject
constructor(
    private val repo: BookRepository,
    private val bookAdder: BookAdder,
    private val playStateManager: PlayStateManager,
    private val playerController: PlayerController,
    private val coverFromDiscCollector: CoverFromDiscCollector,
    @Named(PrefKeys.CURRENT_BOOK)
    private val currentBookIdPref: Pref<Long>
) : Presenter<BookShelfView>() {

  override fun onAttach(view: BookShelfView) {
    bookAdder.scanForFiles()
    setupBookStream()
    setupCurrentBookStream()
    setupLoadingState()
    setupPlayState()
    setupCoverChanged()
    handleFolderWarning()
  }

  private fun setupCurrentBookStream() {
    currentBookIdPref.stream
        .subscribe {
          val book = repo.bookById(it)
          view.updateCurrentBook(book)
        }
        .disposeOnDetach()
  }

  private fun setupBookStream() {
    repo.booksStream()
        .subscribe { view.displayNewBooks(it) }
        .disposeOnDetach()
  }

  private fun setupLoadingState() {
    val noBooks = repo.booksStream().map { it.isEmpty() }
    val showLoading = Observables.combineLatest(bookAdder.scannerActive, noBooks) { active, booksEmpty ->
      if (booksEmpty) active else false
    }
    showLoading.subscribe { view.showLoading(it) }
        .disposeOnDetach()
  }

  private fun setupCoverChanged() {
    coverFromDiscCollector.coverChanged()
        .subscribe { view.bookCoverChanged(it) }
        .disposeOnDetach()
  }

  private fun setupPlayState() {
    playStateManager.playStateStream()
        .map { it == PlayState.PLAYING }
        .distinctUntilChanged()
        .subscribe { view.showPlaying(it) }
        .disposeOnDetach()
  }

  private fun handleFolderWarning() {
    val showFolderWarning = Observables.combineLatest(bookAdder.scannerActive, repo.booksStream()) { scannerActive, books ->
      books.isEmpty() && !scannerActive
    }.filter { it }
        .take(1)
    showFolderWarning
        .subscribe { _ -> view.showNoFolderWarning() }
        .disposeOnDetach()
  }

  fun playPauseRequested() = playerController.playPause()
}
