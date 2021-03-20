/**
 * Copyright 2004-2008 Ricard Marxer  <email></email>@ricardmarxer.com>
 *
 *
 * This file is part of Geomerative.
 *
 *
 * Geomerative is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * Geomerative is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Geomerative.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package geomerativefork.src

import geomerativefork.src.RGeomElem.Companion.GROUP
import geomerativefork.src.RGeomElem.Companion.POLYGON
import geomerativefork.src.RGeomElem.Companion.SHAPE
import geomerativefork.src.RShape.Companion.createEllipse
import geomerativefork.src.RShape.Companion.createRectangle
import processing.core.PApplet
import processing.core.PGraphics
import processing.data.XML

/**
 * @extended
 */
class RSVG {
  fun draw(filename: String, g: PGraphics) = toGroup(filename).draw(g)

  fun draw(filename: String, p: PApplet) = toGroup(filename).draw(p)

  fun draw(filename: String) = toGroup(filename).draw()

  fun saveShape(filename: String, shp: RShape) =
    RG.parent().saveStrings(
      filename,
      fromShape(shp).split("\n").toTypedArray()
    )

  fun fromShape(shape: RShape): String {
    val header =
      "<?xml version=\"1.0\" standalone=\"no\"?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg width=\"100%\" height=\"100%\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n"
    return header + shapeToString(shape) + "</svg>"
  }

  fun saveGroup(filename: String?, grp: RGroup) =
    RG.parent()
      .saveStrings(
        filename,
        fromGroup(grp).split("\n").toTypedArray()
      )

  fun fromGroup(group: RGroup): String {
    val header =
      "<?xml version=\"1.0\" standalone=\"no\"?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg width=\"100%\" height=\"100%\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n"
    return header + groupToString(group) + "</svg>"
  }

  fun toGroup(filename: String): RGroup {
    var svg: XML? = null
    try {
      svg = RG.parent().loadXML(filename)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    if (svg == null) return RGroup()
    if (svg.name != "svg") {
      throw RuntimeException("root is not <svg>, it's <" + svg.name + ">")
    }
    return elemToGroup(svg)
  }

  @JvmOverloads
  fun unitsToPixels(
    units: String,
    originalPxSize: Float,
    dpi: Float = 72.0f
  ): Float {
    fun toPx(chars: Int, multiplier: Float) =
      units.dropLast(chars).toFloat() * multiplier
    return when {
      units.endsWith("em") -> toPx(2, 1.0f)
      units.endsWith("ex") -> toPx(2, 1.0f)
      units.endsWith("px") -> toPx(2, 1.0f)
      units.endsWith("pt") -> toPx(2, 1.25f)
      units.endsWith("pc") -> toPx(2, 15f)
      units.endsWith("cm") -> toPx(2, 35.43307f / 90.0f * dpi)
      units.endsWith("mm") -> toPx(2, 3.543307f / 90.0f * dpi)
      units.endsWith("in") -> toPx(2, dpi)
      units.endsWith("%") -> toPx(1, originalPxSize / 100.0f)
      else -> toPx(0, 1.0f)
    }
  }

  fun toShape(filename: String): RShape {
    var svg: XML? = null
    try {
      svg = RG.parent().loadXML(filename)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    if (svg == null) return RShape()
    if (svg.name != "svg") {
      throw RuntimeException("root is not <svg>, it's <" + svg.name + ">")
    }
    val result = elemToCompositeShape(svg)
    result.elemOrigWidth = result.getWidth()
    result.elemOrigHeight = result.getHeight()
    if (svg.hasAttribute("width") && svg.hasAttribute("height")) {
      val widthStr = svg.getString("width").trim { it <= ' ' }
      val heightStr = svg.getString("height").trim { it <= ' ' }
      result.elemWidth = unitsToPixels(
        widthStr,
        result.elemOrigWidth
      )
      result.elemHeight = unitsToPixels(
        heightStr,
        result.elemOrigHeight
      )
    } else {
      result.elemWidth = result.elemOrigWidth
      result.elemHeight = result.elemOrigHeight
    }
    return result
  }

  fun toPolygon(filename: String): RPolygon = toGroup(filename).toPolygon()

  fun toMesh(filename: String): RMesh = toGroup(filename).toMesh()

  fun groupToString(grp: RGroup): String {
    var result = "<g ${styleToString(grp.style)}>\n"
    grp.elements.forEach { element ->
      when (element.type) {
        GROUP -> result += groupToString(element as RGroup)
        POLYGON -> result += polygonToString(element as RPolygon)
        SHAPE -> result += shapeToString(element as RShape)
      }
    }
    result += "</g>\n"
    return result
  }

  fun polygonToString(poly: RPolygon): String = shapeToString(poly.toShape())

  fun shapeToString(shp: RShape): String {
    var result = ""

    // If it has children it is a group
    result += "<g "
    result += styleToString(shp.style)
    result += ">\n"
    if (shp.paths.isNotEmpty()) {
      result += "<path "
      result += "d=\""
      for (path in shp.paths) {
        var init = true
        for (cmd in path.commands) {
          if (init) {
            result += "M" + cmd.startPoint.x + " " + cmd.startPoint.y + " "
            init = false
          }
          when (cmd.type) {
            RCommand.LINETO -> result += "L${cmd.endPoint.x} ${cmd.endPoint.y} "
            RCommand.QUADBEZIERTO -> result += "Q${cmd.controlPoints[0].x} ${cmd.controlPoints[0].y}${cmd.endPoint.x} ${cmd.endPoint.y} "
            RCommand.CUBICBEZIERTO -> result += "C${cmd.controlPoints[0].x} ${cmd.controlPoints[0].y} ${cmd.controlPoints[1].x} ${cmd.controlPoints[1].y} ${cmd.endPoint.x} ${cmd.endPoint.y} "
          }
        }
        if (path.closed) {
          result += "Z "
        }
      }
      result += "\"/>\n"
    }
    for (child in shp.children) {
      result += shapeToString(child)
    }
    result += "</g>\n"
    return result
  }

  fun styleToString(style: RStyle): String {
    var result = " style=\""
    if (style.fillDef) {
      result += if (!style.fill) {
        "fill:none;"
      } else {
        "fill:#" + PApplet.hex(
          style.fillColor,
          6
        ) + ";"
      }
    }
    if (style.fillAlphaDef) {
      result += "fill-opacity:" + (style.fillAlpha / 255.0f).toString() + ";"
    }
    if (style.strokeDef) {
      result += if (!style.stroke) {
        "stroke:none;"
      } else {
        "stroke:#" + PApplet.hex(
          style.strokeColor,
          6
        ) + ";"
      }
    }
    if (style.strokeAlphaDef) {
      result += "stroke-opacity:" + (style.strokeAlpha / 255.0f).toString() + ";"
    }
    if (style.strokeWeightDef) {
      result += "stroke-width:" + style.strokeWeight.toString() + ";"
    }
    if (style.strokeCapDef) {
      result += "stroke-linecap:"
      when (style.strokeCap) {
        RG.PROJECT -> result += "butt"
        RG.ROUND -> result += "round"
        RG.SQUARE -> result += "square"
        else -> {
        }
      }
      result += ";"
    }
    if (style.strokeJoinDef) {
      result += "stroke-linejoin:"
      when (style.strokeJoin) {
        RG.MITER -> result += "miter"
        RG.ROUND -> result += "round"
        RG.BEVEL -> result += "bevel"
        else -> {
        }
      }
      result += ";"
    }
    result += "\" "
    return result
  }

  /**
   * @invisible
   */
  fun elemToGroup(elem: XML): RGroup {
    val grp = RGroup()

    // Set the defaults SVG styles for the root
    if (elem.name.equals("svg", ignoreCase = true)) {
      grp.setFill(0) // By default in SVG it's black
      grp.fillAlpha = 255 // By default in SVG it's 1
      grp.stroke = false // By default in SVG it's none
      grp.strokeWeight = 1f // By default in SVG it's none
      grp.setStrokeCap("butt") // By default in SVG it's 'butt'
      grp.setStrokeJoin("miter") // By default in SVG it's 'miter'
      grp.strokeAlpha = 255 // By default in SVG it's 1
    }
    val elems = elem.children
    for (i in elems.indices) {
      val name = elems[i].name.toLowerCase()
      val element = elems[i]

      // Parse and create the geometrical element
      var geomElem: RGeomElem? = null
      if (name == "g") {
        geomElem = elemToGroup(element)
      } else if (name == "path") {
        geomElem = elemToShape(element)
      } else if (name == "polygon") {
        geomElem = elemToPolygon(element)
      } else if (name == "polyline") {
        geomElem = elemToPolyline(element)
      } else if (name == "circle") {
        geomElem = elemToCircle(element)
      } else if (name == "ellipse") {
        geomElem = elemToEllipse(element)
      } else if (name == "rect") {
        geomElem = elemToRect(element)
      } else if (name == "line") {
        geomElem = elemToLine(element)
      } else if (name == "defs") {
        // Do nothing normally we should make a hashmap
        // to apply everytime they are called in the actual objects
      } else {
        PApplet.println("Element '$name' not know. Ignoring it.")
      }

      // If the geometrical element has been correctly created
      if (geomElem != null) {
        // Transform geometrical element
        if (element.hasAttribute("transform")) {
          val transformString = element.getString("transform")
          val transf = RMatrix(transformString)
          geomElem.transform(transf)
        }

        // Get the id for the geometrical element
        if (element.hasAttribute("id")) {
          geomElem.elemName = element.getString("id")
        }

        // Get the style for the geometrical element
        if (element.hasAttribute("style")) {
          geomElem.setStyle(element.getString("style"))
        }

        // Get the fill for the geometrical element
        if (element.hasAttribute("fill")) {
          geomElem.setFill(element.getString("fill"))
        }

        // Get the fill-linejoin for the geometrical element
        if (element.hasAttribute("fill-opacity")) {
          geomElem.setFillAlpha(element.getString("fill-opacity"))
        }

        // Get the stroke for the geometrical element
        if (element.hasAttribute("stroke")) {
          geomElem.setStroke(element.getString("stroke"))
        }

        // Get the stroke-width for the geometrical element
        if (element.hasAttribute("stroke-width")) {
          geomElem.setStrokeWeight(element.getString("stroke-width"))
        }

        // Get the stroke-linecap for the geometrical element
        if (element.hasAttribute("stroke-linecap")) {
          geomElem.setStrokeCap(element.getString("stroke-linecap"))
        }

        // Get the stroke-linejoin for the geometrical element
        if (element.hasAttribute("stroke-linejoin")) {
          geomElem.setStrokeJoin(element.getString("stroke-linejoin"))
        }

        // Get the stroke-linejoin for the geometrical element
        if (element.hasAttribute("stroke-opacity")) {
          geomElem.setStrokeAlpha(element.getString("stroke-opacity"))
        }

        // Get the opacity for the geometrical element
        if (element.hasAttribute("opacity")) {
          geomElem.setAlpha(element.getString("opacity"))
        }

        // Get the style for the geometrical element
        grp.addElement(geomElem)
      }
    }

    // Set the original width and height
    grp.updateOrigParams()
    return grp
  }

  /**
   * @invisible
   */
  fun elemToCompositeShape(elem: XML): RShape {
    val shp = RShape()

    // Set the defaults SVG styles for the root
    if (elem.name.equals("svg", ignoreCase = true)) {
      shp.setFill(0) // By default in SVG it's black
      shp.fillAlpha = 255 // By default in SVG it's 1
      shp.stroke = false // By default in SVG it's none
      shp.strokeWeight = 1f // By default in SVG it's none
      shp.setStrokeCap("butt") // By default in SVG it's 'butt'
      shp.setStrokeJoin("miter") // By default in SVG it's 'miter'
      shp.strokeAlpha = 255 // By default in SVG it's 1
      shp.setAlpha(255) // By default in SVG it's 1F
    }
    val elems = elem.children
    for (i in elems.indices) {
      var name = elems[i].name ?: continue
      name = name.toLowerCase()
      val element = elems[i]

      // Parse and create the geometrical element
      var geomElem: RShape? = null
      if (name == "g") {
        geomElem = elemToCompositeShape(element)
      } else if (name == "path") {
        geomElem = elemToShape(element)
      } else if (name == "polygon") {
        geomElem = elemToPolygon(element)
      } else if (name == "polyline") {
        geomElem = elemToPolyline(element)
      } else if (name == "circle") {
        geomElem = elemToCircle(element)
      } else if (name == "ellipse") {
        geomElem = elemToEllipse(element)
      } else if (name == "rect") {
        geomElem = elemToRect(element)
      } else if (name == "line") {
        geomElem = elemToLine(element)
      } else if (name == "defs") {
        // Do nothing normally we should make a hashmap
        // to apply everytime they are called in the actual objects
      } else {
        PApplet.println("Element '$name' not know. Ignoring it.")
      }

      // If the geometrical element has been correctly created
      if (geomElem != null) {
        // Transform geometrical element
        if (element.hasAttribute("transform")) {
          val transformString = element.getString("transform")
          val transf = RMatrix(transformString)
          geomElem.transform(transf)
        }

        // Get the id for the geometrical element
        if (element.hasAttribute("id")) {
          geomElem.elemName = element.getString("id")
        }

        // Get the style for the geometrical element
        if (element.hasAttribute("style")) {
          geomElem.setStyle(element.getString("style"))
        }

        // Get the fill for the geometrical element
        if (element.hasAttribute("fill")) {
          geomElem.setFill(element.getString("fill"))
        }

        // Get the fill-linejoin for the geometrical element
        if (element.hasAttribute("fill-opacity")) {
          geomElem.setFillAlpha(element.getString("fill-opacity"))
        }

        // Get the stroke for the geometrical element
        if (element.hasAttribute("stroke")) {
          geomElem.setStroke(element.getString("stroke"))
        }

        // Get the stroke-width for the geometrical element
        if (element.hasAttribute("stroke-width")) {
          geomElem.setStrokeWeight(element.getString("stroke-width"))
        }

        // Get the stroke-linecap for the geometrical element
        if (element.hasAttribute("stroke-linecap")) {
          geomElem.setStrokeCap(element.getString("stroke-linecap"))
        }

        // Get the stroke-linejoin for the geometrical element
        if (element.hasAttribute("stroke-linejoin")) {
          geomElem.setStrokeJoin(element.getString("stroke-linejoin"))
        }

        // Get the stroke-linejoin for the geometrical element
        if (element.hasAttribute("stroke-opacity")) {
          geomElem.setStrokeAlpha(element.getString("stroke-opacity"))
        }

        // Get the opacity for the geometrical element
        if (element.hasAttribute("opacity")) {
          geomElem.setAlpha(element.getString("opacity"))
        }

        // Get the style for the geometrical element
        shp.addChild(geomElem)
      }
    }
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  fun elemToPolyline(elem: XML): RShape {
    val shp = getPolyline(elem.getString("points").trim { it <= ' ' })
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  fun elemToPolygon(elem: XML): RShape {
    val poly = elemToPolyline(elem)
    poly.addClose()
    poly.updateOrigParams()
    return poly
  }

  /**
   * @invisible
   */
  fun elemToRect(elem: XML): RShape {
    val shp = getRect(
      elem.getFloat("x"),
      elem.getFloat("y"),
      elem.getFloat("width"),
      elem.getFloat("height")
    )
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  fun elemToLine(elem: XML): RShape {
    val shp = getLine(
      elem.getFloat("x1"),
      elem.getFloat("y1"),
      elem.getFloat("x2"),
      elem.getFloat("y2")
    )
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  fun elemToEllipse(elem: XML): RShape {
    val shp = getEllipse(
      elem.getFloat("cx"),
      elem.getFloat("cy"),
      elem.getFloat("rx"),
      elem.getFloat("ry")
    )
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  fun elemToCircle(elem: XML): RShape {
    val r = elem.getFloat("r")
    val shp = getEllipse(
      elem.getFloat("cx"),
      elem.getFloat("cy"),
      r,
      r
    )
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  fun elemToShape(elem: XML): RShape {
    val shp = getShape(elem.getString("d"))
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  private fun getRect(x: Float, y: Float, w: Float, h: Float): RShape {
    val shp = createRectangle(
      x,
      y,
      w,
      h
    )
    shp.updateOrigParams()
    return shp
  }

  /**
   * @invisible
   */
  private fun getLine(x1: Float, y1: Float, x2: Float, y2: Float): RShape {
    val shp = RShape()
    shp.addMoveTo(
      x1,
      y1
    )
    shp.addLineTo(
      x2,
      y2
    )
    return shp
  }

  /**
   * @invisible
   */
  private fun getEllipse(cx: Float, cy: Float, rx: Float, ry: Float): RShape {
    // RShape createEllipse takes as input the width and height of the ellipses
    return createEllipse(
      cx,
      cy,
      rx * 2f,
      ry * 2f
    )
  }

  /**
   * @invisible
   */
  private fun getPolyline(s: String): RShape {
    val poly = RShape()
    var first = true

    //format string to usable format
    var charline = s.toCharArray()
    run {
      var i = 0
      while (i < charline.size) {
        when (charline[i]) {
          '-' -> if (i > 0 && charline[i - 1] != 'e' && charline[i - 1] != 'E') {
            charline = PApplet.splice(
              charline,
              ' ',
              i
            )
            i++
          }
          ',', '\n', '\r', '\t' -> charline[i] = ' '
        }
        i++
      }
    }
    val formatted = String(charline)
    val tags = PApplet.splitTokens(
      formatted,
      ", "
    )
    var i = 0
    while (i < tags.size) {
      val x = PApplet.parseFloat(tags[i])
      val y = PApplet.parseFloat(tags[i + 1])
      i++
      if (first) {
        poly.addMoveTo(
          x,
          y
        )
        first = false
      } else {
        poly.addLineTo(
          x,
          y
        )
      }
      i++
    }
    return poly
  }

  /**
   * @invisible
   */
  private fun getShape(s: String?): RShape {
    val shp = RShape()
    if (s == null) {
      return shp
    }

    //format string to usable format
    var charline = s.toCharArray()
    run {
      var i = 0
      while (i < charline.size) {
        when (charline[i]) {
          'M', 'm', 'Z', 'z', 'C', 'c', 'S', 's', 'L', 'l', 'H', 'h', 'V', 'v' -> {
            charline = PApplet.splice(
              charline,
              ' ',
              i
            )
            i++
            charline = PApplet.splice(
              charline,
              ' ',
              i + 1
            )
            i++
          }
          '-' -> if (i > 0 && charline[i - 1] != 'e' && charline[i - 1] != 'E') {
            charline = PApplet.splice(
              charline,
              ' ',
              i
            )
            i++
          }
          ',', '\n', '\r', '\t' -> charline[i] = ' '
        }
        i++
      }
    }
    val formatted = String(charline)
    val tags = PApplet.splitTokens(formatted)

    //PApplet.println("formatted: " + formatted);
    //PApplet.println("tags: ");
    //PApplet.println(tags);

    //build points
    val curp = RPoint(
      0,
      0
    )
    val relp = RPoint(
      0,
      0
    )
    val refp = RPoint(
      0,
      0
    )
    val strp = RPoint(
      0,
      0
    )
    var command = 'a'
    var i = 0
    while (i < tags.size) {
      val nextChar = tags[i][0]
      when (nextChar) {
        'm', 'M', 'c', 'C', 's', 'S', 'l', 'L', 'h', 'H', 'v', 'V' -> {
          i += 1
          command = nextChar
        }
        'z', 'Z' -> command = nextChar
        else -> if (command == 'm') {
          command = 'l'
        } else if (command == 'M') {
          command = 'L'
        }
      }
      relp.setLocation(
        0f,
        0f
      )
      when (command) {
        'm' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          i = move(
            shp,
            curp,
            relp,
            refp,
            strp,
            tags,
            i
          )
        }
        'M' -> i = move(
          shp,
          curp,
          relp,
          refp,
          strp,
          tags,
          i
        )
        'z' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          shp.addClose()
        }
        'Z' -> shp.addClose()
        'c' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          i = curve(
            shp,
            curp,
            relp,
            refp,
            strp,
            tags,
            i
          )
        }
        'C' -> i = curve(
          shp,
          curp,
          relp,
          refp,
          strp,
          tags,
          i
        )
        's' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          i = smooth(
            shp,
            curp,
            relp,
            refp,
            strp,
            tags,
            i
          )
        }
        'S' -> i = smooth(
          shp,
          curp,
          relp,
          refp,
          strp,
          tags,
          i
        )
        'l' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          i = line(
            shp,
            curp,
            relp,
            refp,
            strp,
            tags,
            i
          )
        }
        'L' -> i = line(
          shp,
          curp,
          relp,
          refp,
          strp,
          tags,
          i
        )
        'h' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          i = horizontal(
            shp,
            curp,
            relp,
            refp,
            strp,
            tags,
            i
          )
        }
        'H' -> i = horizontal(
          shp,
          curp,
          relp,
          refp,
          strp,
          tags,
          i
        )
        'v' -> {
          relp.setLocation(
            curp.x,
            curp.y
          )
          i = vertical(
            shp,
            curp,
            relp,
            refp,
            strp,
            tags,
            i
          )
        }
        'V' -> i = vertical(
          shp,
          curp,
          relp,
          refp,
          strp,
          tags,
          i
        )
      }
      i++
    }
    return shp
  }

  private fun move(
    shp: RShape,
    curp: RPoint,
    relp: RPoint,
    refp: RPoint,
    strp: RPoint,
    tags: Array<String>,
    i: Int
  ): Int {
    shp.addMoveTo(
      PApplet.parseFloat(tags[i]) + relp.x,
      PApplet.parseFloat(tags[i + 1]) + relp.y
    )
    curp.setLocation(
      PApplet.parseFloat(tags[i]) + relp.x,
      PApplet.parseFloat(tags[i + 1]) + relp.y
    )
    refp.setLocation(
      curp.x,
      curp.y
    )
    strp.setLocation(
      curp.x,
      curp.y
    )
    //relp.setLocation(0F, 0F);
    return i + 1
  }

  private fun curve(
    shp: RShape,
    curp: RPoint,
    relp: RPoint,
    refp: RPoint,
    strp: RPoint,
    tags: Array<String>,
    i: Int
  ): Int {
    shp.addBezierTo(
      PApplet.parseFloat(tags[i]) + relp.x,
      PApplet.parseFloat(tags[i + 1]) + relp.y,
      PApplet.parseFloat(tags[i + 2]) + relp.x,
      PApplet.parseFloat(tags[i + 3]) + relp.y,
      PApplet.parseFloat(tags[i + 4]) + relp.x,
      PApplet.parseFloat(tags[i + 5]) + relp.y
    )
    curp.setLocation(
      PApplet.parseFloat(tags[i + 4]) + relp.x,
      PApplet.parseFloat(tags[i + 5]) + relp.y
    )
    refp.setLocation(
      2.0f * curp.x - (PApplet.parseFloat(tags[i + 2]) + relp.x),
      2.0f * curp.y - (PApplet.parseFloat(tags[i + 3]) + relp.y)
    )
    return i + 5
  }

  private fun smooth(
    shp: RShape,
    curp: RPoint,
    relp: RPoint,
    refp: RPoint,
    strp: RPoint,
    tags: Array<String>,
    i: Int
  ): Int {
    shp.addBezierTo(
      refp.x,
      refp.y,
      PApplet.parseFloat(tags[i]) + relp.x,
      PApplet.parseFloat(tags[i + 1]) + relp.y,
      PApplet.parseFloat(tags[i + 2]) + relp.x,
      PApplet.parseFloat(tags[i + 3]) + relp.y
    )
    curp.setLocation(
      PApplet.parseFloat(tags[i + 2]) + relp.x,
      PApplet.parseFloat(tags[i + 3]) + relp.y
    )
    refp.setLocation(
      2.0f * curp.x - (PApplet.parseFloat(tags[i]) + relp.x),
      2.0f * curp.y - (PApplet.parseFloat(tags[i + 1]) + relp.y)
    )
    return i + 3
  }

  private fun line(
    shp: RShape,
    curp: RPoint,
    relp: RPoint,
    refp: RPoint,
    strp: RPoint,
    tags: Array<String>,
    i: Int
  ): Int {
    shp.addLineTo(
      PApplet.parseFloat(tags[i]) + relp.x,
      PApplet.parseFloat(tags[i + 1]) + relp.y
    )
    curp.setLocation(
      PApplet.parseFloat(tags[i]) + relp.x,
      PApplet.parseFloat(tags[i + 1]) + relp.y
    )
    refp.setLocation(
      curp.x,
      curp.y
    )
    return i + 1
  }

  private fun horizontal(
    shp: RShape,
    curp: RPoint,
    relp: RPoint,
    refp: RPoint,
    strp: RPoint,
    tags: Array<String>,
    i: Int
  ): Int {
    shp.addLineTo(
      PApplet.parseFloat(tags[i]) + relp.x,
      curp.y
    )
    curp.setLocation(
      PApplet.parseFloat(tags[i]) + relp.x,
      curp.y
    )
    refp.setLocation(
      curp.x,
      curp.y
    )
    return i
  }

  private fun vertical(
    shp: RShape,
    curp: RPoint,
    relp: RPoint,
    refp: RPoint,
    strp: RPoint,
    tags: Array<String>,
    i: Int
  ): Int {
    shp.addLineTo(
      curp.x,
      PApplet.parseFloat(tags[i]) + relp.y
    )
    curp.setLocation(
      curp.x,
      PApplet.parseFloat(tags[i]) + relp.y
    )
    refp.setLocation(
      curp.x,
      curp.y
    )
    return i
  }
}
