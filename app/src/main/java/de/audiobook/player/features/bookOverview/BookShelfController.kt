package de.audiobook.player.features.bookOverview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.annotation.DrawableRes
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.MenuItem
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import de.audiobook.player.R
import de.audiobook.player.data.Book
import de.audiobook.player.databinding.BookShelfBinding
import de.audiobook.player.features.bookPlaying.BookPlayController
import de.audiobook.player.features.folderOverview.FolderOverviewController
import de.audiobook.player.features.imagepicker.ImagePickerController
import de.audiobook.player.features.settings.SettingsController
import de.audiobook.player.injection.App
import de.audiobook.player.injection.PrefKeys
import de.audiobook.player.misc.color
import de.audiobook.player.misc.conductor.asTransaction
import de.audiobook.player.misc.conductor.clearAfterDestroyView
import de.audiobook.player.misc.conductor.clearAfterDestroyViewNullable
import de.audiobook.player.misc.dpToPxRounded
import de.audiobook.player.misc.postedIfComputingLayout
import de.audiobook.player.misc.supportTransitionName
import de.audiobook.player.mvp.MvpController
import de.audiobook.player.persistence.pref.Pref
import de.audiobook.player.uitools.BookChangeHandler
import de.audiobook.player.uitools.PlayPauseDrawable
import de.audiobook.player.uitools.VerticalDividerItemDecoration
import de.audiobook.player.uitools.visible
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

private const val COVER_FROM_GALLERY = 1

/**
 * Showing the shelf of all the available books and provide a navigation to each book.
 */
class BookShelfController : MvpController<BookShelfView, BookShelfPresenter, BookShelfBinding>(), EditCoverDialogFragment.Callback, EditBookBottomSheet.Callback, BookShelfView {

  override fun createPresenter() = App.component.bookShelfPresenter
  override val layoutRes = R.layout.book_shelf

  override fun provideView() = this

  init {
    App.component.inject(this)
  }

  @field:[Inject Named(PrefKeys.CURRENT_BOOK)]
  lateinit var currentBookIdPref: Pref<Long>
  @field:[Inject Named(PrefKeys.DISPLAY_MODE)]
  lateinit var displayModePref: Pref<DisplayMode>

  private var playPauseDrawable: PlayPauseDrawable by clearAfterDestroyView()
  private var adapter: BookShelfAdapter by clearAfterDestroyView()
  private var listDecoration: RecyclerView.ItemDecoration by clearAfterDestroyView()
  private var gridLayoutManager: GridLayoutManager by clearAfterDestroyView()
  private var linearLayoutManager: RecyclerView.LayoutManager by clearAfterDestroyView()
  private var currentTapTarget by clearAfterDestroyViewNullable<TapTargetView>()
  private var menuBook: Book? = null
  private var pendingTransaction: FragmentTransaction? = null
  private var currentBook: Book? = null

  private var currentPlaying: MenuItem by clearAfterDestroyView()
  private var displayModeItem: MenuItem by clearAfterDestroyView()

  override fun onBindingCreated(binding: BookShelfBinding) {
    playPauseDrawable = PlayPauseDrawable()
    setupToolbar()
    setupFab()
    setupRecyclerView()
  }

  private fun setupFab() {
    binding.fab.setIconDrawable(playPauseDrawable)
    binding.fab.setOnClickListener { presenter.playPauseRequested() }
  }

  private fun setupRecyclerView() {
    binding.recyclerView.setHasFixedSize(true)
    adapter = BookShelfAdapter(activity) { book, clickType ->
      if (clickType == BookShelfAdapter.ClickType.REGULAR) {
        invokeBookSelectionCallback(book.id)
      } else {
        EditBookBottomSheet.newInstance(this, book).show(fragmentManager, "editBottomSheet")
      }
    }
    binding.recyclerView.adapter = adapter
    // without this the item would blink on every change
    val anim = binding.recyclerView.itemAnimator as SimpleItemAnimator
    anim.supportsChangeAnimations = false
    listDecoration = VerticalDividerItemDecoration(activity, activity.dpToPxRounded(72F))
    gridLayoutManager = GridLayoutManager(activity, amountOfColumns())
    linearLayoutManager = LinearLayoutManager(activity)

    updateDisplayMode()
  }

  private fun setupToolbar() {
    binding.toolbar.inflateMenu(R.menu.book_shelf)
    val menu = binding.toolbar.menu
    currentPlaying = menu.findItem(R.id.action_current)
    displayModeItem = menu.findItem(R.id.action_change_layout)
    binding.toolbar.title = getString(R.string.app_name)
    binding.toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.action_settings -> {
          val transaction = SettingsController().asTransaction()
          router.pushController(transaction)
          true
        }
        R.id.action_current -> {
          invokeBookSelectionCallback(currentBookIdPref.value)
          true
        }
        R.id.action_change_layout -> {
          displayModePref.value = !displayModePref.value
          updateDisplayMode()
          true
        }
        R.id.library -> {
          toFolderOverview()
          true
        }
        else -> false
      }
    }
  }

  private fun toFolderOverview() {
    val controller = FolderOverviewController()
    router.pushController(controller.asTransaction())
  }

  override fun onActivityResumed(activity: Activity) {
    super.onActivityResumed(activity)

    pendingTransaction?.commit()
    pendingTransaction = null
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      COVER_FROM_GALLERY -> {
        if (resultCode == Activity.RESULT_OK) {
          val imageUri = data?.data
          val book = menuBook
          if (imageUri == null || book == null) {
            return
          }

          @SuppressLint("CommitTransaction")
          pendingTransaction = fragmentManager.beginTransaction()
              .add(
                  EditCoverDialogFragment.newInstance(this, book, imageUri),
                  EditCoverDialogFragment.TAG
              )
        }
      }
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  // Returns the amount of columns the main-grid will need
  private fun amountOfColumns(): Int {
    val r = binding.recyclerView.resources
    val displayMetrics = r.displayMetrics
    val widthPx = displayMetrics.widthPixels.toFloat()
    val desiredPx = r.getDimensionPixelSize(R.dimen.desired_medium_cover).toFloat()
    val columns = Math.round(widthPx / desiredPx)
    return Math.max(columns, 3)
  }

  private fun updateDisplayMode() {
    val defaultDisplayMode = displayModePref.value
    val margin: Int
    if (defaultDisplayMode == DisplayMode.GRID) {
      binding.recyclerView.removeItemDecoration(listDecoration)
      binding.recyclerView.layoutManager = gridLayoutManager
      margin = activity.dpToPxRounded(8F)
    } else {
      binding.recyclerView.addItemDecoration(listDecoration, 0)
      binding.recyclerView.layoutManager = linearLayoutManager
      margin = 0
    }
    val layoutParams = binding.recyclerView.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.leftMargin = margin
    layoutParams.rightMargin = margin
    adapter.displayMode = defaultDisplayMode

    displayModeItem.setIcon((!displayModePref.value).icon)
  }

  private fun invokeBookSelectionCallback(bookId: Long) {
    currentBookIdPref.value = bookId

    val viewHolder = binding.recyclerView.findViewHolderForItemId(bookId) as BookShelfAdapter.BaseViewHolder?
    val transaction = RouterTransaction.with(BookPlayController(bookId))
    val transition = BookChangeHandler()
    if (viewHolder != null) {
      val transitionName = viewHolder.coverView.supportTransitionName
      transition.transitionName = transitionName
    }
    transaction.pushChangeHandler(transition)
        .popChangeHandler(transition)
    router.pushController(transaction)
  }

  /** Display a new set of books */
  override fun displayNewBooks(books: List<Book>) {
    Timber.i("${books.size} displayNewBooks")
    adapter.newDataSet(books)
  }

  /** The book marked as current was changed. Updates the adapter and fab accordingly. */
  override fun updateCurrentBook(currentBook: Book?) {
    Timber.i("updateCurrentBook: ${currentBook?.name}")
    this.currentBook = currentBook

    for (i in 0 until adapter.itemCount) {
      val itemId = adapter.getItemId(i)
      val vh = binding.recyclerView.findViewHolderForItemId(itemId) as BookShelfAdapter.BaseViewHolder?
      if (itemId == currentBook?.id || (vh != null && vh.indicatorVisible)) {
        adapter.notifyItemChanged(i)
      }
    }

    binding.fab.visible = currentBook != null
    currentPlaying.isVisible = currentBook != null
  }

  /** Sets the fab icon correctly accordingly to the new play state. */
  override fun showPlaying(playing: Boolean) {
    Timber.i("Called showPlaying $playing")
    val laidOut = ViewCompat.isLaidOut(binding.fab)
    if (playing) {
      playPauseDrawable.transformToPause(laidOut)
    } else {
      playPauseDrawable.transformToPlay(laidOut)
    }
  }

  /** Show a warning that no audiobook folder was chosen */
  override fun showNoFolderWarning() {
    if (currentTapTarget?.isVisible == true)
      return

    val target = TapTarget.forToolbarMenuItem(
        binding.toolbar, R.id.library, getString(R.string.onboarding_title), getString(R.string.onboarding_content))
        .cancelable(false)
        .tintTarget(false)
        .outerCircleColor(R.color.welcomeColor)
        .descriptionTextColorInt(Color.WHITE)
        .textColorInt(Color.WHITE)
        .targetCircleColorInt(Color.WHITE)
        .transparentTarget(true)
    currentTapTarget = TapTargetView.showFor(activity, target, object : TapTargetView.Listener() {
      override fun onTargetClick(view: TapTargetView?) {
        super.onTargetClick(view)
        toFolderOverview()
      }
    })
  }

  override fun showLoading(loading: Boolean) {
    binding.loadingProgress.visible = loading
  }

  override fun bookCoverChanged(bookId: Long) {
    // there is an issue where notifyDataSetChanges throws:
    // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling
    binding.recyclerView.postedIfComputingLayout {
      adapter.reloadBookCover(bookId)
    }
  }

  override fun onBookCoverChanged(book: Book) {
    binding.recyclerView.postedIfComputingLayout {
      adapter.reloadBookCover(book.id)
    }
  }

  override fun onInternetCoverRequested(book: Book) {
    router.pushController(ImagePickerController(book).asTransaction())
  }

  override fun onFileCoverRequested(book: Book) {
    menuBook = book
    val galleryPickerIntent = Intent(Intent.ACTION_PICK)
    galleryPickerIntent.type = "image/*"
    startActivityForResult(galleryPickerIntent, COVER_FROM_GALLERY)
  }

  enum class DisplayMode(@DrawableRes val icon: Int) {
    GRID(R.drawable.view_grid),
    LIST(R.drawable.ic_view_list);

    operator fun not(): DisplayMode = if (this == GRID) LIST else GRID
  }

  override fun onDestroyBinding(binding: BookShelfBinding) {
    super.onDestroyBinding(binding)
    binding.recyclerView.adapter = null
    //   currentTapTarget?.dismiss(false)
  }
}
