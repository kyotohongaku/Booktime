package de.audiobook.player.injection

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import de.audiobook.player.features.audio.LoudnessDialog
import de.audiobook.player.features.bookOverview.BookShelfAdapter
import de.audiobook.player.features.bookOverview.BookShelfController
import de.audiobook.player.features.bookOverview.BookShelfPresenter
import de.audiobook.player.features.bookPlaying.BookPlayController
import de.audiobook.player.features.bookPlaying.BookPlayPresenter
import de.audiobook.player.features.bookmarks.BookmarkPresenter
import de.audiobook.player.features.folderChooser.FolderChooserPresenter
import de.audiobook.player.features.folderOverview.FolderOverviewPresenter
import de.audiobook.player.features.imagepicker.ImagePickerController
import de.audiobook.player.features.settings.SettingsController
import de.audiobook.player.playback.MediaPlayer
import de.audiobook.player.playback.PlayStateManager
import javax.inject.Singleton

/**
 * Base component that is the entry point for injection.
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class, PrefsModule::class, BindingModule::class, AndroidSupportInjectionModule::class))
interface AppComponent {

  val bookmarkPresenter: BookmarkPresenter
  val bookShelfPresenter: BookShelfPresenter
  val context: Context
  val player: MediaPlayer
  val playStateManager: PlayStateManager

  @Component.Builder
  interface Builder {

    @BindsInstance
    fun application(application: Application): Builder

    fun build(): AppComponent
  }

  fun inject(target: App)
  fun inject(target: BookPlayController)
  fun inject(target: BookPlayPresenter)
  fun inject(target: BookShelfAdapter)
  fun inject(target: BookShelfController)
  fun inject(target: FolderChooserPresenter)
  fun inject(target: FolderOverviewPresenter)
  fun inject(target: ImagePickerController)
  fun inject(target: LoudnessDialog)
  fun inject(target: SettingsController)
}
