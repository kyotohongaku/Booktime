package de.audiobook.player.mvp

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import de.audiobook.player.features.BaseController
import de.audiobook.player.misc.checkMainThread

/**
 * Base fragment that provides a convenient way for binding a view to a presenter
 */
abstract class MvpController<V : Any, out P, B>(
    args: Bundle = Bundle()
) : BaseController<B>(args) where P : Presenter<V>, B : ViewDataBinding {

  private var internalPresenter: P? = null
  val presenter: P
    get() {
      checkMainThread()
      check(!isDestroyed) { "Must not call presenter when destroyed!" }
      if (internalPresenter == null) {
        internalPresenter = createPresenter()
      }
      return internalPresenter!!
    }

  init {
    addLifecycleListener(
        object : LifecycleListener() {

          override fun onRestoreInstanceState(controller: Controller, savedInstanceState: Bundle) {
            presenter.onRestore(savedInstanceState)
          }

          override fun postAttach(controller: Controller, view: View) {
            presenter.attach(provideView())
          }

          override fun preDetach(controller: Controller, view: View) {
            presenter.detach()
          }

          override fun onSaveInstanceState(controller: Controller, outState: Bundle) {
            presenter.onSave(outState)
          }

          override fun postDestroy(controller: Controller) {
            internalPresenter = null
          }
        }
    )
  }

  @Suppress("UNCHECKED_CAST")
  open fun provideView(): V = this as V

  abstract fun createPresenter(): P
}
