package de.audiobook.player.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.audiobook.player.features.BaseActivity
import de.audiobook.player.features.MainActivity
import de.audiobook.player.features.bookOverview.EditBookBottomSheet
import de.audiobook.player.features.bookOverview.EditBookTitleDialogFragment
import de.audiobook.player.features.bookOverview.EditCoverDialogFragment
import de.audiobook.player.features.bookPlaying.JumpToPositionDialogFragment
import de.audiobook.player.features.bookPlaying.SeekDialogFragment
import de.audiobook.player.features.bookPlaying.SleepTimerDialogFragment
import de.audiobook.player.features.folderChooser.FolderChooserActivity
import de.audiobook.player.features.settings.dialogs.AutoRewindDialogFragment
import de.audiobook.player.features.settings.dialogs.PlaybackSpeedDialogFragment
import de.audiobook.player.features.settings.dialogs.ThemePickerDialogFragment
import de.audiobook.player.features.widget.BaseWidgetProvider
import de.audiobook.player.playback.PlaybackService

/**
 * Module for dagger bindings
 */
@Module
abstract class BindingModule {

  @ContributesAndroidInjector
  abstract fun mainActivity(): MainActivity

  @ContributesAndroidInjector(modules = arrayOf(PlaybackModule::class))
  @PerService
  abstract fun playbackService(): PlaybackService

  @ContributesAndroidInjector
  abstract fun autoRewindDialogFragment(): AutoRewindDialogFragment

  @ContributesAndroidInjector
  abstract fun editCoverDialogFragment(): EditCoverDialogFragment

  @ContributesAndroidInjector
  abstract fun editBookTitleDialogFragment(): EditBookTitleDialogFragment

  @ContributesAndroidInjector
  abstract fun folderChooserActivity(): FolderChooserActivity

  @ContributesAndroidInjector
  abstract fun jumpToPositionDialogFragment(): JumpToPositionDialogFragment

  @ContributesAndroidInjector
  abstract fun playbackSpeedDialogFragment(): PlaybackSpeedDialogFragment

  @ContributesAndroidInjector
  abstract fun seekDialogFragment(): SeekDialogFragment

  @ContributesAndroidInjector
  abstract fun sleepTimerDialogFragment(): SleepTimerDialogFragment

  @ContributesAndroidInjector
  abstract fun themePickerDialogFragment(): ThemePickerDialogFragment

  @ContributesAndroidInjector
  abstract fun baseWidgetProvider(): BaseWidgetProvider

  @ContributesAndroidInjector
  abstract fun editBookBottomSheet(): EditBookBottomSheet

  @ContributesAndroidInjector
  abstract fun baseActivity(): BaseActivity
}
