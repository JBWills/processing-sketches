package util.io.geoJson

import arrow.core.memoize
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.gce.geotiff.GeoTiffReader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opengis.parameter.GeneralParameterValue
import java.awt.image.DataBufferFloat
import java.io.File

val loadGeoDataMemo: (String) -> GridCoverage2D =
  { filename: String -> File(filename).readGeoTiff() }.memoize()

val loadGeoMatMemo: (String) -> Mat =
  { filename: String -> File(filename).readGeoTiff().toMat() }

fun GridCoverage2D.toMat(): Mat {
  val image = renderedImage
  val pixels: FloatArray = (image.data.dataBuffer as DataBufferFloat).data
  return Mat(image.height, image.width, CvType.CV_32F).apply { put(0, 0, pixels) }
}

fun File.readGeoTiff(): GridCoverage2D =
  GeoTiffReader(this)
    .read(*arrayOf<GeneralParameterValue>())

