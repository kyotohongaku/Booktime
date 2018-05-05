package de.audiobook.player.features.bookPlaying

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import dagger.android.support.AndroidSupportInjection
import de.audiobook.player.R
import de.audiobook.player.databinding.DialogAmountChooserBinding
import de.audiobook.player.injection.PrefKeys
import de.audiobook.player.misc.onProgressChanged
import de.audiobook.player.persistence.pref.Pref
import javax.inject.Inject
import javax.inject.Named

class SeekDialogFragment : DialogFragment() {

  @field:[Inject Named(PrefKeys.SEEK_TIME)]
  lateinit var seekTimePref: Pref<Int>

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    AndroidSupportInjection.inject(this)

    // find views
    val binding = DialogAmountChooserBinding.inflate(activity!!.layoutInflater)

    // init
    val oldSeekTime = seekTimePref.value
    binding.seekBar.max = (MAX - MIN) * FACTOR
    binding.seekBar.onProgressChanged(initialNotification = true) {
      val value = it / FACTOR + MIN
      binding.textView.text = context!!.resources.getQuantityString(R.plurals.seconds, value, value)
    }
    binding.seekBar.progress = (oldSeekTime - MIN) * FACTOR

    return MaterialDialog.Builder(context!!)
        .title(R.string.pref_seek_time)
        .customView(binding.root, true)
        .positiveText(R.string.dialog_confirm)
        .negativeText(R.string.dialog_cancel)
        .onPositive { _, _ ->
          val newSeekTime = binding.seekBar.progress / FACTOR + MIN
          seekTimePref.value = newSeekTime
        }.build()
  }

  companion object {
    val TAG: String = SeekDialogFragment::class.java.simpleName

    private val FACTOR = 10
    private val MIN = 3
    private val MAX = 60
  }
}
