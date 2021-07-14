package util.io.geoJson

import arrow.core.memoize
import controls.props.types.LineSimplifierProp
import controls.props.types.LineSimplifierProp.Companion.simplifyContours
import coordinate.BoundRect
import coordinate.Point
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.gce.geotiff.GeoTiffReader
import org.jaitools.media.jai.contour.ContourDescriptor
import org.locationtech.jts.geom.LineString
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opengis.parameter.GeneralParameterValue
import util.asCollection
import util.geo.getSampleDouble
import util.iterators.groupValuesBy
import util.pointsAndLines.polyLine.PolyLine
import java.awt.image.DataBufferFloat
import java.awt.image.Raster
import java.awt.image.RenderedImage
import java.io.File

val loadGeoDataMemo: (String) -> GridCoverage2D =
  { filename: String -> File(filename).readGeoTiff() }.memoize()

val loadGeoMatMemo: (String) -> Mat =
  { filename: String -> File(filename).readGeoTiff().toMat() }

val getGeoContoursMemo = ::loadAndGetGeoContours.memoize()
val loadGeoImageMemo = ::loadGeoImage.memoize()

fun RenderedImage.clamp(min: Number, max: Number = 10_000.0): RenderedImage =
  jaiFilter(Clamp(min, max))

fun RenderedImage.scale(factor: Point): RenderedImage = jaiFilter(Scale(factor))
fun RenderedImage.contour(thresholds: List<Double>): RenderedImage = jaiFilter(Contour(thresholds))

fun RenderedImage.getContourLineStrings(thresholds: List<Double>): List<LineString> =
  contour(thresholds)
    .getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME)
    .asCollection()
    .mapNotNull { line -> if (line == null) null else (line as LineString) }

fun loadGeoImage(fName: String): Pair<RenderedImage, Raster> {
  val image = loadGeoDataMemo(fName).renderedImage

  val scaledImage = image
    .clamp(0)
    .scale(Point.Half)

  val scaledImageData = scaledImage.data

  return scaledImage to scaledImageData
}

fun loadAndGetGeoContours(
  fName: String,
  thresholds: List<Double>,
  lineSimplifierProp: LineSimplifierProp,
): Pair<BoundRect, Map<Double, List<PolyLine>>> {
  val (scaledImage, scaledImageData) = loadGeoImageMemo(fName)

  val res = scaledImage
    .getContourLineStrings(thresholds)
    .simplifyContours(lineSimplifierProp)
    .asPolyLines()
    .map { line ->
      val threshold = scaledImageData.getSampleDouble(line.first())
      threshold to line
    }
    .groupValuesBy { first }
    .mapValues { (_, v) -> v.map { it.second } }

  return BoundRect(scaledImage.width, scaledImage.height) to res
}

fun GridCoverage2D.toMat(): Mat {
  val image = renderedImage
  val pixels: FloatArray = (image.data.dataBuffer as DataBufferFloat).data
  return Mat(image.height, image.width, CvType.CV_32F).apply { put(0, 0, pixels) }
}

fun File.readGeoTiff(): GridCoverage2D =
  GeoTiffReader(this)
    .read(*arrayOf<GeneralParameterValue>())

