package util.io.geoJson

import coordinate.Point
import geomerativefork.src.util.toDoubleArray
import java.awt.image.RenderedImage
import javax.media.jai.InterpolationBicubic
import javax.media.jai.JAI
import javax.media.jai.ParameterBlockJAI
import javax.media.jai.RenderedOp
import javax.media.jai.operator.ClampDescriptor
import javax.media.jai.operator.ScaleDescriptor

sealed class JaiFilter(
  val opName: String,
  private val getOp: (RenderedImage) -> RenderedOp
) {
  fun filter(image: RenderedImage): RenderedImage = getOp(image).rendering
}

class Scale(factor: Point) : JaiFilter(
  "Scale",
  { ScaleDescriptor.create(it, factor.xf, factor.yf, 0f, 0f, InterpolationBicubic(8), null) },
)

class Contour(thresholds: List<Double>) : JaiFilter(
  "Contour",
  { image ->
    ParameterBlockJAI("Contour").let {
      it.setSource("source0", image)
      it.setParameter("levels", thresholds)
      JAI.create("Contour", it)
    }
  },
)

class Clamp(low: Number, high: Number) : JaiFilter(
  "Clamp",
  {
    ClampDescriptor.create(it, low.toDoubleArray(), high.toDoubleArray(), null)
  },
)

fun RenderedImage.jaiFilter(filter: JaiFilter): RenderedImage {
  println("Running filter: ${filter.opName}")
  return filter.filter(this)
}
