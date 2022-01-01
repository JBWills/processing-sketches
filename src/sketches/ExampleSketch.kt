package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class ExampleSketch : SimpleCanvasSketch<ExampleData>("Example", ExampleData()) {

  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (exampleGlobalField) = drawInfo.dataValues
  }
}

@Serializable
data class ExampleData(
  var exampleField: Int = 1,
) : PropData<ExampleData> {
  override fun bind() = tabs {
    tab("Global") {
      slider(::exampleField, 0..10)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = ExampleSketch().run()
