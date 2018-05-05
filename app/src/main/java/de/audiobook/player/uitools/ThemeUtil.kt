package de.audiobook.player.uitools

import android.content.Context
import android.support.annotation.AnyRes
import android.support.annotation.AttrRes
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatDelegate
import android.view.View
import de.audiobook.player.R

var View.visible: Boolean
  get() = visibility == View.VISIBLE
  set(value) {
    visibility = if (value) View.VISIBLE else View.GONE
  }

fun View.setVisibleWeak() {
  visibility = View.INVISIBLE
}

object ThemeUtil {

  @AnyRes
  fun getResourceId(c: Context, @AttrRes attr: Int): Int {
    val ta = c.obtainStyledAttributes(intArrayOf(attr))
    val resId = ta.getResourceId(0, -1)
    ta.recycle()
    require(resId != -1) { "Resource with attr=$attr not found" }
    return resId
  }

  enum class Theme(@StringRes val nameId: Int, @AppCompatDelegate.NightMode val nightMode: Int) {
    DAY_NIGHT(R.string.pref_theme_daynight, AppCompatDelegate.MODE_NIGHT_NO),
    DAY(R.string.pref_theme_day, AppCompatDelegate.MODE_NIGHT_NO),
    NIGHT(R.string.pref_theme_night, AppCompatDelegate.MODE_NIGHT_YES)
  }
}
