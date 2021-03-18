package geomerativefork.src

import geomerativefork.src.IntersectionType.Entering
import geomerativefork.src.IntersectionType.Exiting
import geomerativefork.src.IntersectionType.TangentInside
import geomerativefork.src.IntersectionType.TangentOutside
import geomerativefork.src.util.flatMapArray
import geomerativefork.src.util.flatMapArrayIndexed
import geomerativefork.src.util.mapArray

enum class IntersectionType(val isTangent: Boolean) {
  Entering(false),
  Exiting(false),
  TangentInside(true),
  TangentOutside(true),
}

class Intersection(
  val command: RCommand,
  val otherCommand: RCommand,
  val commandIndex: Int,
  val point: RPoint,
  val type: IntersectionType,
) {
  companion object {
    fun RPath.intersectionsWith(other: RPath): Array<Intersection> =
      other.commands.flatMapArray { otherCommand ->
        commands.flatMapArrayIndexed { commandIndex, command ->
          // The issue is intersection points only works for line-line intersections right now.
          command.intersectionPoints(otherCommand)
            .mapArray { intersectionPoint ->
              val startIsInsideOther = other.contains(command.startPoint)
              val endIsInsideOther = other.contains(command.endPoint)

              val intersectionType = when {
                startIsInsideOther && endIsInsideOther -> TangentInside
                !startIsInsideOther && endIsInsideOther -> Entering
                startIsInsideOther && !endIsInsideOther -> Exiting
                else -> TangentOutside
              }

              Intersection(command, otherCommand, commandIndex, intersectionPoint, intersectionType)
            }
        }
      }

    fun RShape.intersectionsWith(other: RPath): Array<Array<Intersection>> =
      paths.mapArray { it.intersectionsWith(other) }
  }

  override fun toString(): String {
    return "Intersection(point=$point, type=$type)"
  }
}
