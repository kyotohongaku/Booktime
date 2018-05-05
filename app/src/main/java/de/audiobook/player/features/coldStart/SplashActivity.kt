package de.audiobook.player.features.coldStart

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.audiobook.player.features.MainActivity

/**
 * Activity that just exists to fake a toolbar through its windowbackground upon start
 */
class SplashActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // app shortcut
    val playCurrentBookImmediately = intent.action == "playCurrent"
    val intent = MainActivity.newIntent(this, playCurrentBookImmediately)
    startActivity(intent)
    finish()
  }
}
