package appletExtensions

import coordinate.Point

fun PAppletExt.mouseLocation(): Point = Point(mouseX, mouseY)
fun PAppletExt.isMouseHovering() = windowBounds.contains(mouseLocation())
