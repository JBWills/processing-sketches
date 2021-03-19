/*
 * The SEI Software Open Source License, Version 1.0
 *
 * Copyright (c) 2004, Solution Engineering, Inc.
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Solution Engineering, Inc. (http://www.seisw.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 3. The name "Solution Engineering" must not be used to endorse or
 *    promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    admin@seisw.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SOLUTION ENGINEERING, INC. OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
@file:Suppress("unused", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "SameParameterValue")

package geomerativefork.src

import geomerativefork.src.RClip.HState.NoHorizontalEdge
import geomerativefork.src.RClip.HState.nextHState
import geomerativefork.src.RClip.VertexType.ExternalLeftIntermediate
import geomerativefork.src.RClip.VertexType.ExternalMaximum
import geomerativefork.src.RClip.VertexType.ExternalMin
import geomerativefork.src.RClip.VertexType.ExternalMinAndMax
import geomerativefork.src.RClip.VertexType.ExternalRightIntermediate
import geomerativefork.src.RClip.VertexType.InternalLeftIntermediate
import geomerativefork.src.RClip.VertexType.InternalMaximum
import geomerativefork.src.RClip.VertexType.InternalMin
import geomerativefork.src.RClip.VertexType.InternalMinAndMax
import geomerativefork.src.RClip.VertexType.InternalRightIntermediate
import geomerativefork.src.RClip.VertexType.LeftEdge
import geomerativefork.src.RClip.VertexType.RightEdge
import geomerativefork.src.util.mapArray
import kotlin.math.abs

/**
 * `Clip` is a Java version of the *General RPolygon Clipper* algorithm
 * developed by Alan Murta (gpc@cs.man.ac.uk).  The home page for the original source can be
 * found at [
 * http://www.cs.man.ac.uk/aig/staff/alan/software/](http://www.cs.man.ac.uk/aig/staff/alan/software/).
 *
 *
 * **`polyClass:`** Some of the public methods below take a `polyClass`
 * argument.  This `java.lang.Class` object is assumed to implement the `RPolygon`
 * interface and have a no argument constructor.  This was done so that the user of the algorithm
 * could create their own classes that implement the `RPolygon` interface and still uses
 * this algorithm.
 *
 *
 * **Implementation Note:** The converted algorithm does support the *difference*
 * operation, but a public method has not been provided and it has not been tested.  To do so,
 * simply follow what has been done for *intersection*.
 *
 * @author Dan Bridenbecker, Solution Engineering, Inc.
 */
internal object RClip {
  // -----------------
  // --- Constants ---
  // -----------------
  private const val DEBUG = false

  // Maximum precision for floats
  private const val GPC_EPSILON = 2.2204460492503131e-016

  //private static final float GPC_EPSILON = 1.192092896e-07F;
  const val GPC_VERSION = "2.31"
  private const val LEFT = 0
  private const val RIGHT = 1
  private const val ABOVE = 0
  private const val BELOW = 1
  private const val CLIP = 0
  private const val SUBJ = 1
  private const val INVERT_TRISTRIPS = false
  // ----------------------
  // --- Static Methods ---
  // ----------------------
  /**
   * Return the intersection of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <code>polyClass.
   *
   * @param p1        One of the polygons to perform the intersection with
   * @param p2        One of the polygons to perform the intersection with
   * @param polyClass The type of `RPolygon` to return
  </code> */
  fun intersection(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_INT, p1, p2, polyClass)

  /**
   * Return the union of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <code>polyClass.
   *
   * @param p1        One of the polygons to perform the union with
   * @param p2        One of the polygons to perform the union with
   * @param polyClass The type of `RPolygon` to return
  </code> */
  fun union(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_UNION, p1, p2, polyClass)

  /**
   * Return the xor of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <code>polyClass.
   *
   * @param p1        One of the polygons to perform the xor with
   * @param p2        One of the polygons to perform the xor with
   * @param polyClass The type of `RPolygon` to return
  </code> */
  fun xor(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_XOR, p1, p2, polyClass)

  /**
   * Return the diff of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <code>polyClass.
   *
   * @param p1        One of the polygons to perform the diff with
   * @param p2        One of the polygons to perform the diff with
   * @param polyClass The type of `RPolygon` to return
  </code> */
  fun diff(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_DIFF, p1, p2, polyClass)

  /**
   * Return the intersection of `p1` and `p2` where the
   * return type is of `PolyDefault`.
   *
   * @param p1 One of the polygons to perform the intersection with
   * @param p2 One of the polygons to perform the intersection with
   */
  @JvmStatic
  fun intersection(p1: RPolygon, p2: RPolygon): RPolygon =
    clip(OperationType.GPC_INT, p1, p2, RPolygon::class.java)

  /**
   * Return the union of `p1` and `p2` where the
   * return type is of `PolyDefault`.
   *
   * @param p1 One of the polygons to perform the union with
   * @param p2 One of the polygons to perform the union with
   */
  @JvmStatic
  fun union(p1: RPolygon, p2: RPolygon): RPolygon =
    clip(OperationType.GPC_UNION, p1, p2, RPolygon::class.java)

  /**
   * Return the xor of `p1` and `p2` where the
   * return type is of `PolyDefault`.
   *
   * @param p1 One of the polygons to perform the xor with
   * @param p2 One of the polygons to perform the xor with
   */
  @JvmStatic
  fun xor(p1: RPolygon, p2: RPolygon): RPolygon =
    clip(OperationType.GPC_XOR, p1, p2, RPolygon::class.java)

  /**
   * Return the diff of `p1` and `p2` where the
   * return type is of `PolyDefault`.
   *
   * @param p1 One of the polygons to perform the diff with
   * @param p2 One of the polygons to perform the diff with
   */
  @JvmStatic
  fun diff(p1: RPolygon, p2: RPolygon): RPolygon =
    clip(OperationType.GPC_DIFF, p1, p2, RPolygon::class.java)

  /**
   * Updates `p1`.
   *
   * @param p1 One of the polygons to perform the diff with
   */
  @JvmStatic
  fun update(p1: RPolygon): RPolygon =
    clip(OperationType.GPC_DIFF, p1, RPolygon(), RPolygon::class.java)

  // -----------------------
  // --- Private Methods ---
  // -----------------------
  /**
   * Create a new `RPolygon` type object using `polyClass`.
   */
  private fun createNewPoly(polyClass: Class<*>): RPolygon = try {
    polyClass.newInstance() as RPolygon
  } catch (e: Exception) {
    throw RuntimeException(e)
  }

  /**
   * `clip()` is the main method of the clipper algorithm.
   * This is where the conversion from really begins.
   */
  private fun clip(
    op: OperationType,
    subj: RPolygon,
    clip: RPolygon,
    polyClass: Class<*>,
  ): RPolygon {
    if (RG.useFastClip) return FastRClip.clip(op, subj, clip, polyClass)

    val result = createNewPoly(polyClass)

    /* Test for trivial NULL result cases */
    if (subj.isEmpty &&
      clip.isEmpty ||
      subj.isEmpty &&
      (op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) ||
      clip.isEmpty &&
      op === OperationType.GPC_INT
    ) return result

    /* Identify potentially contributing contours */
    if ((op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) && !subj.isEmpty && !clip.isEmpty) {
      minimaxTest(subj, clip, op)
    }

    /* Build LMT */
    val lmtTable = LmtTable()
    val scanBeanEntries = ScanBeamTreeEntries()
    if (!subj.isEmpty) {
      buildLmt(lmtTable, scanBeanEntries, subj, SUBJ, op)
    }

    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for subj ---------")
      lmtTable.print()
    }

    if (!clip.isEmpty) buildLmt(lmtTable, scanBeanEntries, clip, CLIP, op)

    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for clip ---------")
      lmtTable.print()
    }

    /* Return a NULL result if no contours contribute */
    if (lmtTable.topNode == null) return result

    /* Build scanbeam table from scanbeam tree */
    val sbt = scanBeanEntries.buildSbt()
    val parity = arrayOf(LEFT, LEFT)

    /* Invert clip polygon for difference operation */
    if (op === OperationType.GPC_DIFF) parity[CLIP] = RIGHT
    if (DEBUG) printSbt(sbt)
    var localMin = lmtTable.topNode
    val outPoly = TopPolygonNode() // used to create resulting RPolygon
    val aet = AetTree()
    var scanbeam = 0

    /* Process each scanbeam */
    while (scanbeam < sbt.size) {
      /* Set yb and yt to the bottom and top of the scanbeam */
      val yb = sbt[scanbeam++]
      var yt = 0.0f
      var dy = 0.0f
      if (scanbeam < sbt.size) {
        yt = sbt[scanbeam]
        dy = yt - yb
      }

      /* === SCANBEAM BOUNDARY PROCESSING ================================ */

      /* If LMT node corresponding to yb exists */
      if (localMin != null && localMin.y == yb) {
        /* Add edges starting at this local minimum to the AET */
        localMin.firstBound?.walkBound { edge -> addEdgeToAet(aet, edge) }
        localMin = localMin.next
      }

      if (DEBUG) aet.print()

      /* Set dummy previous x value */
      var px = -Float.MAX_VALUE

      /* Create bundles within AET */
      val nonNullTopNode = aet.topNode!!
      var e0 = nonNullTopNode
      var e1 = nonNullTopNode

      /* Set up bundle fields of first edge */
      nonNullTopNode.bundle[ABOVE][nonNullTopNode.type] = if (nonNullTopNode.top.y != yb) 1 else 0
      nonNullTopNode.bundle[ABOVE][if (nonNullTopNode.type == 0) 1 else 0] = 0
      nonNullTopNode.edgeBundleState[ABOVE] = BundleState.UNBUNDLED
      nonNullTopNode.walk { nextTopEdge ->
        val neType = nextTopEdge.type
        val neTypeOpp = if (nextTopEdge.type == 0) 1 else 0 //next edge type opposite

        /* Set up bundle fields of next edge */
        nextTopEdge.bundle[ABOVE][neType] = if (nextTopEdge.top.y != yb) 1 else 0
        nextTopEdge.bundle[ABOVE][neTypeOpp] = 0
        nextTopEdge.edgeBundleState[ABOVE] = BundleState.UNBUNDLED

        /* Bundle edges above the scanbeam boundary if they coincide */
        if (nextTopEdge.bundle[ABOVE][neType] == 1) {
          if (eq(e0.scanBottomX, nextTopEdge.scanBottomX) && eq(
              e0.scanUnitDeltaX,
              nextTopEdge.scanUnitDeltaX
            ) && e0.top.y != yb
          ) {
            nextTopEdge.bundle[ABOVE][neType] =
              nextTopEdge.bundle[ABOVE][neType] xor e0.bundle[ABOVE][neType]
            nextTopEdge.bundle[ABOVE][neTypeOpp] = e0.bundle[ABOVE][neTypeOpp]
            nextTopEdge.edgeBundleState[ABOVE] = BundleState.BUNDLE_HEAD
            e0.bundle[ABOVE][CLIP] = 0
            e0.bundle[ABOVE][SUBJ] = 0
            e0.edgeBundleState[ABOVE] = BundleState.BUNDLE_TAIL
          }
          e0 = nextTopEdge
        }
      }
      val horiz = arrayOf(NoHorizontalEdge, NoHorizontalEdge)
      val exists = arrayOf(0, 0)
      var cf: PolygonNode? = null

      /* Process each edge at this scanbeam boundary */
      aet.topNode?.walk { e ->
        exists[CLIP] = e.bundle[ABOVE][CLIP] + (e.bundle[BELOW][CLIP] shl 1)
        exists[SUBJ] = e.bundle[ABOVE][SUBJ] + (e.bundle[BELOW][SUBJ] shl 1)
        if (exists[CLIP] != 0 || exists[SUBJ] != 0) {
          /* Set bundle side */
          e.bundleSideIndicators[CLIP] = parity[CLIP]
          e.bundleSideIndicators[SUBJ] = parity[SUBJ]
          val contributing: Boolean
          val br: Int
          val bl: Int
          val tr: Int
          val tl: Int
          /* Determine contributing status and quadrant occupancies */
          if (op === OperationType.GPC_DIFF || op === OperationType.GPC_INT) {
            contributing =
              exists[CLIP] != 0 && (parity[SUBJ] != 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] != 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
            br = if (parity[CLIP] != 0 && parity[SUBJ] != 0) 1 else 0
            bl =
              if (parity[CLIP] xor e.bundle[ABOVE][CLIP] != 0 && parity[SUBJ] xor e.bundle[ABOVE][SUBJ] != 0) 1 else 0
            tr =
              if ((parity[CLIP] xor if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) != 0 && (parity[SUBJ] xor if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) != 0) 1 else 0
            tl =
              if (parity[CLIP] xor (if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) xor e.bundle[BELOW][CLIP] != 0 && parity[SUBJ] xor (if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) xor e.bundle[BELOW][SUBJ] != 0) 1 else 0
          } else if (op === OperationType.GPC_XOR) {
            contributing = exists[CLIP] != 0 || exists[SUBJ] != 0
            br = parity[CLIP] xor parity[SUBJ]
            bl =
              parity[CLIP] xor e.bundle[ABOVE][CLIP] xor (parity[SUBJ] xor e.bundle[ABOVE][SUBJ])
            tr =
              parity[CLIP] xor if (horiz[CLIP] != NoHorizontalEdge) 1 else 0 xor (parity[SUBJ] xor if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0)
            tl =
              (parity[CLIP] xor (if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) xor e.bundle[BELOW][CLIP] xor (parity[SUBJ] xor (if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) xor e.bundle[BELOW][SUBJ]))
          } else if (op === OperationType.GPC_UNION) {
            contributing =
              exists[CLIP] != 0 && (parity[SUBJ] == 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] == 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
            br = if (parity[CLIP] != 0 || parity[SUBJ] != 0) 1 else 0
            bl =
              if (parity[CLIP] xor e.bundle[ABOVE][CLIP] != 0 || parity[SUBJ] xor e.bundle[ABOVE][SUBJ] != 0) 1 else 0
            tr =
              if ((parity[CLIP] xor if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) != 0 || (parity[SUBJ] xor if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) != 0) 1 else 0
            tl =
              if (parity[CLIP] xor (if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) xor e.bundle[BELOW][CLIP] != 0 || parity[SUBJ] xor (if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) xor e.bundle[BELOW][SUBJ] != 0) 1 else 0
          } else {
            throw IllegalStateException("Unknown op")
          }

          /* Update parity */
          parity[CLIP] = parity[CLIP] xor e.bundle[ABOVE][CLIP]
          parity[SUBJ] = parity[SUBJ] xor e.bundle[ABOVE][SUBJ]

          /* Update horizontal state */
          if (exists[CLIP] != 0) {
            horiz[CLIP] = nextHState[horiz[CLIP]][(exists[CLIP] - 1 shl 1) + parity[CLIP]]
          }

          if (exists[SUBJ] != 0) {
            horiz[SUBJ] = nextHState[horiz[SUBJ]][(exists[SUBJ] - 1 shl 1) + parity[SUBJ]]
          }

          if (contributing) {
            val xb = e.scanBottomX
            when (VertexType.getType(tr, tl, br, bl)) {
              ExternalMin, InternalMin -> {
                e.outputPoly[ABOVE] = outPoly.addLocalMin(xb, yb)
                px = xb
                cf = e.outputPoly[ABOVE]
              }
              ExternalRightIntermediate -> {
                if (xb != px) {
                  cf!!.addRight(xb, yb)
                  px = xb
                }
                e.outputPoly[ABOVE] = cf
                cf = null
              }
              ExternalLeftIntermediate -> {
                e.outputPoly[BELOW]!!.addLeft(xb, yb)
                px = xb
                cf = e.outputPoly[BELOW]
              }
              ExternalMaximum -> run {
                val cfNonNull = cf ?: return@run
                if (xb != px) {
                  cfNonNull.addLeft(xb, yb)
                  px = xb
                }
                outPoly.mergeRight(cfNonNull, e.outputPoly[BELOW]!!)
                cf = null
              }
              InternalLeftIntermediate -> {
                if (xb != px) {
                  cf!!.addLeft(xb, yb)
                  px = xb
                }
                e.outputPoly[ABOVE] = cf
                cf = null
              }
              InternalRightIntermediate -> {
                e.outputPoly[BELOW]!!.addRight(xb, yb)
                px = xb
                cf = e.outputPoly[BELOW]
                e.outputPoly[BELOW] = null
              }
              InternalMaximum -> {
                if (xb != px) {
                  cf!!.addRight(xb, yb)
                  px = xb
                }
                outPoly.mergeLeft(cf!!, e.outputPoly[BELOW]!!)
                cf = null
                e.outputPoly[BELOW] = null
              }
              InternalMinAndMax -> {
                if (xb != px) {
                  cf!!.addRight(xb, yb)
                  px = xb
                }
                outPoly.mergeLeft(cf!!, e.outputPoly[BELOW]!!)
                e.outputPoly[BELOW] = null
                e.outputPoly[ABOVE] = outPoly.addLocalMin(xb, yb)
                cf = e.outputPoly[ABOVE]
              }
              ExternalMinAndMax -> run {
                val cfNonNull = cf ?: return@run
                if (xb != px) {
                  cfNonNull.addLeft(xb, yb)
                  px = xb
                }
                outPoly.mergeRight(cfNonNull, e.outputPoly[BELOW]!!)
                e.outputPoly[BELOW] = null
                e.outputPoly[ABOVE] = outPoly.addLocalMin(xb, yb)
                cf = e.outputPoly[ABOVE]
              }
              LeftEdge -> {
                if (e.bot.y == yb) e.outputPoly[BELOW]?.addLeft(xb, yb)
                e.outputPoly[ABOVE] = e.outputPoly[BELOW]
                px = xb
              }
              RightEdge -> {
                if (e.bot.y == yb) e.outputPoly[BELOW]?.addRight(xb, yb)
                e.outputPoly[ABOVE] = e.outputPoly[BELOW]
                px = xb
              }
              else -> {
              }
            }
          } /* End of contributing conditional */
        } /* End of edge exists conditional */
        if (DEBUG) outPoly.print()
      }

      /* Delete terminating edges from the AET, otherwise compute xt */
      aet.topNode?.walk { edge ->
        if (edge.top.y == yb) {
          val prevEdge = edge.prev
          val nextEdge = edge.next
          if (prevEdge != null) prevEdge.next = nextEdge else aet.topNode = nextEdge
          if (nextEdge != null) nextEdge.prev = prevEdge

          /* Copy bundle head state to the adjacent tail edge if required */
          if (edge.edgeBundleState[BELOW] === BundleState.BUNDLE_HEAD && prevEdge != null) {
            if (prevEdge.edgeBundleState[BELOW] === BundleState.BUNDLE_TAIL) {
              prevEdge.outputPoly[BELOW] = edge.outputPoly[BELOW]
              prevEdge.edgeBundleState[BELOW] = BundleState.UNBUNDLED
              if (prevEdge.prev?.edgeBundleState?.get(BELOW) === BundleState.BUNDLE_TAIL) {
                prevEdge.edgeBundleState[BELOW] = BundleState.BUNDLE_HEAD
              }
            }
          }
        } else {
          edge.scanTopX = if (edge.top.y == yt) edge.top.x
          else edge.bot.x + edge.scanUnitDeltaX * (yt - edge.bot.y)
        }
      }

      if (scanbeam < scanBeanEntries.sbtEntries) {
        /* === SCANBEAM INTERIOR PROCESSING ============================== */

        /* Build intersection table for the current scanbeam */
        val itTable = ItNodeTable()
        itTable.buildIntersectionTable(aet, dy)

        /* Process each node in the intersection table */
        itTable.topNode?.walk { intersect ->
          e0 = intersect.ie[0]
          e1 = intersect.ie[1]

          /* Only generate output for contributing intersections */
          if ((e0.bundle[ABOVE][CLIP] != 0 || e0.bundle[ABOVE][SUBJ] != 0) &&
            (e1.bundle[ABOVE][CLIP] != 0 || e1.bundle[ABOVE][SUBJ] != 0)
          ) {
            val p = e0.outputPoly[ABOVE]
            val q = e1.outputPoly[ABOVE]
            val ix = intersect.point.x
            val iy = intersect.point.y + yb
            val inClip =
              if (e0.bundle[ABOVE][CLIP] != 0 &&
                e0.bundleSideIndicators[CLIP] == 0 ||
                e1.bundle[ABOVE][CLIP] != 0 &&
                e1.bundleSideIndicators[CLIP] != 0 ||
                e0.bundle[ABOVE][CLIP] == 0 &&
                e1.bundle[ABOVE][CLIP] == 0 &&
                e0.bundleSideIndicators[CLIP] != 0 &&
                e1.bundleSideIndicators[CLIP] != 0
              ) 1 else 0
            val inSubj =
              if (e0.bundle[ABOVE][SUBJ] != 0 &&
                e0.bundleSideIndicators[SUBJ] == 0 ||
                e1.bundle[ABOVE][SUBJ] != 0 &&
                e1.bundleSideIndicators[SUBJ] != 0 ||
                e0.bundle[ABOVE][SUBJ] == 0 &&
                e1.bundle[ABOVE][SUBJ] == 0 &&
                e0.bundleSideIndicators[SUBJ] != 0 &&
                e1.bundleSideIndicators[SUBJ] != 0
              ) 1 else 0
            val tr: Int
            val tl: Int
            val br: Int
            val bl: Int
            /* Determine quadrant occupancies */
            when (op) {
              OperationType.GPC_DIFF, OperationType.GPC_INT -> {
                tr = if (inClip != 0 && inSubj != 0) 1 else 0
                tl =
                  if (inClip xor e1.bundle[ABOVE][CLIP] != 0 && inSubj xor e1.bundle[ABOVE][SUBJ] != 0) 1 else 0
                br =
                  if (inClip xor e0.bundle[ABOVE][CLIP] != 0 && inSubj xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
                bl =
                  if (inClip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] != 0 && inSubj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
              }
              OperationType.GPC_XOR -> {
                tr = inClip xor inSubj
                tl = inClip xor e1.bundle[ABOVE][CLIP] xor (inSubj xor e1.bundle[ABOVE][SUBJ])
                br = inClip xor e0.bundle[ABOVE][CLIP] xor (inSubj xor e0.bundle[ABOVE][SUBJ])
                bl =
                  (inClip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] xor (inSubj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ]))
              }
              OperationType.GPC_UNION -> {
                tr = if (inClip != 0 || inSubj != 0) 1 else 0
                tl =
                  if (inClip xor e1.bundle[ABOVE][CLIP] != 0 || inSubj xor e1.bundle[ABOVE][SUBJ] != 0) 1 else 0
                br =
                  if (inClip xor e0.bundle[ABOVE][CLIP] != 0 || inSubj xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
                bl =
                  if (inClip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] != 0 || inSubj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
              }
              else -> {
                throw IllegalStateException("Unknown op type, $op")
              }
            }

            when (VertexType.getType(tr, tl, br, bl)) {
              ExternalMin -> {
                e0.outputPoly[ABOVE] = outPoly.addLocalMin(ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
              }
              ExternalRightIntermediate -> if (p != null) {
                p.addRight(ix, iy)
                e1.outputPoly[ABOVE] = p
                e0.outputPoly[ABOVE] = null
              }
              ExternalLeftIntermediate -> if (q != null) {
                q.addLeft(ix, iy)
                e0.outputPoly[ABOVE] = q
                e1.outputPoly[ABOVE] = null
              }
              ExternalMaximum -> if (p != null && q != null) {
                p.addLeft(ix, iy)
                outPoly.mergeRight(p, q)
                e0.outputPoly[ABOVE] = null
                e1.outputPoly[ABOVE] = null
              }
              InternalMin -> {
                e0.outputPoly[ABOVE] = outPoly.addLocalMin(ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
              }
              InternalLeftIntermediate -> p?.let {
                p.addLeft(ix, iy)
                e1.outputPoly[ABOVE] = p
                e0.outputPoly[ABOVE] = null
              }
              InternalRightIntermediate -> q?.let {
                q.addRight(ix, iy)
                e0.outputPoly[ABOVE] = q
                e1.outputPoly[ABOVE] = null
              }
              InternalMaximum -> if (p != null && q != null) {
                p.addRight(ix, iy)
                outPoly.mergeLeft(p, q)
                e0.outputPoly[ABOVE] = null
                e1.outputPoly[ABOVE] = null
              }
              InternalMinAndMax -> if (p != null && q != null) {
                p.addRight(ix, iy)
                outPoly.mergeLeft(p, q)
                e0.outputPoly[ABOVE] = outPoly.addLocalMin(ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
              }
              ExternalMinAndMax -> if (p != null && q != null) {
                p.addLeft(ix, iy)
                outPoly.mergeRight(p, q)
                e0.outputPoly[ABOVE] = outPoly.addLocalMin(ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
              }
              else -> {
              }
            }
          } /* End of contributing intersection conditional */

          /* Swap bundle sides in response to edge crossing */
          if (e0.bundle[ABOVE][CLIP] != 0) e1.bundleSideIndicators[CLIP] =
            if (e1.bundleSideIndicators[CLIP] == 0) 1 else 0
          if (e1.bundle[ABOVE][CLIP] != 0) e0.bundleSideIndicators[CLIP] =
            if (e0.bundleSideIndicators[CLIP] == 0) 1 else 0
          if (e0.bundle[ABOVE][SUBJ] != 0) e1.bundleSideIndicators[SUBJ] =
            if (e1.bundleSideIndicators[SUBJ] == 0) 1 else 0
          if (e1.bundle[ABOVE][SUBJ] != 0) e0.bundleSideIndicators[SUBJ] =
            if (e0.bundleSideIndicators[SUBJ] == 0) 1 else 0

          /* Swap e0 and e1 bundles in the AET */
          var prevEdge = e0.prev
          val nextEdge = e1.next
          if (nextEdge != null) {
            nextEdge.prev = e0
          }

          if (e0.edgeBundleState[ABOVE] === BundleState.BUNDLE_HEAD) {
            var search = true
            while (search) {
              prevEdge = prevEdge!!.prev
              if (prevEdge != null) {
                if (prevEdge.edgeBundleState[ABOVE] !== BundleState.BUNDLE_TAIL) {
                  search = false
                }
              } else {
                search = false
              }
            }
          }

          if (prevEdge == null) {
            aet.topNode!!.prev = e1
            e1.next = aet.topNode
            aet.topNode = e0.next
          } else {
            prevEdge.next!!.prev = e1
            e1.next = prevEdge.next
            prevEdge.next = e0.next
          }
          e0.next!!.prev = prevEdge
          e1.next!!.prev = e1
          e0.next = nextEdge
          if (DEBUG) outPoly.print()
        }

        /* Prepare for next scanbeam */
        aet.topNode?.walk { e ->
          val nextEdge = e.next
          val succEdge = e.upperEdge
          if (e.top.y == yt && succEdge != null) {
            /* Replace AET edge by its successor */
            succEdge.outputPoly[BELOW] = e.outputPoly[ABOVE]
            succEdge.edgeBundleState[BELOW] = e.edgeBundleState[ABOVE]
            succEdge.bundle[BELOW][CLIP] = e.bundle[ABOVE][CLIP]
            succEdge.bundle[BELOW][SUBJ] = e.bundle[ABOVE][SUBJ]
            val prevEdge = e.prev
            if (prevEdge != null) prevEdge.next = succEdge else aet.topNode = succEdge
            if (nextEdge != null) nextEdge.prev = succEdge
            succEdge.prev = prevEdge
            succEdge.next = nextEdge
          } else {
            /* Update this edge */
            e.outputPoly[BELOW] = e.outputPoly[ABOVE]
            e.edgeBundleState[BELOW] = e.edgeBundleState[ABOVE]
            e.bundle[BELOW][CLIP] = e.bundle[ABOVE][CLIP]
            e.bundle[BELOW][SUBJ] = e.bundle[ABOVE][SUBJ]
            e.scanBottomX = e.scanTopX
          }
          e.outputPoly[ABOVE] = null
        }
      }
    } /* === END OF SCANBEAM PROCESSING ================================== */

    /* Generate result polygon from out_poly */
    return outPoly.getResult()
  }

  /**
   * Clipper to output tristrips
   */
  private fun clip(op: OperationType, subj: RPolygon, clip: RPolygon): RMesh {
    if (RG.useFastClip) return FastRClip.clip(op, subj, clip)
    var tList: PolygonNode? = null
    var cf: EdgeNode? = null
    var cft = LeftEdge

    /* Test for trivial NULL result cases */
    if (subj.isEmpty &&
      clip.isEmpty ||
      subj.isEmpty &&
      (op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) ||
      clip.isEmpty &&
      op === OperationType.GPC_INT
    ) {
      return RMesh()
    }

    /* Identify potentially contributing contours */
    if ((op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) && !subj.isEmpty && !clip.isEmpty) {
      minimaxTest(subj, clip, op)
    }

    /* Build LMT */
    val lmtTable = LmtTable()
    val scanBeamTreeEntries = ScanBeamTreeEntries()
    if (!subj.isEmpty) buildLmt(lmtTable, scanBeamTreeEntries, subj, SUBJ, op)
    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for subj ---------")
      lmtTable.print()
    }
    if (!clip.isEmpty) buildLmt(lmtTable, scanBeamTreeEntries, clip, CLIP, op)
    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for clip ---------")
      lmtTable.print()
    }

    /* Return a NULL result if no contours contribute */
    if (lmtTable.topNode == null) return RMesh()

    /* Build scanbeam table from scanbeam tree */
    val sbt = scanBeamTreeEntries.buildSbt()
    val parity = arrayOf(LEFT, LEFT)

    /* Invert clip polygon for difference operation */
    if (op === OperationType.GPC_DIFF) parity[CLIP] = RIGHT
    if (DEBUG) printSbt(sbt)
    var localMin = lmtTable.topNode
    val aet = AetTree()
    var scanbeam = 0

    /* Process each scanbeam */
    while (scanbeam < sbt.size) {
      /* Set yb and yt to the bottom and top of the scanbeam */
      val yb = sbt[scanbeam++]
      val yt = if (scanbeam < sbt.size) sbt[scanbeam] else 0.0f
      val dy = if (scanbeam < sbt.size) yt - yb else 0.0f

      /* === SCANBEAM BOUNDARY PROCESSING ================================ */

      /* If LMT node corresponding to yb exists */
      localMin?.let {
        if (it.y == yb) {
          /* Add edges starting at this local minimum to the AET */
          it.firstBound?.walkBound { edge -> addEdgeToAet(aet, edge) }
          localMin = it.next
        }
      }

      if (DEBUG) aet.print()
      /* Set dummy previous x value */
      var prevX = -Float.MAX_VALUE

      val nonNullTopNode = aet.topNode!!

      /* Create bundles within AET */
      var e0 = nonNullTopNode

      /* Set up bundle fields of first edge */
      nonNullTopNode.bundle[ABOVE][nonNullTopNode.type] = if (nonNullTopNode.top.y != yb) 1 else 0
      nonNullTopNode.bundle[ABOVE][if (nonNullTopNode.type == 0) 1 else 0] = 0
      nonNullTopNode.edgeBundleState[ABOVE] = BundleState.UNBUNDLED
      nonNullTopNode.next?.walk { nextTopEdge ->
        val neType = nextTopEdge.type
        val neTypeOpp = if (nextTopEdge.type == 0) 1 else 0 //next edge type opposite

        /* Set up bundle fields of next edge */
        nextTopEdge.bundle[ABOVE][neType] = if (nextTopEdge.top.y != yb) 1 else 0
        nextTopEdge.bundle[ABOVE][neTypeOpp] = 0
        nextTopEdge.edgeBundleState[ABOVE] = BundleState.UNBUNDLED

        /* Bundle edges above the scanbeam boundary if they coincide */
        if (nextTopEdge.bundle[ABOVE][neType] == 1) {
          if (eq(e0.scanBottomX, nextTopEdge.scanBottomX) &&
            eq(e0.scanUnitDeltaX, nextTopEdge.scanUnitDeltaX) &&
            e0.top.y != yb
          ) {
            nextTopEdge.bundle[ABOVE][neType] =
              nextTopEdge.bundle[ABOVE][neType] xor e0.bundle[ABOVE][neType]
            nextTopEdge.bundle[ABOVE][neTypeOpp] = e0.bundle[ABOVE][neTypeOpp]
            nextTopEdge.edgeBundleState[ABOVE] = BundleState.BUNDLE_HEAD
            e0.bundle[ABOVE][CLIP] = 0
            e0.bundle[ABOVE][SUBJ] = 0
            e0.edgeBundleState[ABOVE] = BundleState.BUNDLE_TAIL
          }
          e0 = nextTopEdge
        }
      }

      val horiz = arrayOf(NoHorizontalEdge, NoHorizontalEdge)
      val exists = arrayOf(0, 0)

      /* Process each edge at this scanbeam boundary */
      aet.topNode?.walk { edge ->
        exists[CLIP] = edge.bundle[ABOVE][CLIP] + (edge.bundle[BELOW][CLIP] shl 1)
        exists[SUBJ] = edge.bundle[ABOVE][SUBJ] + (edge.bundle[BELOW][SUBJ] shl 1)
        if (exists[CLIP] != 0 || exists[SUBJ] != 0) {
          /* Set bundle side */
          edge.bundleSideIndicators[CLIP] = parity[CLIP]
          edge.bundleSideIndicators[SUBJ] = parity[SUBJ]
          var contributing = false
          var br = 0
          var bl = 0
          var tr = 0
          var tl = 0
          /* Determine contributing status and quadrant occupancies */
          if (op === OperationType.GPC_DIFF || op === OperationType.GPC_INT) {
            contributing =
              exists[CLIP] != 0 &&
                (parity[SUBJ] != 0 || horiz[SUBJ] != 0) ||
                exists[SUBJ] != 0
                && (parity[CLIP] != 0 ||
                horiz[CLIP] != 0) ||
                exists[CLIP] != 0 &&
                exists[SUBJ] != 0 &&
                parity[CLIP] == parity[SUBJ]
            br = if (parity[CLIP] != 0 && parity[SUBJ] != 0) 1 else 0
            bl = if (
              parity[CLIP] xor edge.bundle[ABOVE][CLIP] != 0 &&
              parity[SUBJ] xor edge.bundle[ABOVE][SUBJ] != 0
            ) 1
            else 0
            tr =
              if ((parity[CLIP] xor if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) != 0 &&
                (parity[SUBJ] xor if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) != 0
              ) 1 else 0
            tl =
              if (parity[CLIP] xor (if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) xor edge.bundle[BELOW][CLIP] != 0 && parity[SUBJ] xor (if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) xor edge.bundle[BELOW][SUBJ] != 0) 1 else 0
          } else if (op === OperationType.GPC_XOR) {
            contributing = exists[CLIP] != 0 || exists[SUBJ] != 0
            br = parity[CLIP] xor parity[SUBJ]
            bl =
              parity[CLIP] xor edge.bundle[ABOVE][CLIP] xor (parity[SUBJ] xor edge.bundle[ABOVE][SUBJ])
            tr =
              parity[CLIP] xor if (horiz[CLIP] != NoHorizontalEdge) 1 else 0 xor (parity[SUBJ] xor if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0)
            tl =
              (parity[CLIP] xor (if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) xor edge.bundle[BELOW][CLIP] xor (parity[SUBJ] xor (if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) xor edge.bundle[BELOW][SUBJ]))
          } else if (op === OperationType.GPC_UNION) {
            contributing =
              exists[CLIP] != 0 && (parity[SUBJ] == 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] == 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
            br = if (parity[CLIP] != 0 || parity[SUBJ] != 0) 1 else 0
            bl =
              if (parity[CLIP] xor edge.bundle[ABOVE][CLIP] != 0 || parity[SUBJ] xor edge.bundle[ABOVE][SUBJ] != 0) 1 else 0
            tr =
              if ((parity[CLIP] xor if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) != 0 || (parity[SUBJ] xor if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) != 0) 1 else 0
            tl =
              if (parity[CLIP] xor (if (horiz[CLIP] != NoHorizontalEdge) 1 else 0) xor edge.bundle[BELOW][CLIP] != 0 || parity[SUBJ] xor (if (horiz[SUBJ] != NoHorizontalEdge) 1 else 0) xor edge.bundle[BELOW][SUBJ] != 0) 1 else 0
          } else {
            throw IllegalStateException("Unknown op")
          }

          /* Update parity */
          parity[CLIP] = parity[CLIP] xor edge.bundle[ABOVE][CLIP]
          parity[SUBJ] = parity[SUBJ] xor edge.bundle[ABOVE][SUBJ]

          /* Update horizontal state */
          if (exists[CLIP] != 0) {
            horiz[CLIP] = nextHState[horiz[CLIP]][(exists[CLIP] - 1 shl 1) + parity[CLIP]]
          }
          if (exists[SUBJ] != 0) {
            horiz[SUBJ] = nextHState[horiz[SUBJ]][(exists[SUBJ] - 1 shl 1) + parity[SUBJ]]
          }
          if (contributing) {
            val xb = edge.scanBottomX
            when (VertexType.getType(tr, tl, br, bl)) {
              ExternalMin -> {
                tList = newTristrip(tList, edge, xb, yb)
                cf = edge
              }
              ExternalRightIntermediate -> {
                edge.outputPoly[ABOVE] = cf!!.outputPoly[ABOVE]
                if (xb != cf?.scanBottomX) {
                  vertex(edge, ABOVE, RIGHT, xb, yb)
                }
                cf = null
              }
              ExternalLeftIntermediate -> {
                vertex(edge, BELOW, LEFT, xb, yb)
                edge.outputPoly[ABOVE] = null
                cf = edge
              }
              ExternalMaximum -> {
                if (xb != cf!!.scanBottomX) {
                  vertex(edge, BELOW, RIGHT, xb, yb)
                }
                edge.outputPoly[ABOVE] = null
                cf = null
              }
              InternalMin -> {
                if (cft == LeftEdge) {
                  cf?.let {
                    if (it.bot.y == yb) vertex(it, BELOW, LEFT, it.scanBottomX, yb)
                    tList = newTristrip(tList, it, it.scanBottomX, yb)
                  }
                }
                edge.outputPoly[ABOVE] = cf!!.outputPoly[ABOVE]
                vertex(edge, ABOVE, RIGHT, xb, yb)
              }
              InternalLeftIntermediate -> {
                tList = newTristrip(tList, edge, xb, yb)
                cf = edge
                cft = InternalLeftIntermediate
              }
              InternalRightIntermediate -> {
                if (cft == LeftEdge) {
                  cf?.let {
                    if (it.bot.y != yb) {
                      vertex(it, BELOW, LEFT, it.scanBottomX, yb)
                    }
                    tList = newTristrip(tList, it, it.scanBottomX, yb)
                  }
                }
                vertex(edge, BELOW, RIGHT, xb, yb)
                edge.outputPoly[ABOVE] = null
              }
              InternalMaximum -> {
                vertex(edge, BELOW, LEFT, xb, yb)
                edge.outputPoly[ABOVE] = null
                cft = InternalMaximum
              }
              InternalMinAndMax -> {
                vertex(edge, BELOW, LEFT, xb, yb)
                edge.outputPoly[ABOVE] = cf!!.outputPoly[ABOVE]
                cf?.let {
                  if (xb != it.scanBottomX) vertex(it, ABOVE, RIGHT, xb, yb)
                }

                cf = edge
              }
              ExternalMinAndMax -> {
                vertex(edge, BELOW, RIGHT, xb, yb)
                edge.outputPoly[ABOVE] = null
                tList = newTristrip(tList, edge, xb, yb)
                cf = edge
              }
              LeftEdge -> {
                if (edge.bot.y == yb) vertex(edge, BELOW, LEFT, xb, yb)
                edge.outputPoly[ABOVE] = edge.outputPoly[BELOW]
                cf = edge
                cft = LeftEdge
              }
              RightEdge -> run {
                val cfNonNull = cf ?: return@run
                edge.outputPoly[ABOVE] = cfNonNull.outputPoly[ABOVE]
                if (cft == LeftEdge) {
                  if (cfNonNull.bot.y == yb) {
                    vertex(edge, BELOW, RIGHT, xb, yb)
                  } else {
                    if (edge.bot.y == yb) {
                      vertex(cfNonNull, BELOW, LEFT, cfNonNull.scanBottomX, yb)
                      vertex(edge, BELOW, RIGHT, xb, yb)
                    }
                  }
                } else {
                  vertex(edge, BELOW, RIGHT, xb, yb)
                  vertex(edge, ABOVE, RIGHT, xb, yb)
                }
                cf = null
              }
              else -> {
              }
            }
          } /* End of contributing conditional */
        } /* End of edge exists conditional */
      }

      /* Delete terminating edges from the AET, otherwise compute xt */
      aet.topNode?.walk { e ->
        if (e.top.y == yb) {
          val prevEdge = e.prev
          val nextEdge = e.next
          prevEdge?.let { it.next = nextEdge } ?: run { aet.topNode = nextEdge }
          nextEdge?.let { it.prev = prevEdge }

          /* Copy bundle head state to the adjacent tail edge if required */
          prevEdge?.let {
            if (e.edgeBundleState[BELOW] === BundleState.BUNDLE_HEAD &&
              it.edgeBundleState[BELOW] === BundleState.BUNDLE_TAIL
            ) {
              it.outputPoly[BELOW] = e.outputPoly[BELOW]
              it.edgeBundleState[BELOW] = BundleState.UNBUNDLED
              if (it.prev?.edgeBundleState?.get(BELOW) === BundleState.BUNDLE_TAIL) {
                it.edgeBundleState[BELOW] = BundleState.BUNDLE_HEAD
              }
            }
          }
        } else {
          e.scanTopX = if (e.top.y == yt) e.top.x
          else e.bot.x + e.scanUnitDeltaX * (yt - e.bot.y)
        }
      }
      if (scanbeam < scanBeamTreeEntries.sbtEntries) {
        /* === SCANBEAM INTERIOR PROCESSING ============================== */
        /* Build intersection table for the current scanbeam */
        val itTable = ItNodeTable()
        itTable.buildIntersectionTable(aet, dy)

        /* Process each node in the intersection table */
        itTable.topNode?.walk { intersect ->
          val e0 = intersect.ie[0]
          val e1 = intersect.ie[1]

          /* Only generate output for contributing intersections */
          if ((e0.bundle[ABOVE][CLIP] != 0 || e0.bundle[ABOVE][SUBJ] != 0) && (e1.bundle[ABOVE][CLIP] != 0 || e1.bundle[ABOVE][SUBJ] != 0)) {
            val p = e0.outputPoly[ABOVE]
            val q = e1.outputPoly[ABOVE]
            val ix = intersect.point.x
            val iy = intersect.point.y + yb
            val inClip =
              if (e0.bundle[ABOVE][CLIP] != 0 && e0.bundleSideIndicators[CLIP] == 0 || e1.bundle[ABOVE][CLIP] != 0 && e1.bundleSideIndicators[CLIP] != 0 || e0.bundle[ABOVE][CLIP] == 0 && e1.bundle[ABOVE][CLIP] == 0 && e0.bundleSideIndicators[CLIP] != 0 && e1.bundleSideIndicators[CLIP] != 0) 1 else 0
            val inSubj =
              if (e0.bundle[ABOVE][SUBJ] != 0 && e0.bundleSideIndicators[SUBJ] == 0 || e1.bundle[ABOVE][SUBJ] != 0 && e1.bundleSideIndicators[SUBJ] != 0 || e0.bundle[ABOVE][SUBJ] == 0 && e1.bundle[ABOVE][SUBJ] == 0 && e0.bundleSideIndicators[SUBJ] != 0 && e1.bundleSideIndicators[SUBJ] != 0) 1 else 0
            var tr = 0
            var tl = 0
            var br = 0
            var bl = 0
            /* Determine quadrant occupancies */
            if (op === OperationType.GPC_DIFF || op === OperationType.GPC_INT) {
              tr = if (inClip != 0 && inSubj != 0) 1 else 0
              tl =
                if (inClip xor e1.bundle[ABOVE][CLIP] != 0 && inSubj xor e1.bundle[ABOVE][SUBJ] != 0) 1 else 0
              br =
                if (inClip xor e0.bundle[ABOVE][CLIP] != 0 && inSubj xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
              bl =
                if (inClip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] != 0 && inSubj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
            } else if (op === OperationType.GPC_XOR) {
              tr = inClip xor inSubj
              tl = inClip xor e1.bundle[ABOVE][CLIP] xor (inSubj xor e1.bundle[ABOVE][SUBJ])
              br = inClip xor e0.bundle[ABOVE][CLIP] xor (inSubj xor e0.bundle[ABOVE][SUBJ])
              bl =
                (inClip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] xor (inSubj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ]))
            } else if (op === OperationType.GPC_UNION) {
              tr = if (inClip != 0 || inSubj != 0) 1 else 0
              tl =
                if (inClip xor e1.bundle[ABOVE][CLIP] != 0 || inSubj xor e1.bundle[ABOVE][SUBJ] != 0) 1 else 0
              br =
                if (inClip xor e0.bundle[ABOVE][CLIP] != 0 || inSubj xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
              bl =
                if (inClip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] != 0 || inSubj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
            } else {
              throw IllegalStateException("Unknown op type, $op")
            }
            val nextEdge = e1.next
            val prevEdge = e0.prev
            when (VertexType.getType(tr, tl, br, bl)) {
              ExternalMin -> {
                tList = newTristrip(tList, e1, ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
              }
              ExternalRightIntermediate -> if (p != null) {
                prevX = pEdge(e0, ABOVE, iy)
                vertex(prevEdge!!, ABOVE, LEFT, prevX, iy)
                vertex(e0, ABOVE, RIGHT, ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
                e0.outputPoly[ABOVE] = null
              }
              ExternalLeftIntermediate -> if (q != null) {
                val nx = nEdge(e1, ABOVE, iy)
                vertex(e1, ABOVE, LEFT, ix, iy)
                vertex(nextEdge!!, ABOVE, RIGHT, nx, iy)
                e0.outputPoly[ABOVE] = e1.outputPoly[ABOVE]
                e1.outputPoly[ABOVE] = null
              }
              ExternalMaximum -> if (p != null && q != null) {
                vertex(e0, ABOVE, LEFT, ix, iy)
                e0.outputPoly[ABOVE] = null
                e1.outputPoly[ABOVE] = null
              }
              InternalMin -> run {
                val prev = prevEdge ?: return@run
                prevX = pEdge(e0, ABOVE, iy)
                vertex(prev, ABOVE, LEFT, prevX, iy)
                val nx = nEdge(e1, ABOVE, iy)
                vertex(prev, ABOVE, RIGHT, nx, iy)
                tList = newTristrip(tList, prev, prevX, iy)
                e1.outputPoly[ABOVE] = prev.outputPoly[ABOVE]
                vertex(e1, ABOVE, RIGHT, ix, iy)
                tList = newTristrip(tList, e0, ix, iy)
                nextEdge!!.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
                vertex(prev, ABOVE, RIGHT, nx, iy)
              }
              InternalLeftIntermediate -> if (p != null) {
                vertex(e0, ABOVE, LEFT, ix, iy)
                val nx = nEdge(e1, ABOVE, iy)
                vertex(nextEdge!!, ABOVE, RIGHT, nx, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
                e0.outputPoly[ABOVE] = null
              }
              InternalRightIntermediate -> if (q != null) {
                vertex(e1, ABOVE, RIGHT, ix, iy)
                prevX = pEdge(e0, ABOVE, iy)
                vertex(prevEdge!!, ABOVE, LEFT, prevX, iy)
                e0.outputPoly[ABOVE] = e1.outputPoly[ABOVE]
                e1.outputPoly[ABOVE] = null
              }
              InternalMaximum -> if (p != null && q != null) run {
                val prev = prevEdge ?: return@run
                val next = nextEdge ?: return@run
                vertex(e0, ABOVE, RIGHT, ix, iy)
                vertex(e1, ABOVE, LEFT, ix, iy)
                e0.outputPoly[ABOVE] = null
                e1.outputPoly[ABOVE] = null
                prevX = pEdge(e0, ABOVE, iy)
                vertex(prev, ABOVE, LEFT, prevX, iy)
                tList = newTristrip(tList, prev, prevX, iy)
                val nx = nEdge(e1, ABOVE, iy)
                vertex(next, ABOVE, RIGHT, nx, iy)
                next.outputPoly[ABOVE] = prev.outputPoly[ABOVE]
                vertex(next, ABOVE, RIGHT, nx, iy)
              }
              InternalMinAndMax -> if (p != null && q != null) run {
                val prev = prevEdge ?: return@run
                val next = nextEdge ?: return@run
                vertex(e0, ABOVE, RIGHT, ix, iy)
                vertex(e1, ABOVE, LEFT, ix, iy)
                prevX = pEdge(e0, ABOVE, iy)
                vertex(prev, ABOVE, LEFT, prevX, iy)
                tList = newTristrip(tList, prev, prevX, iy)
                val nx = nEdge(e1, ABOVE, iy)
                vertex(next, ABOVE, RIGHT, nx, iy)
                e1.outputPoly[ABOVE] = prev.outputPoly[ABOVE]
                vertex(e1, ABOVE, RIGHT, ix, iy)
                tList = newTristrip(tList, e0, ix, iy)
                next.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
                vertex(next, ABOVE, RIGHT, nx, iy)
              }
              ExternalMinAndMax -> if (p != null && q != null) {
                vertex(e0, ABOVE, LEFT, ix, iy)
                tList = newTristrip(tList, e1, ix, iy)
                e1.outputPoly[ABOVE] = e0.outputPoly[ABOVE]
              }
              else -> {
              }
            }
          } /* End of contributing intersection conditional */

          /* Swap bundle sides in response to edge crossing */
          if (e0.bundle[ABOVE][CLIP] != 0) e1.bundleSideIndicators[CLIP] =
            if (e1.bundleSideIndicators[CLIP] == 0) 1 else 0
          if (e1.bundle[ABOVE][CLIP] != 0) e0.bundleSideIndicators[CLIP] =
            if (e0.bundleSideIndicators[CLIP] == 0) 1 else 0
          if (e0.bundle[ABOVE][SUBJ] != 0) e1.bundleSideIndicators[SUBJ] =
            if (e1.bundleSideIndicators[SUBJ] == 0) 1 else 0
          if (e1.bundle[ABOVE][SUBJ] != 0) e0.bundleSideIndicators[SUBJ] =
            if (e0.bundleSideIndicators[SUBJ] == 0) 1 else 0

          /* Swap e0 and e1 bundles in the AET */
          var prevEdge = e0.prev
          val nextEdge = e1.next
          nextEdge?.let { it.prev = e0 }
          if (e0.edgeBundleState[ABOVE] === BundleState.BUNDLE_HEAD) {
            var search = true
            while (search) {
              prevEdge = prevEdge!!.prev
              prevEdge?.let {
                if (it.bundle[ABOVE][CLIP] != 0 || it.bundle[ABOVE][SUBJ] != 0 || it.edgeBundleState[ABOVE] === BundleState.BUNDLE_HEAD) {
                  search = false
                }
              } ?: run {
                search = false
              }
            }
          }
          prevEdge?.let {
            e1.next = it.next
            it.next = e0.next
          } ?: run {
            e1.next = aet.topNode
            aet.topNode = e0.next
          }
          e0.next!!.prev = prevEdge
          e1.next!!.prev = e1
          e0.next = nextEdge
        }

        /* Prepare for next scanbeam */
        aet.topNode?.walk { e ->
          val nextEdge = e.next
          val succEdge = e.upperEdge
          if (e.top.y == yt && succEdge != null) {
            /* Replace AET edge by its successor */
            succEdge.outputPoly[BELOW] = e.outputPoly[ABOVE]
            succEdge.edgeBundleState[BELOW] = e.edgeBundleState[ABOVE]
            succEdge.bundle[BELOW][CLIP] = e.bundle[ABOVE][CLIP]
            succEdge.bundle[BELOW][SUBJ] = e.bundle[ABOVE][SUBJ]
            val prevEdge = e.prev
            prevEdge?.let { it.next = succEdge } ?: run { aet.topNode = succEdge }
            nextEdge?.let { it.prev = succEdge }
            succEdge.prev = prevEdge
            succEdge.next = nextEdge
          } else {
            /* Update this edge */
            e.outputPoly[BELOW] = e.outputPoly[ABOVE]
            e.edgeBundleState[BELOW] = e.edgeBundleState[ABOVE]
            e.bundle[BELOW][CLIP] = e.bundle[ABOVE][CLIP]
            e.bundle[BELOW][SUBJ] = e.bundle[ABOVE][SUBJ]
            e.scanBottomX = e.scanTopX
          }
          e.outputPoly[ABOVE] = null
        }
      }
    } /* === END OF SCANBEAM PROCESSING ================================== */

    /* Generate result tristrip from tList */
    val result = RMesh()
    if (countTristrips(tList) > 0) {
      var lt: VertexNode?
      var rt: VertexNode?
      tList?.walk { tn ->
        if (tn.active > 2) {
          /* Valid tristrip: copy the vertices and free the heap */
          val strip = RStrip()
          if (INVERT_TRISTRIPS) {
            lt = tn.v[RIGHT]
            rt = tn.v[LEFT]
          } else {
            lt = tn.v[LEFT]
            rt = tn.v[RIGHT]
          }

          var v = 0
          while (lt != null || rt != null) {
            lt?.let {
              strip.add(it.x, it.y)
              v++
              lt = it.next
            }

            rt?.let {
              strip.add(it.x, it.y)
              v++
              rt = it.next
            }
          }
          result.addStrip(strip)
        } else {
          /* Invalid tristrip: just free the heap */
          lt = tn.v[LEFT]
          while (lt != null) lt = lt!!.next
          rt = tn.v[RIGHT]
          while (rt != null) rt = rt!!.next
        }
      }
    }
    return result
  }

  @JvmStatic
  fun polygonToMesh(s: RPolygon): RMesh =
    clip(OperationType.GPC_UNION, s.removeOpenContours(), RPolygon())

  private fun eq(a: Float, b: Float): Boolean = abs(a - b) <= GPC_EPSILON

  private fun prevIndex(i: Int, n: Int): Int = (i - 1 + n) % n

  private fun nextIndex(i: Int, n: Int): Int = (i + 1) % n

  private fun optimal(p: RPolygon, i: Int): Boolean =
    p.getY(prevIndex(i, p.numPoints)) != p.getY(i) ||
      p.getY(nextIndex(i, p.numPoints)) != p.getY(i)

  private fun vertex(e: EdgeNode, p: Int, s: Int, x: Float, y: Float) {
    e.outputPoly[p]!!.v[s] = addVertex(e.outputPoly[p]!!.v[s], x, y)
    e.outputPoly[p]!!.active++
  }

  private fun pEdge(e: EdgeNode?, p: Int, j: Float): Float {
    var d = e
    do {
      d = d!!.prev
    } while (d!!.outputPoly[p] == null)
    return d.bot.x + d.scanUnitDeltaX * (j - d.bot.y)
  }

  private fun nEdge(e: EdgeNode?, p: Int, j: Float): Float {
    var d = e
    do {
      d = d!!.next
    } while (d!!.outputPoly[p] == null)
    return d.bot.x + d.scanUnitDeltaX * (j - d.bot.y)
  }

  private fun createContourBoxes(p: RPolygon): Array<RRectangle> =
    p.innerPolys.map { it.bBox }.toTypedArray()

  private fun minimaxTest(subj: RPolygon, clip: RPolygon, op: OperationType) {
    val sBBox = createContourBoxes(subj)
    val cBBox = createContourBoxes(clip)
    val subjNumPoly = subj.numInnerPoly
    val clipNumPoly = clip.numInnerPoly

    /* Check all subject contour bounding boxes against clip boxes */
    val oTable: Array<Array<Boolean>> = sBBox.mapArray { subjBounds ->
      cBBox.mapArray { clipBounds ->
        !(subjBounds.maxX < clipBounds.minX || subjBounds.minX > clipBounds.maxX) &&
          !(subjBounds.maxY < clipBounds.minY || subjBounds.minY > clipBounds.maxY)
      }
    }

    /* For each clip contour, search for any subject contour overlaps */
    for (c in 0 until clipNumPoly) {
      var overlap = false
      var s = 0
      while (!overlap && s < subjNumPoly) {
        overlap = oTable[s][c]
        s++
      }
      if (!overlap) clip.setContributing(c, false)
    }

    if (op === OperationType.GPC_INT) {
      /* For each subject contour, search for any clip contour overlaps */
      for (s in 0 until subjNumPoly) {
        var overlap = false
        var c = 0
        while (!overlap && c < clipNumPoly) {
          overlap = oTable[s][c]
          c++
        }
        if (!overlap) subj.setContributing(s, false)
      }
    }
  }

  private fun boundList(lmtTable: LmtTable, y: Float): LmtNode? {
    return if (lmtTable.topNode == null) {
      lmtTable.topNode = LmtNode(y)
      lmtTable.topNode
    } else {
      var prev: LmtNode? = null
      var node = lmtTable.topNode
      while (true) {
        if (y < node!!.y) {
          /* Insert a new LMT node before the current node */
          val existingNode = node
          node = LmtNode(y)
          node.next = existingNode
          if (prev == null) {
            lmtTable.topNode = node
          } else {
            prev.next = node
          }
          break
        } else if (y > node.y) {
          /* Head further up the LMT */
          if (node.next == null) {
            node.next = LmtNode(y)
            node = node.next
            break
          } else {
            prev = node
            node = node.next
          }
        } else {
          /* Use this existing LMT node */
          break
        }
      }
      node
    }
  }

  private fun insertBound(lmtNode: LmtNode?, e: EdgeNode) {
    if (lmtNode!!.firstBound == null) {
      /* Link node e to the tail of the list */
      lmtNode.firstBound = e
      return
    }

    var prevBound: EdgeNode? = null
    var currentBound = lmtNode.firstBound
    while (true) {
      /* Do primary sort on the x field */
      if (e.bot.x < currentBound!!.bot.x) {
        /* Insert a new node mid-list */
        if (prevBound == null)
          lmtNode.firstBound = e
        else
          prevBound.nextBound = e
        e.nextBound = currentBound
        break
      } else if (e.bot.x == currentBound.bot.x) {
        /* Do secondary sort on the dx field */
        if (e.scanUnitDeltaX < currentBound.scanUnitDeltaX) {
          /* Insert a new node mid-list */
          if (prevBound == null)
            lmtNode.firstBound = e
          else
            prevBound.nextBound = e
          e.nextBound = currentBound
          break
        } else {
          /* Head further down the list */
          if (currentBound.nextBound == null) {
            currentBound.nextBound = e
            break
          } else {
            prevBound = currentBound
            currentBound = currentBound.nextBound
          }
        }
      } else {
        /* Head further down the list */
        if (currentBound.nextBound == null) {
          currentBound.nextBound = e
          break
        } else {
          prevBound = currentBound
          currentBound = currentBound.nextBound
        }
      }
    }
  }

  private fun addEdgeToAet(aet: AetTree, edge: EdgeNode) {
    if (aet.topNode == null) {
      /* Append edge onto the tail end of the AET */
      aet.topNode = edge
      edge.prev = null
      edge.next = null
      return
    }
    var currentEdge = aet.topNode
    var prev: EdgeNode? = null
    while (true) {
      /* Do primary sort on the xb field */
      if (edge.scanBottomX < currentEdge!!.scanBottomX) {
        /* Insert edge here (before the AET edge) */
        edge.prev = prev
        edge.next = currentEdge
        currentEdge.prev = edge
        if (prev == null)
          aet.topNode = edge
        else
          prev.next = edge
        break
      } else if (edge.scanBottomX == currentEdge.scanBottomX) {
        /* Do secondary sort on the dx field */
        if (edge.scanUnitDeltaX < currentEdge.scanUnitDeltaX) {
          /* Insert edge here (before the AET edge) */
          edge.prev = prev
          edge.next = currentEdge
          currentEdge.prev = edge
          if (prev == null) {
            aet.topNode = edge
          } else {
            prev.next = edge
          }
          break
        } else {
          /* Head further into the AET */
          prev = currentEdge
          if (currentEdge.next == null) {
            currentEdge.next = edge
            edge.prev = currentEdge
            edge.next = null
            break
          } else {
            currentEdge = currentEdge.next
          }
        }
      } else {
        /* Head further into the AET */
        prev = currentEdge
        if (currentEdge.next == null) {
          currentEdge.next = edge
          edge.prev = currentEdge
          edge.next = null
          break
        } else {
          currentEdge = currentEdge.next
        }
      }
    }
  }

  private fun addToSbTree(scanBeamEntries: ScanBeamTreeEntries, y: Float) {
    if (scanBeamEntries.sbTree == null) {
      /* Add a new tree node here */
      scanBeamEntries.sbTree = ScanBeamTree(y)
      scanBeamEntries.sbtEntries++
      return
    }

    var treeNode = scanBeamEntries.sbTree
    while (true) {
      if (treeNode!!.y > y) {
        if (treeNode.less == null) {
          treeNode.less = ScanBeamTree(y)
          scanBeamEntries.sbtEntries++
          break
        } else {
          treeNode = treeNode.less
        }
      } else if (treeNode.y < y) {
        if (treeNode.more == null) {
          treeNode.more = ScanBeamTree(y)
          scanBeamEntries.sbtEntries++
          break
        } else {
          treeNode = treeNode.more
        }
      } else {
        break
      }
    }
  }

  private fun buildLmt(
    lmt_table: LmtTable,
    scanBeamEntries: ScanBeamTreeEntries,
    p: RPolygon,
    // poly type SUBJ/CLIP
    type: Int,
    op: OperationType,
  ): EdgeTable {
    /* Create the entire input polygon edge table in one go */
    var edgeTable = EdgeTable()
    p.innerPolys.forEach { ip ->
      if (!ip.isContributing(0)) {
        /* Ignore the non-contributing contour */
        ip.setContributing(0, true)
      } else {
        /* Perform contour optimisation */
        var numVertices = 0
        var eIndex = 0
        edgeTable = EdgeTable()
        ip.points.forEachIndexed { pointIndex, _ ->
          if (optimal(ip, pointIndex)) {
            val x = ip.getX(pointIndex)
            val y = ip.getY(pointIndex)
            edgeTable.addNode(x, y)

            /* Record vertex in the scanbeam table */
            addToSbTree(scanBeamEntries, ip.getY(pointIndex))
            numVertices++
          }
        }

        /* Do the contour forward pass */
        for (min in 0 until numVertices) {
          /* If a forward local minimum... */
          if (edgeTable.fwdMin(min)) {
            /* Search for the next local maximum... */
            var numEdges = 1
            var max = nextIndex(min, numVertices)
            while (edgeTable.notFMax(max)) {
              numEdges++
              max = nextIndex(max, numVertices)
            }

            /* Build the next edge list */
            var v = min
            val e = edgeTable.getNode(eIndex)
            e.edgeBundleState[BELOW] = BundleState.UNBUNDLED
            e.bundle[BELOW][CLIP] = 0
            e.bundle[BELOW][SUBJ] = 0
            for (i in 0 until numEdges) {
              val ei = edgeTable.getNode(eIndex + i)
              var ev = edgeTable.getNode(v)
              ei.scanBottomX = ev.vertex.x
              ei.bot.x = ev.vertex.x
              ei.bot.y = ev.vertex.y
              v = nextIndex(v, numVertices)
              ev = edgeTable.getNode(v)
              ei.top.x = ev.vertex.x
              ei.top.y = ev.vertex.y
              ei.scanUnitDeltaX = (ev.vertex.x - ei.bot.x) / (ei.top.y - ei.bot.y)
              ei.type = type
              ei.outputPoly[ABOVE] = null
              ei.outputPoly[BELOW] = null
              ei.next = null
              ei.prev = null
              ei.upperEdge = if (numEdges > 1 && i < numEdges - 1)
                edgeTable.getNode(eIndex + i + 1)
              else null
              ei.lowerEdge = if (numEdges > 1 && i > 0) edgeTable.getNode(eIndex + i - 1) else null
              ei.nextBound = null
              ei.bundleSideIndicators[CLIP] = if (op === OperationType.GPC_DIFF) RIGHT else LEFT
              ei.bundleSideIndicators[SUBJ] = LEFT
            }
            insertBound(boundList(lmt_table, edgeTable.getNode(min).vertex.y), e)
            if (DEBUG) {
              println("fwd")
              lmt_table.print()
            }
            eIndex += numEdges
          }
        }

        /* Do the contour reverse pass */
        for (min in 0 until numVertices) {
          /* If a reverse local minimum... */
          if (edgeTable.revMin(min)) {
            /* Search for the previous local maximum... */
            var numEdges = 1
            var max = prevIndex(min, numVertices)
            while (edgeTable.notRMax(max)) {
              numEdges++
              max = prevIndex(max, numVertices)
            }

            /* Build the previous edge list */
            var v = min
            val e = edgeTable.getNode(eIndex)
            e.edgeBundleState[BELOW] = BundleState.UNBUNDLED
            e.bundle[BELOW][CLIP] = 0
            e.bundle[BELOW][SUBJ] = 0
            for (i in 0 until numEdges) {
              val ei = edgeTable.getNode(eIndex + i)
              var ev = edgeTable.getNode(v)
              ei.scanBottomX = ev.vertex.x
              ei.bot.x = ev.vertex.x
              ei.bot.y = ev.vertex.y
              v = prevIndex(v, numVertices)
              ev = edgeTable.getNode(v)
              ei.top.x = ev.vertex.x
              ei.top.y = ev.vertex.y
              ei.scanUnitDeltaX = (ev.vertex.x - ei.bot.x) / (ei.top.y - ei.bot.y)
              ei.type = type
              ei.outputPoly[ABOVE] = null
              ei.outputPoly[BELOW] = null
              ei.next = null
              ei.prev = null
              ei.upperEdge =
                if (numEdges > 1 && i < numEdges - 1) edgeTable.getNode(
                  eIndex + i + 1
                ) else null
              ei.lowerEdge = if (numEdges > 1 && i > 0) edgeTable.getNode(eIndex + i - 1) else null
              ei.nextBound = null
              ei.bundleSideIndicators[CLIP] = if (op === OperationType.GPC_DIFF) RIGHT else LEFT
              ei.bundleSideIndicators[SUBJ] = LEFT
            }
            insertBound(boundList(lmt_table, edgeTable.getNode(min).vertex.y), e)
            if (DEBUG) {
              println("rev")
              lmt_table.print()
            }
            eIndex += numEdges
          }
        }
      }
    }
    return edgeTable
  }

  private fun addStEdge(stParam: StNode?, it: ItNodeTable, edge: EdgeNode, dy: Float): StNode {
    var st = stParam ?: return StNode(edge, null)

    val den = st.beamTopX - st.beamBottomX - (edge.scanTopX - edge.scanBottomX)

    /* If new edge and ST edge don't cross */
    if (edge.scanTopX >= st.beamTopX || edge.scanUnitDeltaX == st.unitDeltaX || abs(den) <= GPC_EPSILON) {
      /* No intersection - insert edge here (before the ST edge) */
      val existingNode: StNode = st
      st = StNode(edge, existingNode)
    } else {
      /* Compute intersection between new edge and ST edge */
      val r = (edge.scanBottomX - st.beamBottomX) / den
      val x = st.beamBottomX + r * (st.beamTopX - st.beamBottomX)
      val y = r * dy

      /* Insert the edge pointers and the intersection point in the IT */
      it.topNode = addIntersection(it.topNode, st.edge, edge, x, y)

      /* Head further into the ST */
      st.prev = addStEdge(st.prev, it, edge, dy)
    }
    return st
  }

  private fun addIntersection(
    itNodeParam: ItNode?,
    edge0: EdgeNode,
    edge1: EdgeNode,
    x: Float,
    y: Float,
  ): ItNode {
    var itNode = itNodeParam ?: return ItNode(edge0, edge1, x, y, null)

    if (itNode.point.y > y) {
      /* Insert a new node mid-list */
      val existingNode: ItNode = itNode
      itNode = ItNode(edge0, edge1, x, y, existingNode)
    } else {
      /* Head further down the list */
      itNode.next = addIntersection(itNode.next, edge0, edge1, x, y)
    }

    return itNode
  }

  private fun countTristrips(tnParam: PolygonNode?): Int {
    var total = 0
    tnParam?.walk { tn -> if (tn.active > 2) total++ }
    return total
  }

  private fun addVertex(veNode: VertexNode?, x: Float, y: Float): VertexNode =
    veNode?.also {
      addVertex(it.next, x, y)
    } ?: VertexNode(x, y)

  private fun newTristrip(
    po_node: PolygonNode?,
    edge: EdgeNode?,
    x: Float,
    y: Float,
  ): PolygonNode {
    var poNode = po_node
    if (poNode == null) {
      /* Append a new node to the tail of the list */
      poNode = PolygonNode()
      poNode.v[LEFT] = addVertex(poNode.v[LEFT], x, y)
      edge!!.outputPoly[ABOVE] = poNode
    } else {
      /* Head further down the list */
      poNode.next = newTristrip(poNode.next, edge, x, y)
    }
    return poNode
  }

  // -------------
  // --- DEBUG ---
  // -------------
  private fun printSbt(sbt: FloatArray) {
    println()
    println("sbt.length=" + sbt.size)
    for (i in sbt.indices) {
      println("sbt[" + i + "]=" + sbt[i])
    }
  }

  // ---------------------
  // --- Inner Classes ---
  // ---------------------
  class OperationType private constructor(private val m_Type: String) {
    override fun toString(): String = m_Type

    companion object {
      val GPC_DIFF = OperationType("Difference")
      val GPC_INT = OperationType("Intersection")
      val GPC_XOR = OperationType("Exclusive or")
      val GPC_UNION = OperationType("Union")
    }
  }

  /**
   * Edge intersection classes
   */
  private object VertexType {
    const val EmptyNonIntersection = 0
    const val ExternalMaximum = 1
    const val ExternalLeftIntermediate = 2
    const val TopEdge = 3
    const val ExternalRightIntermediate = 4
    const val RightEdge = 5
    const val InternalMinAndMax = 6
    const val InternalMin = 7
    const val ExternalMin = 8
    const val ExternalMinAndMax = 9
    const val LeftEdge = 10
    const val InternalLeftIntermediate = 11
    const val BottomEdge = 12
    const val InternalRightIntermediate = 13
    const val InternalMaximum = 14
    const val FullNonIntersection = 15
    fun getType(tr: Int, tl: Int, br: Int, bl: Int): Int {
      return tr + (tl shl 1) + (br shl 2) + (bl shl 3)
    }
  }

  /**
   * Horizontal edge states
   */
  private object HState {
    const val NoHorizontalEdge = 0
    const val BottomEdge = 1
    const val TopEdge = 2

    /* Horizontal edge state transitions within scanbeam boundary */
    val nextHState = arrayOf(
      intArrayOf(
        BottomEdge,
        TopEdge,
        TopEdge,
        BottomEdge,
        NoHorizontalEdge,
        NoHorizontalEdge
      ),
      intArrayOf(
        NoHorizontalEdge,
        NoHorizontalEdge,
        NoHorizontalEdge,
        NoHorizontalEdge,
        TopEdge,
        TopEdge
      ),
      intArrayOf(
        NoHorizontalEdge,
        NoHorizontalEdge,
        NoHorizontalEdge,
        NoHorizontalEdge,
        BottomEdge,
        BottomEdge
      )
    )
  }

  /**
   * Edge bundle state
   */
  private class BundleState private constructor(private val mState: String) {
    override fun toString(): String = mState

    companion object {
      val UNBUNDLED = BundleState("UNBUNDLED") // Isolated edge not within a bundle
      val BUNDLE_HEAD = BundleState("BUNDLE_HEAD") // Bundle head node
      val BUNDLE_TAIL = BundleState("BUNDLE_TAIL") // Passive bundle tail node
    }
  }

  /**
   * Internal vertex list datatype
   */
  private class VertexNode(
    // X coordinate component
    var x: Float,
    // Y coordinate component
    var y: Float,
  ) {
    // Pointer to next vertex in list
    var next: VertexNode? = null

    fun walk(block: (VertexNode) -> Unit) {
      var p: VertexNode? = this
      while (p != null) {
        block(p)
        p = p.next
      }
    }
  }

  /**
   * Internal contour / tristrip type
   */
  private class PolygonNode {
    // Active flag / vertex count
    var active: Int

    // Hole / external contour flag
    var hole = false

    // Left and right vertex list pointers
    var v = arrayOfNulls<VertexNode>(2)

    // Pointer to next polygon contour
    var next: PolygonNode?

    // Pointer to actual structure used
    var proxy: PolygonNode

    constructor() {
      /* Make v[LEFT] and v[RIGHT] point to new vertex */
      v[LEFT] = null
      v[RIGHT] = null
      next = null
      proxy = this /* Initialise proxy to point to p itself */
      active = 1 //TRUE
    }

    constructor(next: PolygonNode?, x: Float, y: Float) {
      /* Make v[LEFT] and v[RIGHT] point to new vertex */
      val vn = VertexNode(x, y)
      v[LEFT] = vn
      v[RIGHT] = vn
      this.next = next
      proxy = this /* Initialise proxy to point to p itself */
      active = 1 //TRUE
    }

    fun addRight(x: Float, y: Float) {
      val nv = VertexNode(x, y)

      /* Add vertex nv to the right end of the polygon's vertex list */
      proxy.v[RIGHT]!!.next = nv

      /* Update proxy->v[RIGHT] to point to nv */
      proxy.v[RIGHT] = nv
    }

    fun addLeft(x: Float, y: Float) {
      val nv = VertexNode(x, y)

      /* Add vertex nv to the left end of the polygon's vertex list */
      nv.next = proxy.v[LEFT]

      /* Update proxy->[LEFT] to point to nv */
      proxy.v[LEFT] = nv
    }

    fun walk(block: (PolygonNode) -> Unit) {
      var p: PolygonNode? = this
      while (p != null) {
        block(p)
        p = p.next
      }
    }

    inline fun <reified K> PolygonNode.mapArray(block: (PolygonNode) -> K): Array<K> {
      var p: PolygonNode? = this
      val result = mutableListOf<K>()
      while (p != null) {
        result.add(block(p))
        p = p.next
      }

      return result.toTypedArray()
    }
  }

  private class TopPolygonNode {
    var topNode: PolygonNode? = null
    fun addLocalMin(x: Float, y: Float): PolygonNode =
      PolygonNode(topNode, x, y)
        .also { topNode = it }

    fun mergeLeft(p: PolygonNode, q: PolygonNode) {
      /* Label contour as a hole */
      q.proxy.hole = true
      if (p.proxy !== q.proxy) {
        /* Assign p's vertex list to the left end of q's list */
        p.proxy.v[RIGHT]!!.next = q.proxy.v[LEFT]
        q.proxy.v[LEFT] = p.proxy.v[LEFT]

        /* Redirect any p.proxy references to q.proxy */
        val target = p.proxy
        var node = topNode
        while (node != null) {
          if (node.proxy === target) {
            node.active = 0
            node.proxy = q.proxy
          }
          node = node.next
        }
      }
    }

    fun mergeRight(p: PolygonNode, q: PolygonNode) {
      /* Label contour as external */
      q.proxy.hole = false
      if (p.proxy !== q.proxy) {
        /* Assign p's vertex list to the right end of q's list */
        q.proxy.v[RIGHT]!!.next = p.proxy.v[LEFT]
        q.proxy.v[RIGHT] = p.proxy.v[RIGHT]

        /* Redirect any p->proxy references to q->proxy */
        val target = p.proxy

        topNode?.walk { node ->
          if (node.proxy === target) {
            node.active = 0
            node.proxy = q.proxy
          }
        }
      }
    }

    fun countContours(): Int {
      var nc = 0
      topNode?.walk { polygon ->
        if (polygon.active != 0) {
          /* Count the vertices in the current contour */
          var nv = 0
          var v = polygon.proxy.v[LEFT]
          while (v != null) {
            nv++
            v = v.next
          }

          /* Record valid vertex counts in the active field */
          if (nv > 2) {
            polygon.active = nv
            nc++
          } else {
            polygon.active = 0
          }
        }
      }
      return nc
    }

    fun getResult(): RPolygon {
      var result = RPolygon()
      val numContours = countContours()
      if (numContours == 0) return result
      var c = 0
      topNode?.walk { polyNode ->
        if (polyNode.active != 0) {
          var contour: RContour
          contour = (if (result.contours.isNotEmpty()) result.contours[0] else RContour())
          if (numContours > 0) {
            contour = RContour()
          }
          if (polyNode.proxy.hole) {
            contour.isHole = polyNode.proxy.hole
          }

          // ------------------------------------------------------------------------
          // --- This algorithm puts the vertices into the poly in reverse order ---
          // ------------------------------------------------------------------------
          var vtx = polyNode.proxy.v[LEFT]
          while (vtx != null) {
            contour.addPoint(vtx.x, vtx.y)
            vtx = vtx.next
          }
          if (numContours > 0) {
            result.addContour(contour)
          }
          c++
        }
      }


      // -----------------------------------------
      // --- Sort holes to the end of the list ---
      // -----------------------------------------
      val orig = RPolygon(result)
      result = RPolygon()
      orig.contours.filter { it.isHole }.forEach { result.addContour(it) }
      orig.contours.filter { !it.isHole }.forEach { result.addContour(it) }
      return result
    }

    fun print() {
      println("---- out_poly ----")
      var c = 0
      topNode?.walk { polyNode ->
        println("contour=" + c + "  active=" + polyNode.active + "  hole=" + polyNode.proxy.hole)
        if (polyNode.active == 0) return@walk
        val v = 0
        polyNode.proxy.v[LEFT]?.walk { vertexNode ->
          println("v=" + v + "  vtx.x=" + vertexNode.x + "  vertexNode.y=" + vertexNode.y)
        }
        c++
      }
    }
  }

  private class EdgeNode(var vertex: RPoint) {
    var bot = RPoint(0, 0) /* Edge lower (x, y) coordinate      */
    var top = RPoint(0, 0) /* Edge upper (x, y) coordinate      */
    var scanBottomX = 0f /* Scanbeam bottom x coordinate      */
    var scanTopX = 0f /* Scanbeam top x coordinate         */
    var scanUnitDeltaX = 0f /* Change in x for a unit y increase */
    var type = 0 /* Clip / subject edge flag          */
    var bundle = Array(2) { IntArray(2) } /* Bundle edge flags                 */
    var bundleSideIndicators = IntArray(2) /* Bundle left / right indicators    */
    var edgeBundleState = arrayOfNulls<BundleState>(2) /* Edge bundle state                 */
    var outputPoly = arrayOfNulls<PolygonNode>(2) /* Output polygon / tristrip pointer */
    var prev: EdgeNode? = null /* Previous edge in the AET          */
    var next: EdgeNode? = null /* Next edge in the AET              */
    var lowerEdge: EdgeNode? = null /* Edge connected at the lower end   */
    var upperEdge: EdgeNode? = null /* Edge connected at the upper end   */
    var nextBound: EdgeNode? = null /* Pointer to next bound in LMT      */

    fun walkBound(block: (EdgeNode) -> Unit) {
      var p: EdgeNode? = this
      while (p != null) {
        block(p)
        p = p.nextBound
      }
    }

    fun walk(block: (EdgeNode) -> Unit) {
      var p: EdgeNode? = this
      while (p != null) {
        block(p)
        p = p.next
      }
    }
  }

  private class AetTree {
    var topNode: EdgeNode? = null
    fun print() {
      println()
      println("aet")
      topNode?.walk { edge ->
        println("edge.vertex.x=" + edge.vertex.x + "  edge.vertex.y=" + edge.vertex.y)
      }
    }
  }

  private class EdgeTable {
    private val mList: MutableList<EdgeNode> = mutableListOf()
    fun addNode(x: Float, y: Float) = mList.add(EdgeNode(RPoint(x, y)))

    fun getNode(index: Int): EdgeNode = mList[index]

    fun fwdMin(i: Int): Boolean {
      val prev = mList[prevIndex(i, mList.size)]
      val next = mList[nextIndex(i, mList.size)]
      val ith = mList[i]
      return prev.vertex.y >= ith.vertex.y && next.vertex.y > ith.vertex.y
    }

    fun notFMax(i: Int): Boolean {
      val next = mList[nextIndex(i, mList.size)]
      val ith = mList[i]
      return next.vertex.y > ith.vertex.y
    }

    fun revMin(i: Int): Boolean {
      val prev = mList[prevIndex(i, mList.size)]
      val next = mList[nextIndex(i, mList.size)]
      val ith = mList[i]
      return prev.vertex.y > ith.vertex.y && next.vertex.y >= ith.vertex.y
    }

    fun notRMax(i: Int): Boolean {
      val prev = mList[prevIndex(i, mList.size)]
      val ith = mList[i]
      return prev.vertex.y > ith.vertex.y
    }
  }

  /**
   * Local minima table
   */
  private class LmtNode(
    /* Y coordinate at local minimum     */
    var y: Float,
  ) {
    /* Pointer to bound list             */
    var firstBound: EdgeNode? = null

    /* Pointer to next local minimum     */
    var next: LmtNode? = null

    fun walk(block: (LmtNode) -> Unit) {
      var p: LmtNode? = this
      while (p != null) {
        block(p)
        p = p.next
      }
    }
  }

  private class LmtTable {
    var topNode: LmtNode? = null

    fun print() {
      var n = 0
      topNode?.walk { lmt ->
        println("lmt($n)")
        lmt.firstBound?.walkBound { edge ->
          println("edge.vertex.x=" + edge.vertex.x + "  edge.vertex.y=" + edge.vertex.y)
        }
        n++
      }
    }
  }

  /**
   * Scanbeam tree
   */
  private class ScanBeamTree(
    // Scanbeam node y value
    var y: Float,
  ) {
    var less: ScanBeamTree? = null /* Pointer to nodes with lower y     */
    var more: ScanBeamTree? = null /* Pointer to nodes with higher y    */
  }

  private class ScanBeamTreeEntries {
    var sbtEntries = 0
    var sbTree: ScanBeamTree? = null

    fun buildSbt(): FloatArray {
      val sbt = FloatArray(sbtEntries)
      var entries = 0
      entries = innerBuildSbt(entries, sbt, sbTree!!)
      check(entries == sbtEntries) { "Something went wrong building sbt from tree." }
      return sbt
    }

    private fun innerBuildSbt(entriesParam: Int, sbt: FloatArray, sbt_node: ScanBeamTree): Int {
      var entries = entriesParam
      sbt_node.less?.let { entries = innerBuildSbt(entries, sbt, it) }
      sbt[entries] = sbt_node.y
      entries++
      sbt_node.more?.let { entries = innerBuildSbt(entries, sbt, it) }
      return entries
    }
  }

  /**
   * Intersection table
   */
  private class ItNode(
    edge0: EdgeNode,
    edge1: EdgeNode,
    x: Float,
    y: Float,
    // The next intersection table node
    var next: ItNode?
  ) {
    var ie = listOf(edge0, edge1) /* Intersecting edge (bundle) pair */
    var point = RPoint(x, y) /* Point of intersection */

    fun walk(block: (ItNode) -> Unit) {
      var p: ItNode? = this
      while (p != null) {
        block(p)
        p = p.next
      }
    }
  }

  private class ItNodeTable {
    var topNode: ItNode? = null

    fun buildIntersectionTable(aet: AetTree, dy: Float) {
      var st: StNode? = null

      /* Process each AET edge */
      aet.topNode?.walk { edge ->
        if (edge.edgeBundleState[ABOVE] === BundleState.BUNDLE_HEAD ||
          edge.bundle[ABOVE][CLIP] != 0 ||
          edge.bundle[ABOVE][SUBJ] != 0
        ) {
          st = addStEdge(st, this, edge, dy)
        }
      }
    }
  }

  /**
   * Sorted edge table
   */
  private class StNode(
    /* Pointer to AET edge               */
    var edge: EdgeNode,
    var prev: StNode?,
  ) {
    /* Scanbeam bottom x coordinate      */
    val beamBottomX: Float get() = edge.scanBottomX

    /* Scanbeam top x coordinate         */
    val beamTopX: Float get() = edge.scanTopX

    /* Change in x for a unit y increase */
    val unitDeltaX: Float get() = edge.scanUnitDeltaX
  }
}
