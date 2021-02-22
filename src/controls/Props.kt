package controls

import util.mapArray

/**
 * Basically a wrapper to get values and controls from an object.
 */
interface PropFields<T> {
  fun toControls(): List<ControlGroupable>
  fun toValues(): T
}

/**
 * Props for a sketch.
 */
abstract class Props<TabValues, GlobalValues>(maxLayers: Int) {
  protected abstract fun globalControls(): PropFields<GlobalValues>
  protected abstract fun tabControls(tabIndex: Int): PropFields<TabValues>

  val global: PropFields<GlobalValues> by lazy { globalControls() }
  val tabs: List<PropFields<TabValues>> by lazy {
    (0 until maxLayers).map { tabControls(it) }
  }

  val globalValues: GlobalValues get() = global.toValues()
  val tabValues: List<TabValues> get() = tabs.map { it.toValues() }

  val globalControls: Array<ControlGroupable> get() = global.toControls().toTypedArray()
  val tabControls: Array<Array<ControlGroupable>>
    get() = tabs.mapArray {
      it.toControls().toTypedArray()
    }
}
