package util.polylines

import coordinate.IndexedMeshLines
import coordinate.MeshLines
import util.image.opencvMat.simplify
import util.image.opencvMat.toMatOfPoint2f
import util.image.opencvMat.toPolyLine
import util.tuple.map
import util.tuple.mapPairOfLists

fun PolyLine.simplify(epsilon: Double): PolyLine =
  if (size < 3 || epsilon == 0.0) this
  else toMatOfPoint2f().simplify(epsilon).toPolyLine()

@JvmName("simplifyPolyLines")
fun List<PolyLine>.simplify(epsilon: Double): List<PolyLine> = map { it.simplify(epsilon) }

@JvmName("simplifyIndexedMesh")
fun IndexedMeshLines.simplifyMesh(epsilon: Double): IndexedMeshLines =
  mapPairOfLists { it.simplify(epsilon) }

fun MeshLines.simplifyMesh(epsilon: Double): MeshLines =
  map { it.simplify(epsilon) }
