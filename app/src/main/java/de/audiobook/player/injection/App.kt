package de.audiobook.player.injection

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import de.audiobook.player.BuildConfig
import de.audiobook.player.features.BookAdder
import de.audiobook.player.features.crashlytics.CrashLoggingTree
//import de.audiobook.player.features.crashlytics.CrashlyticsProxy
import de.audiobook.player.features.widget.TriggerWidgetOnChange
import de.audiobook.player.misc.StrictModeInit
import de.audiobook.player.persistence.pref.Pref
import de.audiobook.player.playback.AndroidAutoConnectedReceiver
import de.audiobook.player.uitools.ThemeUtil
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.concurrent.thread

class App : Application(), HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector, HasBroadcastReceiverInjector {

  @Inject lateinit var bookAdder: BookAdder
  @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>
  @Inject lateinit var serviceInjector: DispatchingAndroidInjector<Service>
  @Inject lateinit var broadcastInjector: DispatchingAndroidInjector<BroadcastReceiver>
  @Inject lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>
  @Inject lateinit var triggerWidgetOnChange: TriggerWidgetOnChange
  @Inject lateinit var autoConnectedReceiver: AndroidAutoConnectedReceiver
  @field:[Inject Named(PrefKeys.THEME)]
  lateinit var themePref: Pref<ThemeUtil.Theme>

  override fun activityInjector() = activityInjector
  override fun serviceInjector() = serviceInjector
  override fun supportFragmentInjector() = supportFragmentInjector
  override fun broadcastReceiverInjector() = broadcastInjector

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) StrictModeInit.init()

   // CrashlyticsProxy.init(this)
    thread {
      if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
      else Timber.plant(CrashLoggingTree())
    }

    component = DaggerAppComponent.builder()
        .application(this)
        .build()
    component.inject(this)

    bookAdder.scanForFiles()

    AppCompatDelegate.setDefaultNightMode(themePref.value.nightMode)

    autoConnectedReceiver.register(this)

    triggerWidgetOnChange.init()
  }

  companion object {

    lateinit var component: AppComponent
      @VisibleForTesting set
  }
}
