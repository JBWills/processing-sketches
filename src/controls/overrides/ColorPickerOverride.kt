package controls.overrides

import controlP5.CColor
import controlP5.ColorPicker
import controlP5.ControlFont
import controlP5.ControlListener
import controlP5.ControlP5
import controlP5.ControlWindow
import controlP5.ControllerGroup
import controlP5.ControllerInterface
import controlP5.ControllerProperty
import controlP5.Tab
import processing.core.PApplet
import processing.core.PFont
import processing.core.PGraphics
import processing.event.KeyEvent
import java.awt.Color

typealias ColorListener = (Color) -> Unit

class ColorPickerInterface(val cp: ColorPickerOverride) :
  ControllerInterface<ColorPickerInterface> {
  override fun init() {
    cp.init()
  }

  override fun getWidth(): Int = cp.width


  override fun getHeight(): Int = cp.height

  override fun setValue(p0: Float): ColorPickerInterface {
    cp.value = p0
    return this
  }

  override fun getValue(): Float = cp.value

  override fun setStringValue(p0: String): ColorPickerInterface {
    cp.stringValue = p0
    return this
  }

  override fun getStringValue(): String = cp.stringValue

  override fun getArrayValue(): FloatArray = cp.arrayValue

  override fun getArrayValue(p0: Int): Float = cp.getArrayValue(p0)

  override fun setArrayValue(p0: Int, p1: Float): ColorPickerInterface {
    cp.setArrayValue(p0, p1)
    return this
  }

  override fun setArrayValue(p0: FloatArray?): ColorPickerInterface {
    cp.arrayValue = p0
    return this
  }

  override fun getId(): Int = cp.id

  override fun getPosition(): FloatArray = cp.position

  override fun setPosition(p0: Float, p1: Float): ColorPickerInterface {
    cp.setPosition(p0, p1)
    return this
  }

  override fun setPosition(p0: FloatArray?): ColorPickerInterface {
    cp.position = p0
    return this
  }

  override fun getAbsolutePosition(): FloatArray = cp.absolutePosition

  override fun setAbsolutePosition(p0: FloatArray?): ColorPickerInterface {
    cp.absolutePosition = p0
    return this
  }

  override fun updateAbsolutePosition(): ColorPickerInterface {
    cp.updateAbsolutePosition()
    return this
  }

  override fun getParent(): ControllerInterface<*> = cp.parent

  override fun update(): ColorPickerInterface {
    cp.update()
    return this
  }

  override fun setUpdate(p0: Boolean): ColorPickerInterface {
    cp.isUpdate = p0
    return this
  }

  override fun bringToFront(): ColorPickerInterface {
    cp.bringToFront()
    return this
  }

  override fun bringToFront(p0: ControllerInterface<*>?): ColorPickerInterface {
    cp.bringToFront(p0)
    return this
  }

  override fun isUpdate(): Boolean = cp.isUpdate

  override fun updateEvents(): ColorPickerInterface {
    cp.updateEvents()
    return this
  }

  override fun continuousUpdateEvents() = cp.continuousUpdateEvents()

  override fun updateInternalEvents(p0: PApplet?): ColorPickerInterface {
    cp.updateInternalEvents(p0)
    return this
  }

  override fun draw(p0: PGraphics?) = cp.draw(p0)

  override fun add(p0: ControllerInterface<*>?): ColorPickerInterface {
    cp.add(p0)
    return this
  }

  override fun remove(p0: ControllerInterface<*>?): ColorPickerInterface {
    cp.remove(p0)
    return this
  }

  override fun remove() = cp.remove()

  override fun getName(): String = cp.name

  override fun getAddress(): String = cp.address

  override fun getWindow(): ControlWindow = cp.window

  override fun getTab(): Tab = cp.tab

  override fun setMousePressed(p0: Boolean): Boolean = cp.setMousePressed(p0)

  override fun keyEvent(p0: KeyEvent?) = cp.keyEvent(p0)

  override fun setAddress(p0: String?): ColorPickerInterface {
    cp.address = p0
    return this
  }

  override fun setId(p0: Int): ColorPickerInterface {
    cp.id = p0
    return this
  }

  override fun setLabel(p0: String?): ColorPickerInterface {
    cp.setLabel(p0)
    return this
  }

  override fun setColorActive(p0: Int): ColorPickerInterface {
    cp.setColorActive(p0)
    return this
  }

  override fun setColorForeground(p0: Int): ColorPickerInterface {
    cp.setColorForeground(p0)
    return this
  }

  override fun setColorBackground(p0: Int): ColorPickerInterface {
    cp.setColorBackground(p0)
    return this
  }

  override fun setColorLabel(p0: Int): ColorPickerInterface {
    cp.setColorLabel(p0)
    return this
  }

  override fun setColorValue(p0: Int): ColorPickerInterface {
    cp.colorValue = p0
    return this
  }

  override fun setColor(p0: CColor?): ColorPickerInterface {
    cp.color = p0
    return this
  }

  override fun getColor(): CColor {
    return cp.color
  }

  override fun show(): ColorPickerInterface {
    cp.show()
    return this
  }

  override fun hide(): ColorPickerInterface {
    cp.hide()
    return this
  }

  override fun isVisible(): Boolean {
    return cp.isVisible
  }

  override fun moveTo(p0: ControllerGroup<*>?, p1: Tab?, p2: ControlWindow?): ColorPickerInterface {
    cp.moveTo(p0)
    return this
  }

  override fun moveTo(p0: ControllerGroup<*>?): ColorPickerInterface {
    cp.moveTo(p0)
    return this
  }

  override fun getPickingColor(): Int = cp.pickingColor

  override fun getProperty(p0: String?): ControllerProperty = cp.getProperty(p0)

  override fun getProperty(p0: String?, p1: String?): ControllerProperty = cp.getProperty(p0, p1)

  override fun registerProperty(p0: String?): ColorPickerInterface {
    cp.registerProperty(p0)
    return this
  }

  override fun registerProperty(p0: String?, p1: String?): ColorPickerInterface {
    cp.registerProperty(p0)
    return this
  }

  override fun removeProperty(p0: String?): ColorPickerInterface {
    cp.removeProperty(p0)
    return this
  }

  override fun removeProperty(p0: String?, p1: String?): ColorPickerInterface {
    cp.removeProperty(p0)
    return this
  }

  override fun isMouseOver(): Boolean = cp.isMouseOver

  override fun setMouseOver(p0: Boolean): ColorPickerInterface {
    cp.isMouseOver = p0
    return this
  }

  override fun setFont(p0: PFont?): ColorPickerInterface {
    cp.setFont(p0)
    return this
  }

  override fun setFont(p0: ControlFont?): ColorPickerInterface {
    cp.setFont(p0)
    return this
  }

  override fun addListener(p0: ControlListener?): ColorPickerInterface {
    cp.addListener(p0)
    return this
  }

  override fun setCaptionLabel(p0: String?): ColorPickerInterface {
    cp.setCaptionLabel(p0)
    return this
  }

}

/**
 * Needed to override controlP5 ColorPicker to get a working onChange handler.
 */
class ColorPickerOverride(cp5: ControlP5, name: String) : ColorPicker(cp5, name) {
  private val listeners = mutableListOf<ColorListener>()

  init {
    sliderRed.onChange {
      listeners.forEach { it(Color(colorValue)) }
    }
    sliderGreen.onChange {
      listeners.forEach { it(Color(colorValue)) }
    }
    sliderBlue.onChange {
      listeners.forEach { it(Color(colorValue)) }
    }
  }

  fun onChange(listener: ColorListener) {
    listeners.add(listener)
  }

  fun removeListener(listener: ColorListener) {
    listeners.remove(listener)
  }
}
