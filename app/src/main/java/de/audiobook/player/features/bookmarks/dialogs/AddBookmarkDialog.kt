package de.audiobook.player.features.bookmarks.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import de.audiobook.player.R
import de.audiobook.player.misc.DialogController

/**
 * Dialog for chosing a title for a new bookmark.
 */
class AddBookmarkDialog : DialogController() {

  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    val inputType = InputType.TYPE_CLASS_TEXT or
        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
        InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
    val dialog = MaterialDialog.Builder(activity!!)
        .title(R.string.bookmark)
        .inputType(inputType)
        .input(activity!!.getString(R.string.bookmark_edit_hint), null, true) { _, charSequence ->
          val title = charSequence.toString()
          val callback = targetController as AddBookmarkDialog.Callback
          callback.onBookmarkNameChosen(title)
        }
        .positiveText(de.audiobook.player.R.string.dialog_confirm)
        .build()
    val editText = dialog.inputEditText!!
    editText.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        val title = editText.text.toString()
        val callback = targetController as AddBookmarkDialog.Callback
        callback.onBookmarkNameChosen(title)
        dismissDialog()
        true
      } else false
    }
    return dialog
  }

  interface Callback {
    fun onBookmarkNameChosen(name: String)
  }

  companion object {
    operator fun <T> invoke(target: T) where T : Controller, T : AddBookmarkDialog.Callback = AddBookmarkDialog().apply {
      targetController = target
    }
  }
}
