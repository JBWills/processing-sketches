package util.layers

import kotlinx.serialization.Serializable
import util.print.Style

@Serializable
data class LayerSVGConfig(
  val layerName: String? = null,
  val nextLayerName: String? = null,
  val style: Style = Style()
)
