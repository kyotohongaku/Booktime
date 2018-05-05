package de.audiobook.player.features.audio

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.afollestad.materialdialogs.MaterialDialog
import de.audiobook.player.R
import de.audiobook.player.data.repo.BookRepository
import de.audiobook.player.databinding.LoudnessBinding
import de.audiobook.player.injection.App
import de.audiobook.player.misc.DialogController
import de.audiobook.player.misc.progressChangedStream
import de.audiobook.player.playback.PlayerController
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Dialog for controlling the loudness.
 */
class LoudnessDialog(args: Bundle) : DialogController(args) {

  @Inject lateinit var repo: BookRepository
  @Inject lateinit var player: PlayerController

  private val dbFormat = DecimalFormat("0.0 dB")

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    App.component.inject(this)

    val binding = LoudnessBinding.inflate(LayoutInflater.from(activity))

    val bookId = args.getLong(NI_BOOK_ID)
    val book = repo.bookById(bookId)
        ?: return MaterialDialog.Builder(activity!!).build()

    binding.seekBar.max = LoudnessGain.MAX_MB
    binding.seekBar.progress = book.loudnessGain
    binding.seekBar.progressChangedStream()
        .throttleLast(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .subscribe {
          player.setLoudnessGain(it)
          binding.currentValue.text = format(it)
        }

    binding.currentValue.text = format(book.loudnessGain)
    binding.maxValue.text = format(binding.seekBar.max)

    return MaterialDialog.Builder(activity!!)
        .title(R.string.volume_boost)
        .customView(binding.root, true)
        .build()
  }

  private fun format(milliDb: Int) = dbFormat.format(milliDb / 100.0)

  companion object {
    private const val NI_BOOK_ID = "ni#bookId"
    operator fun invoke(bookId: Long) = LoudnessDialog(
        Bundle().apply {
          putLong(NI_BOOK_ID, bookId)
        }
    )
  }
}
