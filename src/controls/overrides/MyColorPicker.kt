package controls.overrides

import controlP5.Canvas
import controlP5.ControlEvent
import controlP5.ControlGroup
import controlP5.ControlP5
import controlP5.ControllerGroup
import controlP5.ControllerPlug
import controlP5.Slider
import processing.core.PGraphics
import java.awt.Color
import java.lang.reflect.InvocationTargetException

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
open class ColorPicker protected constructor(
  var1: ControlP5?,
  var2: ControllerGroup<*>?,
  var3: String,
  var4: Int,
  var5: Int,
  var6: Int,
  var7: Int
) :
  ControlGroup<ColorPicker?>(var1, var2, var3, var4, var5, var6, var7) {

  private val listeners = mutableListOf<(Color) -> Unit>()
  protected var sliderRed: Slider
  protected var sliderGreen: Slider
  protected var sliderBlue: Slider
  protected var sliderAlpha: Slider
  protected var currentColor: Canvas
  private var _myPlug: Any?
  private var _myPlugName: String
  private var broadcast: Boolean

  constructor(var1: ControlP5, var2: String) : this(var1, var1.defaultTab, var2, 0, 0, 255, 10) {
    var1.register(var1.papplet, var2, this)
  }

  fun onChange(listener: (Color) -> Unit) {
    listeners.add(listener)
  }

  fun plugTo(var1: Any?): ColorPicker {
    _myPlug = var1
    if (!ControllerPlug.checkPlug(_myPlug, _myPlugName, arrayOf<Class<*>>(Integer.TYPE))) {
      _myPlug = null
    }
    return this
  }

  fun plugTo(var1: Any?, var2: String): ColorPicker {
    _myPlug = var1
    _myPlugName = var2
    if (!ControllerPlug.checkPlug(_myPlug, _myPlugName, arrayOf<Class<*>>(Integer.TYPE))) {
      _myPlug = null
    }
    return this
  }

  override fun controlEvent(var1: ControlEvent) {
    if (broadcast) {
      _myArrayValue[var1.id] = var1.value
      broadcast()
    }
  }

  private fun broadcast(): ColorPicker {
    val var1 = ControlEvent(this)
    this.value = colorValue.toFloat()
    cp5.controlBroadcaster.broadcast(var1, 2)
    if (_myPlug != null) {
      try {
        val var2 = _myPlug!!.javaClass.getMethod(_myPlugName, Integer.TYPE)
        var2.invoke(_myPlug, colorValue)
      } catch (var3: SecurityException) {
        var3.printStackTrace()
      } catch (var4: NoSuchMethodException) {
        var4.printStackTrace()
      } catch (var5: IllegalArgumentException) {
        var5.printStackTrace()
      } catch (var6: IllegalAccessException) {
        var6.printStackTrace()
      } catch (var7: InvocationTargetException) {
        var7.printStackTrace()
      }
    }
    return this
  }

  override fun setArrayValue(var1: FloatArray): ColorPicker {
    broadcast = false
    sliderRed.value = var1[0]
    sliderGreen.value = var1[1]
    sliderBlue.value = var1[2]
    sliderAlpha.value = var1[3]
    broadcast = true
    _myArrayValue = var1
    return broadcast()
  }

  override fun setColorValue(var1: Int): ColorPicker {
    this.arrayValue = floatArrayOf(
      (var1 shr 16 and 255).toFloat(),
      (var1 shr 8 and 255).toFloat(),
      (var1 shr 0 and 255).toFloat(),
      (var1 shr 24 and 255).toFloat(),
    )
    return this
  }

  val colorValue: Int
    get() = -1 and _myArrayValue[3]
      .toInt() shl 24 or (_myArrayValue[0]
      .toInt() shl 16) or (_myArrayValue[1].toInt() shl 8) or (_myArrayValue[2].toInt() shl 0)

  override fun getInfo(): String {
    return """
       type:	ColorPicker
       ${super.getInfo()}
       """.trimIndent()
  }

  private inner class ColorField : Canvas() {
    override fun draw(var1: PGraphics) {
      var1.fill(
        _myArrayValue[0],
        _myArrayValue[1],
        _myArrayValue[2], _myArrayValue[3],
      )
      var1.rect(0.0f, 44.0f, this@ColorPicker.width.toFloat(), 15.0f)
    }
  }

  init {
    isBarVisible = false
    isCollapse = false
    _myArrayValue = floatArrayOf(255.0f, 255.0f, 255.0f, 255.0f)
    currentColor = addCanvas(ColorField())
    sliderRed = cp5.addSlider("$var3-red", 0.0f, 255.0f, 0, 0, var6, var7)
    cp5.removeProperty(sliderRed)
    sliderRed.id = 0
    sliderRed.isBroadcast = false
    sliderRed.addListener(this)
    sliderRed.moveTo(this)
    sliderRed.isMoveable = false
    sliderRed.setColorBackground(-10092544)
    sliderRed.setColorForeground(-5636096)
    sliderRed.setColorActive(-65536)
    sliderRed.captionLabel.isVisible = false
    sliderRed.decimalPrecision = 0
    sliderRed.value = 255.0f
    sliderGreen = cp5.addSlider("$var3-green", 0.0f, 255.0f, 0, var7 + 1, var6, var7)
    cp5.removeProperty(sliderGreen)
    sliderGreen.id = 1
    sliderGreen.isBroadcast = false
    sliderGreen.addListener(this)
    sliderGreen.moveTo(this)
    sliderGreen.isMoveable = false
    sliderGreen.setColorBackground(-16751104)
    sliderGreen.setColorForeground(-16733696)
    sliderGreen.setColorActive(-16711936)
    sliderGreen.captionLabel.isVisible = false
    sliderGreen.decimalPrecision = 0
    sliderGreen.value = 255.0f
    sliderBlue = cp5.addSlider("$var3-blue", 0.0f, 255.0f, 0, (var7 + 1) * 2, var6, var7)
    cp5.removeProperty(sliderBlue)
    sliderBlue.id = 2
    sliderBlue.isBroadcast = false
    sliderBlue.addListener(this)
    sliderBlue.moveTo(this)
    sliderBlue.isMoveable = false
    sliderBlue.setColorBackground(-16777114)
    sliderBlue.setColorForeground(-16777046)
    sliderBlue.setColorActive(-16776961)
    sliderBlue.captionLabel.isVisible = false
    sliderBlue.decimalPrecision = 0
    sliderBlue.value = 255.0f
    sliderAlpha = cp5.addSlider("$var3-alpha", 0.0f, 255.0f, 0, (var7 + 1) * 3, var6, var7)
    cp5.removeProperty(sliderAlpha)
    sliderAlpha.id = 3
    sliderAlpha.isBroadcast = false
    sliderAlpha.addListener(this)
    sliderAlpha.moveTo(this)
    sliderAlpha.isMoveable = false
    sliderAlpha.setColorBackground(-10066330)
    sliderAlpha.setColorForeground(-5592406)
    sliderAlpha.setColorActive(-1)
    sliderAlpha.captionLabel.isVisible = false
    sliderAlpha.decimalPrecision = 0
    sliderAlpha.valueLabel.color = -16777216
    sliderAlpha.value = 255.0f
    _myPlug = cp5.papplet
    _myPlugName = this.name
    if (!ControllerPlug.checkPlug(_myPlug, _myPlugName, arrayOf<Class<*>>(Integer.TYPE))) {
      _myPlug = null
    }
    broadcast = true
  }
}
