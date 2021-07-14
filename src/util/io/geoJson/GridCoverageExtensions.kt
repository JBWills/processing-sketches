package util.io.geoJson

import coordinate.BoundRect
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.data.simple.SimpleFeatureCollection
import org.geotools.process.raster.ContourProcess
import org.opengis.util.ProgressListener

fun GridCoverage2D.contourGrid(
  thresholds: List<Double>,
  simplify: Boolean = true,
  smooth: Boolean = true,
  roi: BoundRect? = null,
  progressListener: ProgressListener? = null,
): SimpleFeatureCollection = ContourProcess().execute(
  this,
  0,
  thresholds.toTypedArray().toDoubleArray(),
  null,
  simplify,
  smooth,
  null, // todo: use ROI
  progressListener,
)
