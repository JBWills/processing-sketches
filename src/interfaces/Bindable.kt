package interfaces

import BaseSketch
import controls.ControlSectionable

interface Bindable {

  /**
   * Just a helper so this can be called from outside of Bindable
   */
  fun bindSketch(s: BaseSketch): ControlSectionable = s.bind()
  fun BaseSketch.bind(): ControlSectionable
}
