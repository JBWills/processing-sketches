package sketches

import controls.ControlField.Companion.intField
import controls.ControlGroupable
import controls.PropFields
import controls.Props
import controls.controls
import sketches.Example.ExampleGlobalValues
import sketches.Example.ExampleTabValues
import sketches.base.LayeredCanvasSketch

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Example : LayeredCanvasSketch<ExampleTabValues, ExampleGlobalValues>("Example") {
  override fun drawSetup(drawInfo: DrawInfo) {
  }

  override fun drawOnce(layer: LayerInfo) {

  }

  data class ExampleTabValues(
    val exampleField: Int = 1
  )

  data class ExampleGlobalValues(
    val exampleField: Int = 1
  )

  // TODO: Can this be simplified?
  override fun initProps(): Props<ExampleTabValues, ExampleGlobalValues> =
    object : Props<ExampleTabValues, ExampleGlobalValues>(maxLayers) {
      override fun globalControls(): PropFields<ExampleGlobalValues> =
        object : PropFields<ExampleGlobalValues> {
          private val defaults = ExampleGlobalValues()
          val exampleField = intField(defaults::exampleField, 0..100)

          override fun toControls(): List<ControlGroupable> = controls(exampleField)

          override fun toValues(): ExampleGlobalValues = ExampleGlobalValues(
            exampleField.get()
          )
        }

      override fun tabControls(tabIndex: Int): PropFields<ExampleTabValues> =
        object : PropFields<ExampleTabValues> {
          private val defaults = ExampleTabValues()
          val exampleField = intField(defaults::exampleField, 0..100)

          override fun toControls(): List<ControlGroupable> = controls(exampleField)

          override fun toValues() = ExampleTabValues(exampleField.get())
        }
    }
}

fun main() = Example().run()
