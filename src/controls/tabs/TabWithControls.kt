package controls.tabs

import controls.ControlGroupable
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

/**
 * Abstract class to make controls simpler.
 *
 * Provides getControls() method that takes all of the ControlFields on the object
 * and returns them in order, no need to
 */
abstract class TabWithControls {
  inline fun <reified T : TabWithControls> getControls(): Array<ControlGroupable> =
    T::class.memberProperties
      .mapNotNull {
        when {
          KVisibility.PUBLIC != it.visibility -> null
          it.get(this as T) is ControlGroupable -> it.get(this) as ControlGroupable
          else -> null
        }
      }
      .toTypedArray()
}
