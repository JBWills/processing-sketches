/*
  Copyright 2004-2008 Ricard Marxer  <email@ricardmarxer.com>
  <p>
  This file is part of Geomerative.
  <p>
  Geomerative is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  <p>
  Geomerative is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  <p>
  You should have received a copy of the GNU General Public License
  along with Geomerative.  If not, see <http://www.gnu.org/licenses/>.
 */
package geomerativefork.src

import org.apache.batik.svggen.font.Font
import org.apache.batik.svggen.font.Glyph
import org.apache.batik.svggen.font.table.CmapFormat
import org.apache.batik.svggen.font.table.SingleSubst
import org.apache.batik.svggen.font.table.Table
import org.apache.batik.svggen.font.table.Table.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.util.*

/**
 * RShape is a reduced interface for creating, holding and drawing text from TrueType Font files. It's a basic interpreter of TrueType fonts enabling to access any String in the form of a group of shapes.  Enabling us in this way to access their geometry.
 *
 * @eexample RFont
 * @usage Geometry
 * @related RGroup
 * @extended
 */
class RFont @JvmOverloads constructor(
  fontPath: String?, size: Int =
    DEFAULT_SIZE, align: Int =
    DEFAULT_ALIGN
) : PConstants {
  /**
   * The point size of the font.
   *
   * @eexample size
   * @related setSize ( )
   * @related RFont
   */
  var size = DEFAULT_SIZE
    set(value) {
      val unitsPerEm = f.headTable.unitsPerEm
      val resolution: Int = RG.dpi
      scaleFactor = value.toFloat() * resolution.toFloat() / (72f * unitsPerEm.toFloat())
      field = value
    }


  /**
   * The alignment of the font. This property can take the following values: RFont.LEFT, RFont.CENTER and RFont.RIGHT
   *
   * @eexample align
   * @related setAlign ( )
   * @related RFont
   */
  var align = DEFAULT_ALIGN
    set(value) {
      if (align != PConstants.LEFT && align != PConstants.CENTER && align != PConstants.RIGHT) {
        throw RuntimeException("Alignment unknown.  The only accepted values are: RFont.LEFT, RFont.CENTER and RFont.RIGHT")
      }
      field = align
    }

  /**
   * Should we try to use ASCII, rather than Unicode?
   */
  var forceAscii = false
  var f: Font
  var scaleFactor = 0.2f

  // More info at:
  //    http://fontforge.sourceforge.net/faq.html#linespace
  //    http://typophile.com/node/13081
  val lineSpacing: Float
    get() {
      // More info at:
      //    http://fontforge.sourceforge.net/faq.html#linespace
      //    http://typophile.com/node/13081
      val unitsPerEm = f.headTable.unitsPerEm
      println("UnitsPerEm (emsize): $unitsPerEm")

      // HHEA table method:
      val hheaLineGap =
        (f.hheaTable.ascender - f.hheaTable.descender + f.hheaTable.lineGap) * scaleFactor
      println("HHEA lineGap: $hheaLineGap")

      // OS2 table typographic line gap method:
      val os2TypoLineGap =
        (f.oS2Table.typoAscender - f.oS2Table.typoDescender + f.oS2Table.typoLineGap) * scaleFactor
      println("Os2 Typo lineGap: $os2TypoLineGap")

      // OS2 table win line gap method:
      val os2WinLineGap = (f.oS2Table.winAscent + f.oS2Table.winDescent) * scaleFactor
      println("Os2 Win lineGap: $os2WinLineGap")

      // Automatic calculation
      val autoLineGap = f.headTable.unitsPerEm * 1.25f * scaleFactor
      println("Automatic lineGap: $autoLineGap")
      return hheaLineGap
    }

  /**
   * @invisible
   */
  val family: String
    get() = f.nameTable.getRecord(Table.nameFontFamilyName)

  /**
   * Use this method to get the outlines of a character in the form of an RShape.
   *
   * @param character char, the character we want the outline from.
   * @return RShape, the outline of the character.
   * @eexample RFont_toShape
   * @related toGroup ( )
   * @related toPolygon ( )
   * @related draw ( )
   */
  fun toShape(character: Char): RShape {
    val grp = toGroup(character.toString())
    return if (grp.elements.isNotEmpty()) grp.elements[0] as RShape else RShape()
  }

  /**
   * Use this method to get the outlines of a character in the form of an RPolygon.
   *
   * @param character char, the character we want the outline from.
   * @return RPolygon, the outline of the character.
   * @eexample RFont_toPolygon
   * @related toGroup ( )
   * @related toShape ( )
   * @related draw ( )
   */
  fun toPolygon(character: Char): RPolygon = toShape(character).toPolygon()

  // We've been asked to use the ASCII/Macintosh cmap format
  private val cmapFormat: CmapFormat?
    get() {
      if (forceAscii) {
        // We've been asked to use the ASCII/Macintosh cmap format
        return f.cmapTable.getCmapFormat(platformMacintosh, encodingRoman)
      }

      val platforms = shortArrayOf(platformMicrosoft, platformAppleUnicode, platformMacintosh)
      val encodings = shortArrayOf(encodingUGL, encodingKorean, encodingHebrew, encodingUndefined)
      for (i in encodings.indices) {
        for (j in platforms.indices) {
          val cmapFmt = f.cmapTable.getCmapFormat(
            platforms[j],
            encodings[i]
          )
          if (cmapFmt != null) {
            return cmapFmt
          }
        }
      }
      return null
    }

  /**
   * Use this method to get the outlines of a string in the form of an RGroup.  All the elements of the group will be RShapes.
   *
   * @param text String, the string we want the outlines from.
   * @return RGroup, the group of outlines of the character.  All the elements are RShapes.
   * @eexample RFont_toGroup
   * @related toShape ( )
   * @related draw ( )
   */
  @Throws(RuntimeException::class)
  fun toGroup(text: String): RGroup {
    val result = RGroup()

    // Decide upon a cmap table to use for our character to glyph look-up
    val cmapFmt = cmapFormat ?: throw RuntimeException("Cannot find a suitable cmap table")

    // If this font includes arabic script, we want to specify
    // substitutions for initial, medial, terminal & isolated
    // cases.
    /*
      GsubTable gsub = (GsubTable) f.getTable(Table.GSUB);
      SingleSubst initialSubst = null;
      SingleSubst medialSubst = null;
      SingleSubst terminalSubst = null;
      if (gsub != null) {
      Script s = gsub.getScriptList().findScript(ScriptTags.SCRIPT_TAG_ARAB);
      if (s != null) {
      LangSys ls = s.getDefaultLangSys();
      if (ls != null) {
      Feature init = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_INIT);
      Feature medi = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_MEDI);
      Feature fina = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_FINA);

      initialSubst = (SingleSubst)
      gsub.getLookupList().getLookup(init, 0).getSubtable(0);
      medialSubst = (SingleSubst)
      gsub.getLookupList().getLookup(medi, 0).getSubtable(0);
      terminalSubst = (SingleSubst)
      gsub.getLookupList().getLookup(fina, 0).getSubtable(0);
      }
      }
      }*/
    var x = 0
    for (i in 0 until text.length) {
      val glyphIndex = cmapFmt.mapCharCode(text[i].toInt())
      val glyph = f.getGlyph(glyphIndex)
      val default_advance_x = f.hmtxTable.getAdvanceWidth(glyphIndex)
      x += if (glyph != null) {
        glyph.scale(scaleFactor.toInt())
        // Add the Glyph to the Shape with an horizontal offset of x
        result.addElement(
          getGlyphAsShape(
            f,
            glyph,
            glyphIndex,
            x
              .toFloat()
          )
        )
        glyph.advanceWidth
      } else {
        (default_advance_x.toFloat() * scaleFactor).toInt()
      }
    }
    if (align != PConstants.LEFT && align != PConstants.CENTER && align != PConstants.RIGHT) {
      throw RuntimeException("Alignment unknown.  The only accepted values are: RFont.LEFT, RFont.CENTER and RFont.RIGHT")
    }
    val r: RRectangle
    val mattrans: RMatrix
    when (align) {
      PConstants.CENTER -> {
        r = result.bounds
        mattrans = RMatrix()
        mattrans.translate(
          (r.minX - r.maxX) / 2, 0f
        )
        result.transform(mattrans)
      }
      PConstants.RIGHT -> {
        r = result.bounds
        mattrans = RMatrix()
        mattrans.translate(
          r.minX - r.maxX, 0f
        )
        result.transform(mattrans)
      }
      PConstants.LEFT -> {
      }
    }
    return result
  }

  @Throws(RuntimeException::class)
  fun toShape(text: String): RShape {
    val result = RShape()

    // Decide upon a cmap table to use for our character to glyph look-up
    val cmapFmt = cmapFormat ?: throw RuntimeException("Cannot find a suitable cmap table")

    // If this font includes arabic script, we want to specify
    // substitutions for initial, medial, terminal & isolated
    // cases.
    /*
      GsubTable gsub = (GsubTable) f.getTable(Table.GSUB);
      SingleSubst initialSubst = null;
      SingleSubst medialSubst = null;
      SingleSubst terminalSubst = null;
      if (gsub != null) {
      Script s = gsub.getScriptList().findScript(ScriptTags.SCRIPT_TAG_ARAB);
      if (s != null) {
      LangSys ls = s.getDefaultLangSys();
      if (ls != null) {
      Feature init = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_INIT);
      Feature medi = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_MEDI);
      Feature fina = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_FINA);

      initialSubst = (SingleSubst)
      gsub.getLookupList().getLookup(init, 0).getSubtable(0);
      medialSubst = (SingleSubst)
      gsub.getLookupList().getLookup(medi, 0).getSubtable(0);
      terminalSubst = (SingleSubst)
      gsub.getLookupList().getLookup(fina, 0).getSubtable(0);
      }
      }
      }*/
    var x = 0
    for (i in 0 until text.length) {
      val glyphIndex = cmapFmt.mapCharCode(text[i].toInt())
      val glyph = f.getGlyph(glyphIndex)
      val default_advance_x = f.hmtxTable.getAdvanceWidth(glyphIndex)
      x += if (glyph != null) {
        glyph.scale(scaleFactor.toInt())
        // Add the Glyph to the Shape with an horizontal offset of x
        result.addChild(
          getGlyphAsShape(
            f,
            glyph,
            glyphIndex,
            x
              .toFloat()
          )
        )
        glyph.advanceWidth
      } else {
        (default_advance_x.toFloat() * scaleFactor).toInt()
      }
    }
    if (align != PConstants.LEFT && align != PConstants.CENTER && align != PConstants.RIGHT) {
      throw RuntimeException("Alignment unknown.  The only accepted values are: RFont.LEFT, RFont.CENTER and RFont.RIGHT")
    }
    val r: RRectangle
    val mattrans: RMatrix
    when (align) {
      PConstants.CENTER -> {
        r = result.bounds
        mattrans = RMatrix()
        mattrans.translate(
          (r.minX - r.maxX) / 2, 0f
        )
        result.transform(mattrans)
      }
      PConstants.RIGHT -> {
        r = result.bounds
        mattrans = RMatrix()
        mattrans.translate(
          r.minX - r.maxX, 0f
        )
        result.transform(mattrans)
      }
      PConstants.LEFT -> {
      }
    }
    return result
  }

  /**
   * Use this method to draw a character on a certain canvas.
   *
   * @param character the character to be drawn
   * @param g         the canvas where to draw
   * @eexample RFont_draw
   * @related toShape ( )
   * @related toGroup ( )
   */
  @Throws(RuntimeException::class)
  fun draw(character: Char, g: PGraphics?) {
    this.toShape(character).draw(g!!)
  }

  /**
   * Use this method to draw a character on a certain canvas.
   *
   * @param text the string to be drawn
   * @param g    the canvas where to draw
   * @eexample RFont_draw
   * @related toShape ( )
   * @related toGroup ( )
   */
  @Throws(RuntimeException::class)
  fun draw(text: String, g: PGraphics?) {
    toGroup(text).draw(g!!)
  }

  /**
   * Use this method to draw a character on a certain canvas.
   *
   * @param character char, the character to be drawn
   * @param g         the canvas where to draw
   * @eexample RFont_draw
   * @related toShape ( )
   * @related toGroup ( )
   */
  @Throws(RuntimeException::class)
  fun draw(character: Char, g: PApplet?) {
    this.toShape(character).draw(g!!)
  }

  /**
   * Use this method to draw a character on a certain canvas.
   *
   * @param text the string to be drawn
   * @param g    the canvas where to draw
   * @eexample RFont_draw
   * @related toShape ( )
   * @related toGroup ( )
   */
  @Throws(RuntimeException::class)
  fun draw(text: String, g: PApplet?) {
    toGroup(text).draw(g!!)
  }

  @Throws(RuntimeException::class)
  fun draw(text: String) {
    toGroup(text).draw()
  }

  @Throws(RuntimeException::class)
  fun draw(character: Char) {
    this.toShape(character).draw()
  }

  companion object {
    const val DEFAULT_SIZE = 48
    const val DEFAULT_RESOLUTION = 72

    //int scaleFactorFixed = 1;
    const val DEFAULT_ALIGN = PConstants.LEFT
    private fun midValue(a: Float, b: Float): Float {
      return a + (b - a) / 2
    }

    protected fun getContourAsShape(glyph: Glyph, startIndex: Int, count: Int): RShape {
      return getContourAsShape(
        glyph,
        startIndex,
        count, 0f
      )
    }

    protected fun getContourAsShape(
      glyph: Glyph,
      startIndex: Int,
      count: Int,
      xadv: Float
    ): RShape {

      // If this is a single point on it's own, weSystem.out.println("Value of pointx: "+pointx); can't do anything with it
      if (glyph.getPoint(startIndex).endOfContour) {
        return RShape()
      }
      val result = RShape()
      var offset = 0
      //float originx = 0F,originy = 0F;
      while (offset < count) {
        val point = glyph.getPoint(startIndex + offset % count)
        val point_plus1 = glyph.getPoint(startIndex + (offset + 1) % count)
        val point_plus2 = glyph.getPoint(startIndex + (offset + 2) % count)
        val pointx = point.x.toFloat() + xadv
        val pointy = point.y.toFloat()
        val point_plus1x = point_plus1.x.toFloat() + xadv
        val point_plus1y = point_plus1.y.toFloat()
        val point_plus2x = point_plus2.x.toFloat() + xadv
        val point_plus2y = point_plus2.y.toFloat()
        if (offset == 0) {
          // move command
          result.addMoveTo(
            pointx,
            pointy
          )
        }
        if (point.onCurve && point_plus1.onCurve) {
          // line command
          result.addLineTo(
            point_plus1x,
            point_plus1y
          )
          offset++
        } else if (point.onCurve && !point_plus1.onCurve && point_plus2.onCurve) {
          // This is a curve with no implied points
          // quadratic bezier command
          result.addQuadTo(
            point_plus1x,
            point_plus1y,
            point_plus2x,
            point_plus2y
          )
          offset += 2
        } else if (point.onCurve && !point_plus1.onCurve && !point_plus2.onCurve) {
          // This is a curve with one implied point
          // quadratic bezier command avec le endPoint implicit
          result.addQuadTo(
            point_plus1x,
            point_plus1y,
            midValue(
              point_plus1x,
              point_plus2x
            ),
            midValue(
              point_plus1y,
              point_plus2y
            )
          )
          offset += 2
        } else if (!point.onCurve && !point_plus1.onCurve) {
          // This is a curve with two implied points
          // quadratic bezier with
          result.addQuadTo(
            pointx,
            pointy,
            midValue(
              pointx,
              point_plus1x
            ),
            midValue(
              pointy,
              point_plus1y
            )
          )
          offset++
        } else if (!point.onCurve && point_plus1.onCurve) {
          // This is a curve with no implied points
          result.addQuadTo(
            pointx,
            pointy,
            point_plus1x,
            point_plus1y
          )
          offset++
        } else {
          println("drawGlyph case not catered for!!")
          break
        }
      }
      result.addClose()
      return result
    }

    protected fun getGlyphAsShape(font: Font?, glyph: Glyph?, glyphIndex: Int): RShape {
      return getGlyphAsShape(
        font,
        glyph,
        glyphIndex, 0f
      )
    }

    protected fun getGlyphAsShape(
      font: Font?,
      glyph: Glyph?,
      glyphIndex: Int,
      xadv: Float
    ): RShape {
      val result = RShape()
      var firstIndex = 0
      var count = 0
      var i: Int
      if (glyph != null) {
        i = 0
        while (i < glyph.pointCount) {
          count++
          if (glyph.getPoint(i).endOfContour) {
            result.addShape(
              getContourAsShape(
                glyph,
                firstIndex,
                count,
                xadv
              )
            )
            firstIndex = i + 1
            count = 0
          }
          i++
        }
      }
      return result
    }

    protected fun getGlyphAsShape(
      font: Font,
      glyph: Glyph?,
      glyphIndex: Int,
      arabInitSubst: SingleSubst?,
      arabMediSubst: SingleSubst?,
      arabTermSubst: SingleSubst?
    ): RShape {
      return getGlyphAsShape(
        font,
        glyph,
        glyphIndex,
        arabInitSubst,
        arabMediSubst,
        arabTermSubst, 0f
      )
    }

    protected fun getGlyphAsShape(
      font: Font,
      glyph: Glyph?,
      glyphIndex: Int,
      arabInitSubst: SingleSubst?,
      arabMediSubst: SingleSubst?,
      arabTermSubst: SingleSubst?,
      xadv: Float
    ): RShape {
      val result = RShape()
      var substituted = false

      // arabic = "initial | medial | terminal | isolated"
      var arabInitGlyphIndex = glyphIndex
      var arabMediGlyphIndex = glyphIndex
      var arabTermGlyphIndex = glyphIndex
      if (arabInitSubst != null) {
        arabInitGlyphIndex = arabInitSubst.substitute(glyphIndex)
      }
      if (arabMediSubst != null) {
        arabMediGlyphIndex = arabMediSubst.substitute(glyphIndex)
      }
      if (arabTermSubst != null) {
        arabTermGlyphIndex = arabTermSubst.substitute(glyphIndex)
      }
      if (arabInitGlyphIndex != glyphIndex) {
        result.addShape(
          getGlyphAsShape(
            font,
            font.getGlyph(arabInitGlyphIndex),
            arabInitGlyphIndex
          )
        )
        substituted = true
      }
      if (arabMediGlyphIndex != glyphIndex) {
        result.addShape(
          getGlyphAsShape(
            font,
            font.getGlyph(arabMediGlyphIndex),
            arabMediGlyphIndex
          )
        )
        substituted = true
      }
      if (arabTermGlyphIndex != glyphIndex) {
        result.addShape(
          getGlyphAsShape(
            font,
            font.getGlyph(arabTermGlyphIndex),
            arabTermGlyphIndex
          )
        )
        substituted = true
      }
      if (substituted) {
        result.addShape(
          getGlyphAsShape(
            font,
            glyph,
            glyphIndex
          )
        )
      } else {
        result.addShape(
          getGlyphAsShape(
            font,
            glyph,
            glyphIndex
          )
        )
      }
      return result
    }
  }

  /**
   * The constructor of the RFont object.  Use this in order to create a font with which we will be able to draw and obtain outlines of text.
   *
   * @param fontPath String, the name of the TrueType Font file which should be situated in the data folder of the sketch.
   * @param size     int, the point size of the font in points.
   * @param align    int, this can only take the following values: RFont.LEFT, RFont.CENTER and RFont.RIGHT.
   * @eexample RFont
   * @related toGroup ( )
   * @related toShape ( )
   * @related toPolygon ( )
   * @related toMesh ( )
   * @related draw ( )
   */
  init {
    // Try to find the font as font path
    val bs = RG.parent().loadBytes(fontPath)
    f = Font.create(Arrays.toString(bs))
    this.size = size
    this.align = align
  }
}
