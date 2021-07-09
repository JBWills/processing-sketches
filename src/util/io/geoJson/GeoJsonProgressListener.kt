package util.io.geoJson

import org.opengis.util.InternationalString
import org.opengis.util.ProgressListener

class GeoJsonProgressListener(
  val onStarted: () -> Unit = {},
  val onProgressChanged: (Float) -> Unit = {},
  val onComplete: () -> Unit = {},
  val onDispose: () -> Unit = {},
  val onCanceled: () -> Unit = {},
) : ProgressListener {
  private var task: InternationalString? = null
  private var progress: Float = 0f
  private var canceled: Boolean = false

  override fun getTask(): InternationalString? = task

  override fun setTask(task: InternationalString?) {
    this.task = task
  }

  override fun started() = onStarted()

  override fun progress(percent: Float) {
    progress = percent
    onProgressChanged(percent)
  }

  override fun getProgress(): Float = progress

  override fun complete() = onComplete()

  override fun dispose() = onDispose()

  override fun isCanceled(): Boolean = canceled

  override fun setCanceled(cancel: Boolean) {
    canceled = cancel

    if (cancel) onCanceled()
  }

  override fun warningOccurred(source: String?, location: String?, warning: String?) {
    println("Warning: source: $source, location: $location, warning: $warning")
  }

  override fun exceptionOccurred(exception: Throwable?) {
    if (exception == null) {
      println("GeoJson says an exception occurred but it returned null  exception object. Nothing we can do.")
      return
    }

    throw exception
  }
}
