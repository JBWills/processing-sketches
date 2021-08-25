package util.javageom

import coordinate.BoundRect
import coordinate.Point
import java.awt.Rectangle
import java.awt.Shape

fun Rectangle.toBoundRect() = BoundRect(Point(x, y), width, height)

fun Shape.boundRect(): BoundRect = bounds.toBoundRect()
