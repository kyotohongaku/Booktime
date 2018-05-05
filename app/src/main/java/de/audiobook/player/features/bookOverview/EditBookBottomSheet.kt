package de.audiobook.player.features.bookOverview

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import dagger.android.support.AndroidSupportInjection
import de.audiobook.player.R
import de.audiobook.player.data.Book
import de.audiobook.player.data.repo.BookRepository
import de.audiobook.player.databinding.BookMoreBottomSheetBinding
import de.audiobook.player.features.bookmarks.BookmarkController
import de.audiobook.player.misc.RouterProvider
import de.audiobook.player.misc.bottomCompoundDrawable
import de.audiobook.player.misc.color
import de.audiobook.player.misc.conductor.asTransaction
import de.audiobook.player.misc.endCompoundDrawable
import de.audiobook.player.misc.findCallback
import de.audiobook.player.misc.startCompoundDrawable
import de.audiobook.player.misc.tinted
import de.audiobook.player.misc.topCompoundDrawable
import timber.log.Timber
import javax.inject.Inject

/**
 * Bottom sheet dialog fragment that will be displayed when a book edit was requested
 */
class EditBookBottomSheet : BottomSheetDialogFragment() {

  @Inject lateinit var repo: BookRepository

  private fun callback() = findCallback<Callback>(NI_TARGET)

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    AndroidSupportInjection.inject(this)

    val dialog = BottomSheetDialog(context!!, R.style.BottomSheetStyle)

    // if there is no book, skip here
    val book = repo.bookById(bookId())
    if (book == null) {
      Timber.e("book is null. Return early")
      return dialog
    }

    val binding = BookMoreBottomSheetBinding.inflate(activity!!.layoutInflater)
    dialog.setContentView(binding.root)

    binding.title.setOnClickListener {
      EditBookTitleDialogFragment.newInstance(book)
          .show(fragmentManager, EditBookTitleDialogFragment.TAG)
      dismiss()
    }
    binding.internetCover.setOnClickListener {
      callback().onInternetCoverRequested(book)
      dismiss()
    }
    binding.fileCover.setOnClickListener {
      callback().onFileCoverRequested(book)
      dismiss()
    }
    binding.bookmark.setOnClickListener {
      val router = (activity as RouterProvider).provideRouter()
      val controller = BookmarkController.newInstance(book.id)
      router.pushController(controller.asTransaction())

      dismiss()
    }

    tintLeftDrawable(binding.title)
    tintLeftDrawable(binding.internetCover)
    tintLeftDrawable(binding.fileCover)
    tintLeftDrawable(binding.bookmark)

    return dialog
  }

  private fun tintLeftDrawable(textView: TextView) {
    val left = textView.startCompoundDrawable()!!
    val tinted = left.tinted(context!!.color(R.color.icon_color))
    textView.setCompoundDrawablesRelative(
        tinted,
        textView.topCompoundDrawable(),
        textView.endCompoundDrawable(),
        textView.bottomCompoundDrawable()
    )
  }

  private fun bookId() = arguments!!.getLong(NI_BOOK)

  companion object {
    private const val NI_BOOK = "ni#book"
    private const val NI_TARGET = "ni#target"
    fun <T> newInstance(target: T, book: Book) where T : Controller, T : Callback = EditBookBottomSheet().apply {
      arguments = Bundle().apply {
        putLong(NI_BOOK, book.id)
        putString(NI_TARGET, target.instanceId)
      }
    }
  }

  interface Callback {
    fun onInternetCoverRequested(book: Book)
    fun onFileCoverRequested(book: Book)
  }
}
