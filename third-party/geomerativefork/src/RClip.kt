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
@file:Suppress("unused")

package geomerativefork.src

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
   * for more on <ocde>polyClass.
   *
   * @param p1        One of the polygons to performt he intersection with
   * @param p2        One of the polygons to performt he intersection with
   * @param polyClass The type of `RPolygon` to return
  </ocde> */
  fun intersection(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_INT, p1, p2, polyClass)

  /**
   * Return the union of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <ocde>polyClass.
   *
   * @param p1        One of the polygons to performt he union with
   * @param p2        One of the polygons to performt he union with
   * @param polyClass The type of `RPolygon` to return
  </ocde> */
  fun union(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_UNION, p1, p2, polyClass)

  /**
   * Return the xor of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <ocde>polyClass.
   *
   * @param p1        One of the polygons to performt he xor with
   * @param p2        One of the polygons to performt he xor with
   * @param polyClass The type of `RPolygon` to return
  </ocde> */
  fun xor(p1: RPolygon, p2: RPolygon, polyClass: Class<*>): RPolygon =
    clip(OperationType.GPC_XOR, p1, p2, polyClass)

  /**
   * Return the diff of `p1` and `p2` where the
   * return type is of `polyClass`.  See the note in the class description
   * for more on <ocde>polyClass.
   *
   * @param p1        One of the polygons to performt he diff with
   * @param p2        One of the polygons to performt he diff with
   * @param polyClass The type of `RPolygon` to return
  </ocde> */
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
   * @param p1 One of the polygons to performt he xor with
   * @param p2 One of the polygons to performt he xor with
   */
  @JvmStatic
  fun xor(p1: RPolygon, p2: RPolygon): RPolygon =
    clip(OperationType.GPC_XOR, p1, p2, RPolygon::class.java)

  /**
   * Return the diff of `p1` and `p2` where the
   * return type is of `PolyDefault`.
   *
   * @param p1 One of the polygons to performt he diff with
   * @param p2 One of the polygons to performt he diff with
   */
  @JvmStatic
  fun diff(p1: RPolygon, p2: RPolygon): RPolygon =
    clip(OperationType.GPC_DIFF, p1, p2, RPolygon::class.java)

  /**
   * Updates `p1`.
   *
   * @param p1 One of the polygons to performt he diff with
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
    op: OperationType, subj: RPolygon, clip: RPolygon, polyClass: Class<*>,
  ): RPolygon {
    if (RG.useFastClip) {
      return FastRClip.clip(op, subj, clip, polyClass)
    }
    var result = createNewPoly(polyClass)

    /* Test for trivial NULL result cases */
    if (subj.isEmpty && clip.isEmpty || subj.isEmpty && (op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) || clip.isEmpty && op === OperationType.GPC_INT) {
      return result
    }

    /* Identify potentially contributing contours */
    if ((op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) && !subj.isEmpty && !clip.isEmpty) {
      minimax_test(subj, clip, op)
    }

    /* Build LMT */
    val lmt_table = LmtTable()
    val sbte = ScanBeamTreeEntries()
    if (!subj.isEmpty) {
      build_lmt(lmt_table, sbte, subj, SUBJ, op)
    }
    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for subj ---------")
      lmt_table.print()
    }
    if (!clip.isEmpty) {
      build_lmt(lmt_table, sbte, clip, CLIP, op)
    }
    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for clip ---------")
      lmt_table.print()
    }

    /* Return a NULL result if no contours contribute */
    if (lmt_table.top_node == null) {
      return result
    }

    /* Build scanbeam table from scanbeam tree */
    val sbt = sbte.build_sbt()
    val parity = IntArray(2)
    parity[0] = LEFT
    parity[1] = LEFT

    /* Invert clip polygon for difference operation */
    if (op === OperationType.GPC_DIFF) {
      parity[CLIP] = RIGHT
    }
    if (DEBUG) {
      print_sbt(sbt)
    }
    var local_min = lmt_table.top_node
    val out_poly = TopPolygonNode() // used to create resulting RPolygon
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
      if (local_min != null) {
        if (local_min.y == yb) {
          /* Add edges starting at this local minimum to the AET */
          var edge = local_min.first_bound
          while (edge != null) {
            add_edge_to_aet(aet, edge)
            edge = edge.next_bound
          }
          local_min = local_min.next
        }
      }
      if (DEBUG) {
        aet.print()
      }
      /* Set dummy previous x value */
      var px = -Float.MAX_VALUE

      /* Create bundles within AET */
      var e0 = aet.top_node
      var e1 = aet.top_node

      /* Set up bundle fields of first edge */
      aet.top_node!!.bundle[ABOVE][aet.top_node!!.type] = if (aet.top_node!!.top.y != yb) 1 else 0
      aet.top_node!!.bundle[ABOVE][if (aet.top_node!!.type == 0) 1 else 0] = 0
      aet.top_node!!.bstate[ABOVE] = BundleState.UNBUNDLED
      var next_edge = aet.top_node!!.next
      while (next_edge != null) {
        val ne_type = next_edge.type
        val ne_type_opp = if (next_edge.type == 0) 1 else 0 //next edge type opposite

        /* Set up bundle fields of next edge */
        next_edge.bundle[ABOVE][ne_type] = if (next_edge.top.y != yb) 1 else 0
        next_edge.bundle[ABOVE][ne_type_opp] = 0
        next_edge.bstate[ABOVE] = BundleState.UNBUNDLED

        /* Bundle edges above the scanbeam boundary if they coincide */
        if (next_edge.bundle[ABOVE][ne_type] == 1) {
          if (EQ(e0!!.xb, next_edge.xb) && EQ(e0.dx, next_edge.dx) && e0.top.y != yb) {
            next_edge.bundle[ABOVE][ne_type] =
              next_edge.bundle[ABOVE][ne_type] xor e0.bundle[ABOVE][ne_type]
            next_edge.bundle[ABOVE][ne_type_opp] = e0.bundle[ABOVE][ne_type_opp]
            next_edge.bstate[ABOVE] = BundleState.BUNDLE_HEAD
            e0.bundle[ABOVE][CLIP] = 0
            e0.bundle[ABOVE][SUBJ] = 0
            e0.bstate[ABOVE] = BundleState.BUNDLE_TAIL
          }
          e0 = next_edge
        }
        next_edge = next_edge.next
      }
      val horiz = IntArray(2)
      horiz[CLIP] = HState.NH
      horiz[SUBJ] = HState.NH
      val exists = IntArray(2)
      exists[CLIP] = 0
      exists[SUBJ] = 0
      var cf: PolygonNode? = null

      /* Process each edge at this scanbeam boundary */
      run {
        var edge = aet.top_node
        while (edge != null) {
          exists[CLIP] = edge!!.bundle[ABOVE][CLIP] + (edge!!.bundle[BELOW][CLIP] shl 1)
          exists[SUBJ] = edge!!.bundle[ABOVE][SUBJ] + (edge!!.bundle[BELOW][SUBJ] shl 1)
          if (exists[CLIP] != 0 || exists[SUBJ] != 0) {
            /* Set bundle side */
            edge!!.bside[CLIP] = parity[CLIP]
            edge!!.bside[SUBJ] = parity[SUBJ]
            var contributing = false
            var br = 0
            var bl = 0
            var tr = 0
            var tl = 0
            /* Determine contributing status and quadrant occupancies */
            if (op === OperationType.GPC_DIFF || op === OperationType.GPC_INT) {
              contributing =
                exists[CLIP] != 0 && (parity[SUBJ] != 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] != 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
              br = if (parity[CLIP] != 0 && parity[SUBJ] != 0) 1 else 0
              bl =
                if (parity[CLIP] xor edge!!.bundle[ABOVE][CLIP] != 0 && parity[SUBJ] xor edge!!.bundle[ABOVE][SUBJ] != 0) 1 else 0
              tr =
                if ((parity[CLIP] xor if (horiz[CLIP] != HState.NH) 1 else 0) != 0 && (parity[SUBJ] xor if (horiz[SUBJ] != HState.NH) 1 else 0) != 0) 1 else 0
              tl =
                if (parity[CLIP] xor (if (horiz[CLIP] != HState.NH) 1 else 0) xor edge!!.bundle[BELOW][CLIP] != 0 && parity[SUBJ] xor (if (horiz[SUBJ] != HState.NH) 1 else 0) xor edge!!.bundle[BELOW][SUBJ] != 0) 1 else 0
            } else if (op === OperationType.GPC_XOR) {
              contributing = exists[CLIP] != 0 || exists[SUBJ] != 0
              br = parity[CLIP] xor parity[SUBJ]
              bl =
                parity[CLIP] xor edge!!.bundle[ABOVE][CLIP] xor (parity[SUBJ] xor edge!!.bundle[ABOVE][SUBJ])
              tr =
                parity[CLIP] xor if (horiz[CLIP] != HState.NH) 1 else 0 xor (parity[SUBJ] xor if (horiz[SUBJ] != HState.NH) 1 else 0)
              tl =
                (parity[CLIP] xor (if (horiz[CLIP] != HState.NH) 1 else 0) xor edge!!.bundle[BELOW][CLIP] xor (parity[SUBJ] xor (if (horiz[SUBJ] != HState.NH) 1 else 0) xor edge!!.bundle[BELOW][SUBJ]))
            } else if (op === OperationType.GPC_UNION) {
              contributing =
                exists[CLIP] != 0 && (parity[SUBJ] == 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] == 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
              br = if (parity[CLIP] != 0 || parity[SUBJ] != 0) 1 else 0
              bl =
                if (parity[CLIP] xor edge!!.bundle[ABOVE][CLIP] != 0 || parity[SUBJ] xor edge!!.bundle[ABOVE][SUBJ] != 0) 1 else 0
              tr =
                if ((parity[CLIP] xor if (horiz[CLIP] != HState.NH) 1 else 0) != 0 || (parity[SUBJ] xor if (horiz[SUBJ] != HState.NH) 1 else 0) != 0) 1 else 0
              tl =
                if (parity[CLIP] xor (if (horiz[CLIP] != HState.NH) 1 else 0) xor edge!!.bundle[BELOW][CLIP] != 0 || parity[SUBJ] xor (if (horiz[SUBJ] != HState.NH) 1 else 0) xor edge!!.bundle[BELOW][SUBJ] != 0) 1 else 0
            } else {
              throw IllegalStateException("Unknown op")
            }

            /* Update parity */
            parity[CLIP] = parity[CLIP] xor edge!!.bundle[ABOVE][CLIP]
            parity[SUBJ] = parity[SUBJ] xor edge!!.bundle[ABOVE][SUBJ]

            /* Update horizontal state */
            if (exists[CLIP] != 0) {
              horiz[CLIP] =
                HState.next_h_state[horiz[CLIP]][(exists[CLIP] - 1 shl 1) + parity[CLIP]]
            }
            if (exists[SUBJ] != 0) {
              horiz[SUBJ] =
                HState.next_h_state[horiz[SUBJ]][(exists[SUBJ] - 1 shl 1) + parity[SUBJ]]
            }
            if (contributing) {
              val xb = edge!!.xb
              val vclass = VertexType.getType(tr, tl, br, bl)
              when (vclass) {
                VertexType.EMN, VertexType.IMN -> {
                  edge!!.outp[ABOVE] = out_poly.add_local_min(xb, yb)
                  px = xb
                  cf = edge!!.outp[ABOVE]
                }
                VertexType.ERI -> {
                  if (xb != px) {
                    cf!!.add_right(xb, yb)
                    px = xb
                  }
                  edge!!.outp[ABOVE] = cf
                  cf = null
                }
                VertexType.ELI -> {
                  edge!!.outp[BELOW]!!.add_left(xb, yb)
                  px = xb
                  cf = edge!!.outp[BELOW]
                }
                VertexType.EMX -> {
                  if (xb != px) {
                    cf!!.add_left(xb, yb)
                    px = xb
                  }
                  out_poly.merge_right(cf, edge!!.outp[BELOW])
                  cf = null
                }
                VertexType.ILI -> {
                  if (xb != px) {
                    cf!!.add_left(xb, yb)
                    px = xb
                  }
                  edge!!.outp[ABOVE] = cf
                  cf = null
                }
                VertexType.IRI -> {
                  edge!!.outp[BELOW]!!.add_right(xb, yb)
                  px = xb
                  cf = edge!!.outp[BELOW]
                  edge!!.outp[BELOW] = null
                }
                VertexType.IMX -> {
                  if (xb != px) {
                    cf!!.add_right(xb, yb)
                    px = xb
                  }
                  out_poly.merge_left(cf!!, edge!!.outp[BELOW]!!)
                  cf = null
                  edge!!.outp[BELOW] = null
                }
                VertexType.IMM -> {
                  if (xb != px) {
                    cf!!.add_right(xb, yb)
                    px = xb
                  }
                  out_poly.merge_left(cf!!, edge!!.outp[BELOW]!!)
                  edge!!.outp[BELOW] = null
                  edge!!.outp[ABOVE] = out_poly.add_local_min(xb, yb)
                  cf = edge!!.outp[ABOVE]
                }
                VertexType.EMM -> {
                  if (xb != px) {
                    cf!!.add_left(xb, yb)
                    px = xb
                  }
                  out_poly.merge_right(cf, edge!!.outp[BELOW])
                  edge!!.outp[BELOW] = null
                  edge!!.outp[ABOVE] = out_poly.add_local_min(xb, yb)
                  cf = edge!!.outp[ABOVE]
                }
                VertexType.LED -> {
                  if (edge!!.bot.y == yb) edge!!.outp[BELOW]!!.add_left(xb, yb)
                  edge!!.outp[ABOVE] = edge!!.outp[BELOW]
                  px = xb
                }
                VertexType.RED -> {
                  if (edge!!.bot.y == yb) edge!!.outp[BELOW]!!.add_right(xb, yb)
                  edge!!.outp[ABOVE] = edge!!.outp[BELOW]
                  px = xb
                }
                else -> {
                }
              }
            } /* End of contributing conditional */
          } /* End of edge exists conditional */
          if (DEBUG) {
            out_poly.print()
          }
          edge = edge!!.next
        }
      }

      /* Delete terminating edges from the AET, otherwise compute xt */
      var edge = aet.top_node
      while (edge != null) {
        if (edge!!.top.y == yb) {
          val prev_edge = edge!!.prev
          val next_edge = edge!!.next
          if (prev_edge != null) prev_edge.next = next_edge else aet.top_node = next_edge
          if (next_edge != null) next_edge.prev = prev_edge

          /* Copy bundle head state to the adjacent tail edge if required */
          if (edge!!.bstate[BELOW] === BundleState.BUNDLE_HEAD && prev_edge != null) {
            if (prev_edge.bstate[BELOW] === BundleState.BUNDLE_TAIL) {
              prev_edge.outp[BELOW] = edge!!.outp[BELOW]
              prev_edge.bstate[BELOW] = BundleState.UNBUNDLED
              if (prev_edge.prev != null) {
                if (prev_edge.prev!!.bstate[BELOW] === BundleState.BUNDLE_TAIL) {
                  prev_edge.bstate[BELOW] = BundleState.BUNDLE_HEAD
                }
              }
            }
          }
        } else {
          if (edge!!.top.y == yt) edge!!.xt = edge!!.top.x else edge!!.xt =
            edge!!.bot.x + edge!!.dx * (yt - edge!!.bot.y)
        }
        edge = edge!!.next
      }
      if (scanbeam < sbte.sbt_entries) {
        /* === SCANBEAM INTERIOR PROCESSING ============================== */

        /* Build intersection table for the current scanbeam */
        val it_table = ItNodeTable()
        it_table.build_intersection_table(aet, dy)

        /* Process each node in the intersection table */
        var intersect = it_table.top_node
        while (intersect != null) {
          e0 = intersect.ie[0]
          e1 = intersect.ie[1]

          /* Only generate output for contributing intersections */
          if ((e0.bundle[ABOVE][CLIP] != 0 || e0.bundle[ABOVE][SUBJ] != 0) && (e1.bundle[ABOVE][CLIP] != 0 || e1.bundle[ABOVE][SUBJ] != 0)) {
            val p = e0.outp[ABOVE]
            val q = e1.outp[ABOVE]
            val ix = intersect.point.x
            val iy = intersect.point.y + yb
            val inClip =
              if (e0.bundle[ABOVE][CLIP] != 0 && e0.bside[CLIP] == 0 || e1.bundle[ABOVE][CLIP] != 0 && e1.bside[CLIP] != 0 || e0.bundle[ABOVE][CLIP] == 0 && e1.bundle[ABOVE][CLIP] == 0 && e0.bside[CLIP] != 0 && e1.bside[CLIP] != 0) 1 else 0
            val inSubj =
              if (e0.bundle[ABOVE][SUBJ] != 0 && e0.bside[SUBJ] == 0 || e1.bundle[ABOVE][SUBJ] != 0 && e1.bside[SUBJ] != 0 || e0.bundle[ABOVE][SUBJ] == 0 && e1.bundle[ABOVE][SUBJ] == 0 && e0.bside[SUBJ] != 0 && e1.bside[SUBJ] != 0) 1 else 0
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
            val vclass = VertexType.getType(tr, tl, br, bl)
            when (vclass) {
              VertexType.EMN -> {
                e0.outp[ABOVE] = out_poly.add_local_min(ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
              }
              VertexType.ERI -> if (p != null) {
                p.add_right(ix, iy)
                e1.outp[ABOVE] = p
                e0.outp[ABOVE] = null
              }
              VertexType.ELI -> if (q != null) {
                q.add_left(ix, iy)
                e0.outp[ABOVE] = q
                e1.outp[ABOVE] = null
              }
              VertexType.EMX -> if (p != null && q != null) {
                p.add_left(ix, iy)
                out_poly.merge_right(p, q)
                e0.outp[ABOVE] = null
                e1.outp[ABOVE] = null
              }
              VertexType.IMN -> {
                e0.outp[ABOVE] = out_poly.add_local_min(ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
              }
              VertexType.ILI -> if (p != null) {
                p.add_left(ix, iy)
                e1.outp[ABOVE] = p
                e0.outp[ABOVE] = null
              }
              VertexType.IRI -> if (q != null) {
                q.add_right(ix, iy)
                e0.outp[ABOVE] = q
                e1.outp[ABOVE] = null
              }
              VertexType.IMX -> if (p != null && q != null) {
                p.add_right(ix, iy)
                out_poly.merge_left(p, q)
                e0.outp[ABOVE] = null
                e1.outp[ABOVE] = null
              }
              VertexType.IMM -> if (p != null && q != null) {
                p.add_right(ix, iy)
                out_poly.merge_left(p, q)
                e0.outp[ABOVE] = out_poly.add_local_min(ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
              }
              VertexType.EMM -> if (p != null && q != null) {
                p.add_left(ix, iy)
                out_poly.merge_right(p, q)
                e0.outp[ABOVE] = out_poly.add_local_min(ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
              }
              else -> {
              }
            }
          } /* End of contributing intersection conditional */

          /* Swap bundle sides in response to edge crossing */
          if (e0.bundle[ABOVE][CLIP] != 0) e1.bside[CLIP] = if (e1.bside[CLIP] == 0) 1 else 0
          if (e1.bundle[ABOVE][CLIP] != 0) e0.bside[CLIP] = if (e0.bside[CLIP] == 0) 1 else 0
          if (e0.bundle[ABOVE][SUBJ] != 0) e1.bside[SUBJ] = if (e1.bside[SUBJ] == 0) 1 else 0
          if (e1.bundle[ABOVE][SUBJ] != 0) e0.bside[SUBJ] = if (e0.bside[SUBJ] == 0) 1 else 0

          /* Swap e0 and e1 bundles in the AET */
          var prev_edge = e0.prev
          val next_edge = e1.next
          if (next_edge != null) {
            next_edge.prev = e0
          }
          if (e0.bstate[ABOVE] === BundleState.BUNDLE_HEAD) {
            var search = true
            while (search) {
              prev_edge = prev_edge!!.prev
              if (prev_edge != null) {
                if (prev_edge.bstate[ABOVE] !== BundleState.BUNDLE_TAIL) {
                  search = false
                }
              } else {
                search = false
              }
            }
          }
          if (prev_edge == null) {
            aet.top_node!!.prev = e1
            e1.next = aet.top_node
            aet.top_node = e0.next
          } else {
            prev_edge.next!!.prev = e1
            e1.next = prev_edge.next
            prev_edge.next = e0.next
          }
          e0.next!!.prev = prev_edge
          e1.next!!.prev = e1
          e0.next = next_edge
          if (DEBUG) {
            out_poly.print()
          }
          intersect = intersect.next
        }

        /* Prepare for next scanbeam */
        var edge = aet.top_node
        while (edge != null) {
          val next_edge = edge!!.next
          val succ_edge = edge!!.succ
          if (edge!!.top.y == yt && succ_edge != null) {
            /* Replace AET edge by its successor */
            succ_edge.outp[BELOW] = edge!!.outp[ABOVE]
            succ_edge.bstate[BELOW] = edge!!.bstate[ABOVE]
            succ_edge.bundle[BELOW][CLIP] = edge!!.bundle[ABOVE][CLIP]
            succ_edge.bundle[BELOW][SUBJ] = edge!!.bundle[ABOVE][SUBJ]
            val prev_edge = edge!!.prev
            if (prev_edge != null) prev_edge.next = succ_edge else aet.top_node = succ_edge
            if (next_edge != null) next_edge.prev = succ_edge
            succ_edge.prev = prev_edge
            succ_edge.next = next_edge
          } else {
            /* Update this edge */
            edge!!.outp[BELOW] = edge!!.outp[ABOVE]
            edge!!.bstate[BELOW] = edge!!.bstate[ABOVE]
            edge!!.bundle[BELOW][CLIP] = edge!!.bundle[ABOVE][CLIP]
            edge!!.bundle[BELOW][SUBJ] = edge!!.bundle[ABOVE][SUBJ]
            edge!!.xb = edge!!.xt
          }
          edge!!.outp[ABOVE] = null
          edge = edge!!.next
        }
      }
    } /* === END OF SCANBEAM PROCESSING ================================== */

    /* Generate result polygon from out_poly */
    result = out_poly.getResult(polyClass)
    return result
  }

  /**
   * Clipper to output tristrips
   */
  private fun clip(op: OperationType, subj: RPolygon, clip: RPolygon): RMesh {
    if (RG.useFastClip) {
      return FastRClip.clip(op, subj, clip)
    }
    var tlist: PolygonNode? = null
    var tnn: PolygonNode
    var tn: PolygonNode?
    var prev_edge: EdgeNode?
    var next_edge: EdgeNode?
    var edge: EdgeNode?
    var cf: EdgeNode? = null
    var succ_edge: EdgeNode?
    var e0: EdgeNode?
    var e1: EdgeNode
    var lt: VertexNode?
    var ltn: VertexNode
    var rt: VertexNode?
    var rtn: VertexNode
    var cft = VertexType.LED
    val sbt: FloatArray
    var xb: Float
    var px: Float
    var nx = 0f
    var yb: Float
    var yt: Float
    var dy: Float
    var ix: Float
    var iy: Float

    /* Test for trivial NULL result cases */
    if (subj.isEmpty && clip.isEmpty || subj.isEmpty && (op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) || clip.isEmpty && op === OperationType.GPC_INT) {
      return RMesh()
    }

    /* Identify potentialy contributing contours */
    if ((op === OperationType.GPC_INT || op === OperationType.GPC_DIFF) && !subj.isEmpty && !clip.isEmpty) {
      minimax_test(subj, clip, op)
    }

    /* Build LMT */
    val lmt_table = LmtTable()
    val sbte = ScanBeamTreeEntries()
    if (!subj.isEmpty) {
      build_lmt(lmt_table, sbte, subj, SUBJ, op)
    }
    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for subj ---------")
      lmt_table.print()
    }
    if (!clip.isEmpty) {
      build_lmt(lmt_table, sbte, clip, CLIP, op)
    }
    if (DEBUG) {
      println()
      println(" ------------ After build_lmt for clip ---------")
      lmt_table.print()
    }

    /* Return a NULL result if no contours contribute */
    if (lmt_table.top_node == null) {
      return RMesh()
    }

    /* Build scanbeam table from scanbeam tree */
    sbt = sbte.build_sbt()
    val parity = IntArray(2)
    parity[0] = LEFT
    parity[1] = LEFT

    /* Invert clip polygon for difference operation */
    if (op === OperationType.GPC_DIFF) {
      parity[CLIP] = RIGHT
    }
    if (DEBUG) {
      print_sbt(sbt)
    }
    var local_min = lmt_table.top_node
    val aet = AetTree()
    var scanbeam = 0

    /* Process each scanbeam */
    while (scanbeam < sbt.size) {
      /* Set yb and yt to the bottom and top of the scanbeam */
      yb = sbt[scanbeam++]
      yt = 0.0f
      dy = 0.0f
      if (scanbeam < sbt.size) {
        yt = sbt[scanbeam]
        dy = yt - yb
      }

      /* === SCANBEAM BOUNDARY PROCESSING ================================ */

      /* If LMT node corresponding to yb exists */
      if (local_min != null) {
        if (local_min.y == yb) {
          /* Add edges starting at this local minimum to the AET */
          edge = local_min.first_bound
          while (edge != null) {
            add_edge_to_aet(aet, edge)
            edge = edge.next_bound
          }
          local_min = local_min.next
        }
      }
      if (DEBUG) {
        aet.print()
      }
      /* Set dummy previous x value */
      px = -Float.MAX_VALUE

      /* Create bundles within AET */
      e0 = aet.top_node

      /* Set up bundle fields of first edge */
      aet.top_node!!.bundle[ABOVE][aet.top_node!!.type] = if (aet.top_node!!.top.y != yb) 1 else 0
      aet.top_node!!.bundle[ABOVE][if (aet.top_node!!.type == 0) 1 else 0] = 0
      aet.top_node!!.bstate[ABOVE] = BundleState.UNBUNDLED
      next_edge = aet.top_node!!.next
      while (next_edge != null) {
        val ne_type = next_edge.type
        val ne_type_opp = if (next_edge.type == 0) 1 else 0 //next edge type opposite

        /* Set up bundle fields of next edge */
        next_edge.bundle[ABOVE][ne_type] = if (next_edge.top.y != yb) 1 else 0
        next_edge.bundle[ABOVE][ne_type_opp] = 0
        next_edge.bstate[ABOVE] = BundleState.UNBUNDLED

        /* Bundle edges above the scanbeam boundary if they coincide */
        if (next_edge.bundle[ABOVE][ne_type] == 1) {
          if (EQ(e0!!.xb, next_edge.xb) && EQ(e0.dx, next_edge.dx) && e0.top.y != yb) {
            next_edge.bundle[ABOVE][ne_type] =
              next_edge.bundle[ABOVE][ne_type] xor e0.bundle[ABOVE][ne_type]
            next_edge.bundle[ABOVE][ne_type_opp] = e0.bundle[ABOVE][ne_type_opp]
            next_edge.bstate[ABOVE] = BundleState.BUNDLE_HEAD
            e0.bundle[ABOVE][CLIP] = 0
            e0.bundle[ABOVE][SUBJ] = 0
            e0.bstate[ABOVE] = BundleState.BUNDLE_TAIL
          }
          e0 = next_edge
        }
        next_edge = next_edge.next
      }
      val horiz = IntArray(2)
      horiz[CLIP] = HState.NH
      horiz[SUBJ] = HState.NH
      val exists = IntArray(2)
      exists[CLIP] = 0
      exists[SUBJ] = 0

      /* Process each edge at this scanbeam boundary */
      edge = aet.top_node
      while (edge != null) {
        exists[CLIP] = edge.bundle[ABOVE][CLIP] + (edge.bundle[BELOW][CLIP] shl 1)
        exists[SUBJ] = edge.bundle[ABOVE][SUBJ] + (edge.bundle[BELOW][SUBJ] shl 1)
        if (exists[CLIP] != 0 || exists[SUBJ] != 0) {
          /* Set bundle side */
          edge.bside[CLIP] = parity[CLIP]
          edge.bside[SUBJ] = parity[SUBJ]
          var contributing = false
          var br = 0
          var bl = 0
          var tr = 0
          var tl = 0
          /* Determine contributing status and quadrant occupancies */
          if (op === OperationType.GPC_DIFF || op === OperationType.GPC_INT) {
            contributing =
              exists[CLIP] != 0 && (parity[SUBJ] != 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] != 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
            br = if (parity[CLIP] != 0 && parity[SUBJ] != 0) 1 else 0
            bl =
              if (parity[CLIP] xor edge.bundle[ABOVE][CLIP] != 0 && parity[SUBJ] xor edge.bundle[ABOVE][SUBJ] != 0) 1 else 0
            tr =
              if ((parity[CLIP] xor if (horiz[CLIP] != HState.NH) 1 else 0) != 0 && (parity[SUBJ] xor if (horiz[SUBJ] != HState.NH) 1 else 0) != 0) 1 else 0
            tl =
              if (parity[CLIP] xor (if (horiz[CLIP] != HState.NH) 1 else 0) xor edge.bundle[BELOW][CLIP] != 0 && parity[SUBJ] xor (if (horiz[SUBJ] != HState.NH) 1 else 0) xor edge.bundle[BELOW][SUBJ] != 0) 1 else 0
          } else if (op === OperationType.GPC_XOR) {
            contributing = exists[CLIP] != 0 || exists[SUBJ] != 0
            br = parity[CLIP] xor parity[SUBJ]
            bl =
              parity[CLIP] xor edge.bundle[ABOVE][CLIP] xor (parity[SUBJ] xor edge.bundle[ABOVE][SUBJ])
            tr =
              parity[CLIP] xor if (horiz[CLIP] != HState.NH) 1 else 0 xor (parity[SUBJ] xor if (horiz[SUBJ] != HState.NH) 1 else 0)
            tl =
              (parity[CLIP] xor (if (horiz[CLIP] != HState.NH) 1 else 0) xor edge.bundle[BELOW][CLIP] xor (parity[SUBJ] xor (if (horiz[SUBJ] != HState.NH) 1 else 0) xor edge.bundle[BELOW][SUBJ]))
          } else if (op === OperationType.GPC_UNION) {
            contributing =
              exists[CLIP] != 0 && (parity[SUBJ] == 0 || horiz[SUBJ] != 0) || exists[SUBJ] != 0 && (parity[CLIP] == 0 || horiz[CLIP] != 0) || exists[CLIP] != 0 && exists[SUBJ] != 0 && parity[CLIP] == parity[SUBJ]
            br = if (parity[CLIP] != 0 || parity[SUBJ] != 0) 1 else 0
            bl =
              if (parity[CLIP] xor edge.bundle[ABOVE][CLIP] != 0 || parity[SUBJ] xor edge.bundle[ABOVE][SUBJ] != 0) 1 else 0
            tr =
              if ((parity[CLIP] xor if (horiz[CLIP] != HState.NH) 1 else 0) != 0 || (parity[SUBJ] xor if (horiz[SUBJ] != HState.NH) 1 else 0) != 0) 1 else 0
            tl =
              if (parity[CLIP] xor (if (horiz[CLIP] != HState.NH) 1 else 0) xor edge.bundle[BELOW][CLIP] != 0 || parity[SUBJ] xor (if (horiz[SUBJ] != HState.NH) 1 else 0) xor edge.bundle[BELOW][SUBJ] != 0) 1 else 0
          } else {
            throw IllegalStateException("Unknown op")
          }

          /* Update parity */
          parity[CLIP] = parity[CLIP] xor edge.bundle[ABOVE][CLIP]
          parity[SUBJ] = parity[SUBJ] xor edge.bundle[ABOVE][SUBJ]

          /* Update horizontal state */
          if (exists[CLIP] != 0) {
            horiz[CLIP] = HState.next_h_state[horiz[CLIP]][(exists[CLIP] - 1 shl 1) + parity[CLIP]]
          }
          if (exists[SUBJ] != 0) {
            horiz[SUBJ] = HState.next_h_state[horiz[SUBJ]][(exists[SUBJ] - 1 shl 1) + parity[SUBJ]]
          }
          if (contributing) {
            xb = edge.xb
            val vclass = VertexType.getType(tr, tl, br, bl)
            when (vclass) {
              VertexType.EMN -> {
                tlist = new_tristrip(tlist, edge, xb, yb)
                cf = edge
              }
              VertexType.ERI -> {
                edge.outp[ABOVE] = cf!!.outp[ABOVE]
                if (xb != cf.xb) {
                  VERTEX(edge, ABOVE, RIGHT, xb, yb)
                }
                cf = null
              }
              VertexType.ELI -> {
                VERTEX(edge, BELOW, LEFT, xb, yb)
                edge.outp[ABOVE] = null
                cf = edge
              }
              VertexType.EMX -> {
                if (xb != cf!!.xb) {
                  VERTEX(edge, BELOW, RIGHT, xb, yb)
                }
                edge.outp[ABOVE] = null
                cf = null
              }
              VertexType.IMN -> {
                if (cft == VertexType.LED) {
                  if (cf!!.bot.y != yb) {
                    VERTEX(cf, BELOW, LEFT, cf.xb, yb)
                  }
                  tlist = new_tristrip(tlist, cf, cf.xb, yb)
                }
                edge.outp[ABOVE] = cf!!.outp[ABOVE]
                VERTEX(edge, ABOVE, RIGHT, xb, yb)
              }
              VertexType.ILI -> {
                tlist = new_tristrip(tlist, edge, xb, yb)
                cf = edge
                cft = VertexType.ILI
              }
              VertexType.IRI -> {
                if (cft == VertexType.LED) {
                  if (cf!!.bot.y != yb) {
                    VERTEX(cf, BELOW, LEFT, cf.xb, yb)
                  }
                  tlist = new_tristrip(tlist, cf, cf.xb, yb)
                }
                VERTEX(edge, BELOW, RIGHT, xb, yb)
                edge.outp[ABOVE] = null
              }
              VertexType.IMX -> {
                VERTEX(edge, BELOW, LEFT, xb, yb)
                edge.outp[ABOVE] = null
                cft = VertexType.IMX
              }
              VertexType.IMM -> {
                VERTEX(edge, BELOW, LEFT, xb, yb)
                edge.outp[ABOVE] = cf!!.outp[ABOVE]
                if (xb != cf.xb) {
                  VERTEX(cf, ABOVE, RIGHT, xb, yb)
                }
                cf = edge
              }
              VertexType.EMM -> {
                VERTEX(edge, BELOW, RIGHT, xb, yb)
                edge.outp[ABOVE] = null
                tlist = new_tristrip(tlist, edge, xb, yb)
                cf = edge
              }
              VertexType.LED -> {
                if (edge.bot.y == yb) VERTEX(edge, BELOW, LEFT, xb, yb)
                edge.outp[ABOVE] = edge.outp[BELOW]
                cf = edge
                cft = VertexType.LED
              }
              VertexType.RED -> {
                edge.outp[ABOVE] = cf!!.outp[ABOVE]
                if (cft == VertexType.LED) {
                  if (cf.bot.y == yb) {
                    VERTEX(edge, BELOW, RIGHT, xb, yb)
                  } else {
                    if (edge.bot.y == yb) {
                      VERTEX(cf, BELOW, LEFT, cf.xb, yb)
                      VERTEX(edge, BELOW, RIGHT, xb, yb)
                    }
                  }
                } else {
                  VERTEX(edge, BELOW, RIGHT, xb, yb)
                  VERTEX(edge, ABOVE, RIGHT, xb, yb)
                }
                cf = null
              }
              else -> {
              }
            }
          } /* End of contributing conditional */
        } /* End of edge exists conditional */
        edge = edge.next
      }

      /* Delete terminating edges from the AET, otherwise compute xt */
      edge = aet.top_node
      while (edge != null) {
        if (edge.top.y == yb) {
          prev_edge = edge.prev
          next_edge = edge.next
          if (prev_edge != null) prev_edge.next = next_edge else aet.top_node = next_edge
          if (next_edge != null) next_edge.prev = prev_edge

          /* Copy bundle head state to the adjacent tail edge if required */
          if (edge.bstate[BELOW] === BundleState.BUNDLE_HEAD && prev_edge != null) {
            if (prev_edge.bstate[BELOW] === BundleState.BUNDLE_TAIL) {
              prev_edge.outp[BELOW] = edge.outp[BELOW]
              prev_edge.bstate[BELOW] = BundleState.UNBUNDLED
              if (prev_edge.prev != null) {
                if (prev_edge.prev!!.bstate[BELOW] === BundleState.BUNDLE_TAIL) {
                  prev_edge.bstate[BELOW] = BundleState.BUNDLE_HEAD
                }
              }
            }
          }
        } else {
          if (edge.top.y == yt) edge.xt = edge.top.x else edge.xt =
            edge.bot.x + edge.dx * (yt - edge.bot.y)
        }
        edge = edge.next
      }
      if (scanbeam < sbte.sbt_entries) {
        /* === SCANBEAM INTERIOR PROCESSING ============================== */
        /* Build intersection table for the current scanbeam */
        val it_table = ItNodeTable()
        it_table.build_intersection_table(aet, dy)

        /* Process each node in the intersection table */
        var intersect = it_table.top_node
        while (intersect != null) {
          e0 = intersect.ie[0]
          e1 = intersect.ie[1]

          /* Only generate output for contributing intersections */
          if ((e0.bundle[ABOVE][CLIP] != 0 || e0.bundle[ABOVE][SUBJ] != 0) && (e1.bundle[ABOVE][CLIP] != 0 || e1.bundle[ABOVE][SUBJ] != 0)) {
            val p = e0.outp[ABOVE]
            val q = e1.outp[ABOVE]
            ix = intersect.point.x
            iy = intersect.point.y + yb
            val in_clip =
              if (e0.bundle[ABOVE][CLIP] != 0 && e0.bside[CLIP] == 0 || e1.bundle[ABOVE][CLIP] != 0 && e1.bside[CLIP] != 0 || e0.bundle[ABOVE][CLIP] == 0 && e1.bundle[ABOVE][CLIP] == 0 && e0.bside[CLIP] != 0 && e1.bside[CLIP] != 0) 1 else 0
            val in_subj =
              if (e0.bundle[ABOVE][SUBJ] != 0 && e0.bside[SUBJ] == 0 || e1.bundle[ABOVE][SUBJ] != 0 && e1.bside[SUBJ] != 0 || e0.bundle[ABOVE][SUBJ] == 0 && e1.bundle[ABOVE][SUBJ] == 0 && e0.bside[SUBJ] != 0 && e1.bside[SUBJ] != 0) 1 else 0
            var tr = 0
            var tl = 0
            var br = 0
            var bl = 0
            /* Determine quadrant occupancies */
            if (op === OperationType.GPC_DIFF || op === OperationType.GPC_INT) {
              tr = if (in_clip != 0 && in_subj != 0) 1 else 0
              tl =
                if (in_clip xor e1.bundle[ABOVE][CLIP] != 0 && in_subj xor e1.bundle[ABOVE][SUBJ] != 0) 1 else 0
              br =
                if (in_clip xor e0.bundle[ABOVE][CLIP] != 0 && in_subj xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
              bl =
                if (in_clip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] != 0 && in_subj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
            } else if (op === OperationType.GPC_XOR) {
              tr = in_clip xor in_subj
              tl = in_clip xor e1.bundle[ABOVE][CLIP] xor (in_subj xor e1.bundle[ABOVE][SUBJ])
              br = in_clip xor e0.bundle[ABOVE][CLIP] xor (in_subj xor e0.bundle[ABOVE][SUBJ])
              bl =
                (in_clip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] xor (in_subj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ]))
            } else if (op === OperationType.GPC_UNION) {
              tr = if (in_clip != 0 || in_subj != 0) 1 else 0
              tl =
                if (in_clip xor e1.bundle[ABOVE][CLIP] != 0 || in_subj xor e1.bundle[ABOVE][SUBJ] != 0) 1 else 0
              br =
                if (in_clip xor e0.bundle[ABOVE][CLIP] != 0 || in_subj xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
              bl =
                if (in_clip xor e1.bundle[ABOVE][CLIP] xor e0.bundle[ABOVE][CLIP] != 0 || in_subj xor e1.bundle[ABOVE][SUBJ] xor e0.bundle[ABOVE][SUBJ] != 0) 1 else 0
            } else {
              throw IllegalStateException("Unknown op type, $op")
            }
            next_edge = e1.next
            prev_edge = e0.prev
            val vclass = VertexType.getType(tr, tl, br, bl)
            when (vclass) {
              VertexType.EMN -> {
                tlist = new_tristrip(tlist, e1, ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
              }
              VertexType.ERI -> if (p != null) {
                px = P_EDGE(prev_edge, e0, ABOVE, px, iy)
                VERTEX(prev_edge, ABOVE, LEFT, px, iy)
                VERTEX(e0, ABOVE, RIGHT, ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
                e0.outp[ABOVE] = null
              }
              VertexType.ELI -> if (q != null) {
                nx = N_EDGE(next_edge, e1, ABOVE, nx, iy)
                VERTEX(e1, ABOVE, LEFT, ix, iy)
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
                e0.outp[ABOVE] = e1.outp[ABOVE]
                e1.outp[ABOVE] = null
              }
              VertexType.EMX -> if (p != null && q != null) {
                VERTEX(e0, ABOVE, LEFT, ix, iy)
                e0.outp[ABOVE] = null
                e1.outp[ABOVE] = null
              }
              VertexType.IMN -> {
                px = P_EDGE(prev_edge, e0, ABOVE, px, iy)
                VERTEX(prev_edge, ABOVE, LEFT, px, iy)
                nx = N_EDGE(next_edge, e1, ABOVE, nx, iy)
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
                tlist = new_tristrip(tlist, prev_edge, px, iy)
                e1.outp[ABOVE] = prev_edge!!.outp[ABOVE]
                VERTEX(e1, ABOVE, RIGHT, ix, iy)
                tlist = new_tristrip(tlist, e0, ix, iy)
                next_edge!!.outp[ABOVE] = e0.outp[ABOVE]
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
              }
              VertexType.ILI -> if (p != null) {
                VERTEX(e0, ABOVE, LEFT, ix, iy)
                nx = N_EDGE(next_edge, e1, ABOVE, nx, iy)
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
                e0.outp[ABOVE] = null
              }
              VertexType.IRI -> if (q != null) {
                VERTEX(e1, ABOVE, RIGHT, ix, iy)
                px = P_EDGE(prev_edge, e0, ABOVE, px, iy)
                VERTEX(prev_edge, ABOVE, LEFT, px, iy)
                e0.outp[ABOVE] = e1.outp[ABOVE]
                e1.outp[ABOVE] = null
              }
              VertexType.IMX -> if (p != null && q != null) {
                VERTEX(e0, ABOVE, RIGHT, ix, iy)
                VERTEX(e1, ABOVE, LEFT, ix, iy)
                e0.outp[ABOVE] = null
                e1.outp[ABOVE] = null
                px = P_EDGE(prev_edge, e0, ABOVE, px, iy)
                VERTEX(prev_edge, ABOVE, LEFT, px, iy)
                tlist = new_tristrip(tlist, prev_edge, px, iy)
                nx = N_EDGE(next_edge, e1, ABOVE, nx, iy)
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
                next_edge!!.outp[ABOVE] = prev_edge!!.outp[ABOVE]
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
              }
              VertexType.IMM -> if (p != null && q != null) {
                VERTEX(e0, ABOVE, RIGHT, ix, iy)
                VERTEX(e1, ABOVE, LEFT, ix, iy)
                px = P_EDGE(prev_edge, e0, ABOVE, px, iy)
                VERTEX(prev_edge, ABOVE, LEFT, px, iy)
                tlist = new_tristrip(tlist, prev_edge, px, iy)
                nx = N_EDGE(next_edge, e1, ABOVE, nx, iy)
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
                e1.outp[ABOVE] = prev_edge!!.outp[ABOVE]
                VERTEX(e1, ABOVE, RIGHT, ix, iy)
                tlist = new_tristrip(tlist, e0, ix, iy)
                next_edge!!.outp[ABOVE] = e0.outp[ABOVE]
                VERTEX(next_edge, ABOVE, RIGHT, nx, iy)
              }
              VertexType.EMM -> if (p != null && q != null) {
                VERTEX(e0, ABOVE, LEFT, ix, iy)
                tlist = new_tristrip(tlist, e1, ix, iy)
                e1.outp[ABOVE] = e0.outp[ABOVE]
              }
              else -> {
              }
            }
          } /* End of contributing intersection conditional */

          /* Swap bundle sides in response to edge crossing */
          if (e0.bundle[ABOVE][CLIP] != 0) e1.bside[CLIP] = if (e1.bside[CLIP] == 0) 1 else 0
          if (e1.bundle[ABOVE][CLIP] != 0) e0.bside[CLIP] = if (e0.bside[CLIP] == 0) 1 else 0
          if (e0.bundle[ABOVE][SUBJ] != 0) e1.bside[SUBJ] = if (e1.bside[SUBJ] == 0) 1 else 0
          if (e1.bundle[ABOVE][SUBJ] != 0) e0.bside[SUBJ] = if (e0.bside[SUBJ] == 0) 1 else 0

          /* Swap e0 and e1 bundles in the AET */
          prev_edge = e0.prev
          next_edge = e1.next
          if (next_edge != null) {
            next_edge.prev = e0
          }
          if (e0.bstate[ABOVE] === BundleState.BUNDLE_HEAD) {
            var search = true
            while (search) {
              prev_edge = prev_edge!!.prev
              if (prev_edge != null) {
                if (prev_edge.bundle[ABOVE][CLIP] != 0 || prev_edge.bundle[ABOVE][SUBJ] != 0 || prev_edge.bstate[ABOVE] === BundleState.BUNDLE_HEAD) {
                  search = false
                }
              } else {
                search = false
              }
            }
          }
          if (prev_edge == null) {
            e1.next = aet.top_node
            aet.top_node = e0.next
          } else {
            e1.next = prev_edge.next
            prev_edge.next = e0.next
          }
          e0.next!!.prev = prev_edge
          e1.next!!.prev = e1
          e0.next = next_edge
          intersect = intersect.next
        }

        /* Prepare for next scanbeam */
        edge = aet.top_node
        while (edge != null) {
          next_edge = edge.next
          succ_edge = edge.succ
          if (edge.top.y == yt && succ_edge != null) {
            /* Replace AET edge by its successor */
            succ_edge.outp[BELOW] = edge.outp[ABOVE]
            succ_edge.bstate[BELOW] = edge.bstate[ABOVE]
            succ_edge.bundle[BELOW][CLIP] = edge.bundle[ABOVE][CLIP]
            succ_edge.bundle[BELOW][SUBJ] = edge.bundle[ABOVE][SUBJ]
            prev_edge = edge.prev
            if (prev_edge != null) prev_edge.next = succ_edge else aet.top_node = succ_edge
            if (next_edge != null) next_edge.prev = succ_edge
            succ_edge.prev = prev_edge
            succ_edge.next = next_edge
          } else {
            /* Update this edge */
            edge.outp[BELOW] = edge.outp[ABOVE]
            edge.bstate[BELOW] = edge.bstate[ABOVE]
            edge.bundle[BELOW][CLIP] = edge.bundle[ABOVE][CLIP]
            edge.bundle[BELOW][SUBJ] = edge.bundle[ABOVE][SUBJ]
            edge.xb = edge.xt
          }
          edge.outp[ABOVE] = null
          edge = edge.next
        }
      }
    } /* === END OF SCANBEAM PROCESSING ================================== */

    /* Generate result tristrip from tlist */
    val result = RMesh()
    if (count_tristrips(tlist) > 0) {
      var s: Int
      var v: Int
      s = 0
      tn = tlist
      while (tn != null) {
        tnn = tn.next!!
        if (tn.active > 2) {
          /* Valid tristrip: copy the vertices and free the heap */
          val strip = RStrip()
          v = 0
          if (INVERT_TRISTRIPS == true) {
            lt = tn.v[RIGHT]
            rt = tn.v[LEFT]
          } else {
            lt = tn.v[LEFT]
            rt = tn.v[RIGHT]
          }
          while (lt != null || rt != null) {
            if (lt != null) {
              ltn = lt.next!!
              strip.add(lt.x, lt.y)
              v++
              lt = ltn
            }
            if (rt != null) {
              rtn = rt.next!!
              strip.add(rt.x, rt.y)
              v++
              rt = rtn
            }
          }
          result.addStrip(strip)
          s++
        } else {
          /* Invalid tristrip: just free the heap */
          lt = tn.v[LEFT]
          while (lt != null) {
            ltn = lt.next!!
            lt = ltn
          }
          rt = tn.v[RIGHT]
          while (rt != null) {
            rtn = rt.next!!
            rt = rtn
          }
        }
        tn = tnn
      }
    }
    return result
  }

  @JvmStatic
  fun polygonToMesh(s: RPolygon): RMesh {
    val c = RPolygon()
    val s_clean = s.removeOpenContours()
    /*
    for(int i=0; i<s_clean.contours.size; i++)
      {
        System.out.println("  " + s_clean.contours[i]);
        System.out.println("Contour " + (i + 1) + "/" + s_clean.contours.size + ":");
        for(int j=0;j<s_clean.contours[i].points.size;j++)
          {
            System.out.println("  Point " + (j + 1) + "/" + s_clean.contours[i].points.size + ":" + "(" + s_clean.contours[i].points[j].x + ", " + s_clean.contours[i].points[j].y + ")");
          }
      }
    */
    return clip(OperationType.GPC_UNION, s_clean, c)
  }

  private fun EQ(a: Float, b: Float): Boolean {
    return Math.abs(a - b) <= GPC_EPSILON
  }

  private fun PREV_INDEX(i: Int, n: Int): Int {
    return (i - 1 + n) % n
  }

  private fun NEXT_INDEX(i: Int, n: Int): Int {
    return (i + 1) % n
  }

  private fun OPTIMAL(p: RPolygon, i: Int): Boolean {
    return p.getY(PREV_INDEX(i, p.numPoints)) != p.getY(i) || p.getY(
      NEXT_INDEX(i, p.numPoints)
    ) != p.getY(i)
  }

  private fun VERTEX(e: EdgeNode?, p: Int, s: Int, x: Float, y: Float) {
    e!!.outp[p]!!.v[s] = add_vertex(e.outp[p]!!.v[s], x, y)
    e.outp[p]!!.active++
  }

  private fun P_EDGE(d: EdgeNode?, e: EdgeNode?, p: Int, i: Float, j: Float): Float {
    var d = d
    d = e
    do {
      d = d!!.prev
    } while (d!!.outp[p] == null)
    return d.bot.x + d.dx * (j - d.bot.y)
  }

  private fun N_EDGE(d: EdgeNode?, e: EdgeNode?, p: Int, i: Float, j: Float): Float {
    var d = d
    d = e
    do {
      d = d!!.next
    } while (d!!.outp[p] == null)
    return d.bot.x + d.dx * (j - d.bot.y)
  }

  private fun create_contour_bboxes(p: RPolygon): Array<RRectangle?> {
    val box = arrayOfNulls<RRectangle>(p.numInnerPoly)

    /* Construct contour bounding boxes */
    for (c in 0 until p.numInnerPoly) {
      val inner_poly = p.getInnerPoly(c)
      box[c] = inner_poly.bBox
    }
    return box
  }

  private fun minimax_test(subj: RPolygon, clip: RPolygon, op: OperationType) {
    val s_bbox = create_contour_bboxes(subj)
    val c_bbox = create_contour_bboxes(clip)
    val subj_num_poly = subj.numInnerPoly
    val clip_num_poly = clip.numInnerPoly
    val o_table = Array(subj_num_poly) { BooleanArray(clip_num_poly) }

    /* Check all subject contour bounding boxes against clip boxes */
    for (s in 0 until subj_num_poly) {
      for (c in 0 until clip_num_poly) {
        o_table[s][c] =
          !(s_bbox[s]!!.maxX < c_bbox[c]!!.minX || s_bbox[s]!!.minX > c_bbox[c]!!.maxX) && !(s_bbox[s]!!.maxY < c_bbox[c]!!.minY || s_bbox[s]!!.minY > c_bbox[c]!!.maxY)
      }
    }

    /* For each clip contour, search for any subject contour overlaps */
    for (c in 0 until clip_num_poly) {
      var overlap = false
      var s = 0
      while (!overlap && s < subj_num_poly) {
        overlap = o_table[s][c]
        s++
      }
      if (!overlap) {
        clip.setContributing(c, false) // Flag non contributing status
      }
    }
    if (op === OperationType.GPC_INT) {
      /* For each subject contour, search for any clip contour overlaps */
      for (s in 0 until subj_num_poly) {
        var overlap = false
        var c = 0
        while (!overlap && c < clip_num_poly) {
          overlap = o_table[s][c]
          c++
        }
        if (!overlap) {
          subj.setContributing(s, false) // Flag non contributing status
        }
      }
    }
  }

  private fun bound_list(lmt_table: LmtTable, y: Float): LmtNode? {
    return if (lmt_table.top_node == null) {
      lmt_table.top_node = LmtNode(y)
      lmt_table.top_node
    } else {
      var prev: LmtNode? = null
      var node = lmt_table.top_node
      var done = false
      while (!done) {
        if (y < node!!.y) {
          /* Insert a new LMT node before the current node */
          val existing_node = node
          node = LmtNode(y)
          node.next = existing_node
          if (prev == null) {
            lmt_table.top_node = node
          } else {
            prev.next = node
          }
          //               if( existing_node == lmt_table.top_node )
          //               {
          //                  lmt_table.top_node = node ;
          //               }
          done = true
        } else if (y > node.y) {
          /* Head further up the LMT */
          if (node.next == null) {
            node.next = LmtNode(y)
            node = node.next
            done = true
          } else {
            prev = node
            node = node.next
          }
        } else {
          /* Use this existing LMT node */
          done = true
        }
      }
      node
    }
  }

  private fun insert_bound(lmt_node: LmtNode?, e: EdgeNode) {
    if (lmt_node!!.first_bound == null) {
      /* Link node e to the tail of the list */
      lmt_node.first_bound = e
    } else {
      var done = false
      var prev_bound: EdgeNode? = null
      var current_bound = lmt_node.first_bound
      while (!done) {
        /* Do primary sort on the x field */
        if (e.bot.x < current_bound!!.bot.x) {
          /* Insert a new node mid-list */
          if (prev_bound == null) {
            lmt_node.first_bound = e
          } else {
            prev_bound.next_bound = e
          }
          e.next_bound = current_bound

          //               EdgeNode existing_bound = current_bound ;
          //               current_bound = e ;
          //               current_bound.next_bound = existing_bound ;
          //               if( lmt_node.first_bound == existing_bound )
          //               {
          //                  lmt_node.first_bound = current_bound ;
          //               }
          done = true
        } else if (e.bot.x == current_bound.bot.x) {
          /* Do secondary sort on the dx field */
          if (e.dx < current_bound.dx) {
            /* Insert a new node mid-list */
            if (prev_bound == null) {
              lmt_node.first_bound = e
            } else {
              prev_bound.next_bound = e
            }
            e.next_bound = current_bound
            //                  EdgeNode existing_bound = current_bound ;
            //                  current_bound = e ;
            //                  current_bound.next_bound = existing_bound ;
            //                  if( lmt_node.first_bound == existing_bound )
            //                  {
            //                     lmt_node.first_bound = current_bound ;
            //                  }
            done = true
          } else {
            /* Head further down the list */
            if (current_bound.next_bound == null) {
              current_bound.next_bound = e
              done = true
            } else {
              prev_bound = current_bound
              current_bound = current_bound.next_bound
            }
          }
        } else {
          /* Head further down the list */
          if (current_bound.next_bound == null) {
            current_bound.next_bound = e
            done = true
          } else {
            prev_bound = current_bound
            current_bound = current_bound.next_bound
          }
        }
      }
    }
  }

  private fun add_edge_to_aet(aet: AetTree, edge: EdgeNode) {
    if (aet.top_node == null) {
      /* Append edge onto the tail end of the AET */
      aet.top_node = edge
      edge.prev = null
      edge.next = null
    } else {
      var current_edge = aet.top_node
      var prev: EdgeNode? = null
      var done = false
      while (!done) {
        /* Do primary sort on the xb field */
        if (edge.xb < current_edge!!.xb) {
          /* Insert edge here (before the AET edge) */
          edge.prev = prev
          edge.next = current_edge
          current_edge.prev = edge
          if (prev == null) {
            aet.top_node = edge
          } else {
            prev.next = edge
          }
          //               if( current_edge == aet.top_node )
          //               {
          //                  aet.top_node = edge ;
          //               }
          //               current_edge = edge ;
          done = true
        } else if (edge.xb == current_edge.xb) {
          /* Do secondary sort on the dx field */
          if (edge.dx < current_edge.dx) {
            /* Insert edge here (before the AET edge) */
            edge.prev = prev
            edge.next = current_edge
            current_edge.prev = edge
            if (prev == null) {
              aet.top_node = edge
            } else {
              prev.next = edge
            }
            //                  if( current_edge == aet.top_node )
            //                  {
            //                     aet.top_node = edge ;
            //                  }
            //                  current_edge = edge ;
            done = true
          } else {
            /* Head further into the AET */
            prev = current_edge
            if (current_edge.next == null) {
              current_edge.next = edge
              edge.prev = current_edge
              edge.next = null
              done = true
            } else {
              current_edge = current_edge.next
            }
          }
        } else {
          /* Head further into the AET */
          prev = current_edge
          if (current_edge.next == null) {
            current_edge.next = edge
            edge.prev = current_edge
            edge.next = null
            done = true
          } else {
            current_edge = current_edge.next
          }
        }
      }
    }
  }

  private fun add_to_sbtree(sbte: ScanBeamTreeEntries, y: Float) {
    if (sbte.sb_tree == null) {
      /* Add a new tree node here */
      sbte.sb_tree = ScanBeamTree(y)
      sbte.sbt_entries++
      return
    }
    var tree_node = sbte.sb_tree
    var done = false
    while (!done) {
      if (tree_node!!.y > y) {
        if (tree_node.less == null) {
          tree_node.less = ScanBeamTree(y)
          sbte.sbt_entries++
          done = true
        } else {
          tree_node = tree_node.less
        }
      } else if (tree_node.y < y) {
        if (tree_node.more == null) {
          tree_node.more = ScanBeamTree(y)
          sbte.sbt_entries++
          done = true
        } else {
          tree_node = tree_node.more
        }
      } else {
        done = true
      }
    }
  }

  private fun build_lmt(
    lmt_table: LmtTable,
    sbte: ScanBeamTreeEntries,
    p: RPolygon,
    type: Int,  //poly type SUBJ/CLIP
    op: OperationType,
  ): EdgeTable {
    /* Create the entire input polygon edge table in one go */
    var edge_table = EdgeTable()
    for (c in 0 until p.numInnerPoly) {
      val ip = p.getInnerPoly(c)
      if (!ip.isContributing(0)) {
        /* Ignore the non-contributing contour */
        ip.setContributing(0, true)
      } else {
        /* Perform contour optimisation */
        var num_vertices = 0
        var e_index = 0
        edge_table = EdgeTable()
        for (i in 0 until ip.numPoints) {
          if (OPTIMAL(ip, i)) {
            val x = ip.getX(i)
            val y = ip.getY(i)
            edge_table.addNode(x, y)

            /* Record vertex in the scanbeam table */
            add_to_sbtree(sbte, ip.getY(i))
            num_vertices++
          }
        }

        /* Do the contour forward pass */
        for (min in 0 until num_vertices) {
          /* If a forward local minimum... */
          if (edge_table.FWD_MIN(min)) {
            /* Search for the next local maximum... */
            var num_edges = 1
            var max = NEXT_INDEX(min, num_vertices)
            while (edge_table.NOT_FMAX(max)) {
              num_edges++
              max = NEXT_INDEX(max, num_vertices)
            }

            /* Build the next edge list */
            var v = min
            val e = edge_table.getNode(e_index)
            e.bstate[BELOW] = BundleState.UNBUNDLED
            e.bundle[BELOW][CLIP] = 0
            e.bundle[BELOW][SUBJ] = 0
            for (i in 0 until num_edges) {
              val ei = edge_table.getNode(e_index + i)
              var ev = edge_table.getNode(v)
              ei.xb = ev.vertex.x
              ei.bot.x = ev.vertex.x
              ei.bot.y = ev.vertex.y
              v = NEXT_INDEX(v, num_vertices)
              ev = edge_table.getNode(v)
              ei.top.x = ev.vertex.x
              ei.top.y = ev.vertex.y
              ei.dx = (ev.vertex.x - ei.bot.x) / (ei.top.y - ei.bot.y)
              ei.type = type
              ei.outp[ABOVE] = null
              ei.outp[BELOW] = null
              ei.next = null
              ei.prev = null
              ei.succ =
                if (num_edges > 1 && i < num_edges - 1) edge_table.getNode(
                  e_index + i + 1
                ) else null
              ei.pred = if (num_edges > 1 && i > 0) edge_table.getNode(e_index + i - 1) else null
              ei.next_bound = null
              ei.bside[CLIP] = if (op === OperationType.GPC_DIFF) RIGHT else LEFT
              ei.bside[SUBJ] = LEFT
            }
            insert_bound(bound_list(lmt_table, edge_table.getNode(min).vertex.y), e)
            if (DEBUG) {
              println("fwd")
              lmt_table.print()
            }
            e_index += num_edges
          }
        }

        /* Do the contour reverse pass */
        for (min in 0 until num_vertices) {
          /* If a reverse local minimum... */
          if (edge_table.REV_MIN(min)) {
            /* Search for the previous local maximum... */
            var num_edges = 1
            var max = PREV_INDEX(min, num_vertices)
            while (edge_table.NOT_RMAX(max)) {
              num_edges++
              max = PREV_INDEX(max, num_vertices)
            }

            /* Build the previous edge list */
            var v = min
            val e = edge_table.getNode(e_index)
            e.bstate[BELOW] = BundleState.UNBUNDLED
            e.bundle[BELOW][CLIP] = 0
            e.bundle[BELOW][SUBJ] = 0
            for (i in 0 until num_edges) {
              val ei = edge_table.getNode(e_index + i)
              var ev = edge_table.getNode(v)
              ei.xb = ev.vertex.x
              ei.bot.x = ev.vertex.x
              ei.bot.y = ev.vertex.y
              v = PREV_INDEX(v, num_vertices)
              ev = edge_table.getNode(v)
              ei.top.x = ev.vertex.x
              ei.top.y = ev.vertex.y
              ei.dx = (ev.vertex.x - ei.bot.x) / (ei.top.y - ei.bot.y)
              ei.type = type
              ei.outp[ABOVE] = null
              ei.outp[BELOW] = null
              ei.next = null
              ei.prev = null
              ei.succ =
                if (num_edges > 1 && i < num_edges - 1) edge_table.getNode(
                  e_index + i + 1
                ) else null
              ei.pred = if (num_edges > 1 && i > 0) edge_table.getNode(e_index + i - 1) else null
              ei.next_bound = null
              ei.bside[CLIP] = if (op === OperationType.GPC_DIFF) RIGHT else LEFT
              ei.bside[SUBJ] = LEFT
            }
            insert_bound(bound_list(lmt_table, edge_table.getNode(min).vertex.y), e)
            if (DEBUG) {
              println("rev")
              lmt_table.print()
            }
            e_index += num_edges
          }
        }
      }
    }
    return edge_table
  }

  private fun add_st_edge(st: StNode?, it: ItNodeTable, edge: EdgeNode, dy: Float): StNode {
    var st = st
    if (st == null) {
      /* Append edge onto the tail end of the ST */
      st = StNode(edge, null)
    } else {
      val den = st.xt - st.xb - (edge.xt - edge.xb)

      /* If new edge and ST edge don't cross */
      if (edge.xt >= st.xt || edge.dx == st.dx || Math.abs(den) <= GPC_EPSILON) {
        /* No intersection - insert edge here (before the ST edge) */
        val existing_node: StNode = st
        st = StNode(edge, existing_node)
      } else {
        /* Compute intersection between new edge and ST edge */
        val r = (edge.xb - st.xb) / den
        val x = st.xb + r * (st.xt - st.xb)
        val y = r * dy

        /* Insert the edge pointers and the intersection point in the IT */
        it.top_node = add_intersection(it.top_node, st.edge, edge, x, y)

        /* Head further into the ST */
        st.prev = add_st_edge(st.prev, it, edge, dy)
      }
    }
    return st
  }

  private fun add_intersection(
    it_node: ItNode?,
    edge0: EdgeNode,
    edge1: EdgeNode,
    x: Float,
    y: Float,
  ): ItNode {
    var it_node = it_node
    if (it_node == null) {
      /* Append a new node to the tail of the list */
      it_node = ItNode(edge0, edge1, x, y, null)
    } else {
      if (it_node.point.y > y) {
        /* Insert a new node mid-list */
        val existing_node: ItNode = it_node
        it_node = ItNode(edge0, edge1, x, y, existing_node)
      } else {
        /* Head further down the list */
        it_node.next = add_intersection(it_node.next, edge0, edge1, x, y)
      }
    }
    return it_node
  }

  private fun count_tristrips(tn: PolygonNode?): Int {
    var tn = tn
    var total: Int
    total = 0
    while (tn != null) {
      if (tn.active > 2) {
        total++
      }
      tn = tn.next
    }
    return total
  }

  private fun add_vertex(ve_node: VertexNode?, x: Float, y: Float): VertexNode {
    var ve_node = ve_node
    if (ve_node == null) {
      /* Append a new node to the tail of the list */
      ve_node = VertexNode(x, y)
    } else {
      /* Head further down the list */
      ve_node.next = add_vertex(ve_node.next, x, y)
    }
    return ve_node
  }

  private fun new_tristrip(
    po_node: PolygonNode?, edge: EdgeNode?, x: Float, y: Float,
  ): PolygonNode {
    var po_node = po_node
    if (po_node == null) {
      /* Append a new node to the tail of the list */
      po_node = PolygonNode()
      po_node.v[LEFT] = add_vertex(po_node.v[LEFT], x, y)
      edge!!.outp[ABOVE] = po_node
    } else {
      /* Head further down the list */
      po_node.next = new_tristrip(po_node.next, edge, x, y)
    }
    return po_node
  }

  // -------------
  // --- DEBUG ---
  // -------------
  private fun print_sbt(sbt: FloatArray) {
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
    override fun toString(): String {
      return m_Type
    }

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
    const val NUL = 0 /* Empty non-intersection            */
    const val EMX = 1 /* External maximum                  */
    const val ELI = 2 /* External left intermediate        */
    const val TED = 3 /* Top edge                          */
    const val ERI = 4 /* External right intermediate       */
    const val RED = 5 /* Right edge                        */
    const val IMM = 6 /* Internal maximum and minimum      */
    const val IMN = 7 /* Internal minimum                  */
    const val EMN = 8 /* External minimum                  */
    const val EMM = 9 /* External maximum and minimum      */
    const val LED = 10 /* Left edge                         */
    const val ILI = 11 /* Internal left intermediate        */
    const val BED = 12 /* Bottom edge                       */
    const val IRI = 13 /* Internal right intermediate       */
    const val IMX = 14 /* Internal maximum                  */
    const val FUL = 15 /* Full non-intersection             */
    fun getType(tr: Int, tl: Int, br: Int, bl: Int): Int {
      return tr + (tl shl 1) + (br shl 2) + (bl shl 3)
    }
  }

  /**
   * Horizontal edge states
   */
  private object HState {
    const val NH = 0 /* No horizontal edge                */
    const val BH = 1 /* Bottom horizontal edge            */
    const val TH = 2 /* Top horizontal edge               */

    /* Horizontal edge state transitions within scanbeam boundary */
    val next_h_state =
      arrayOf(
        intArrayOf(BH, TH, TH, BH, NH, NH), intArrayOf(NH, NH, NH, NH, TH, TH),
        intArrayOf(NH, NH, NH, NH, BH, BH)
      )
  }

  /**
   * Edge bundle state
   */
  private class BundleState private constructor(private val m_State: String) {
    override fun toString(): String {
      return m_State
    }

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
    var x: Float,   // Y coordinate component
    var y: Float,
  ) {
    var next // Pointer to next vertex in list
      : VertexNode? = null
  }

  /**
   * Internal contour / tristrip type
   */
  private class PolygonNode {
    var active /* Active flag / vertex count        */: Int
    var hole /* Hole / external contour flag      */ = false
    var v = arrayOfNulls<VertexNode>(2) /* Left and right vertex list ptrs   */
    var next /* Pointer to next polygon contour   */: PolygonNode?
    var proxy /* Pointer to actual structure used  */: PolygonNode

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

    fun add_right(x: Float, y: Float) {
      val nv = VertexNode(x, y)

      /* Add vertex nv to the right end of the polygon's vertex list */
      proxy.v[RIGHT]!!.next = nv

      /* Update proxy->v[RIGHT] to point to nv */
      proxy.v[RIGHT] = nv
    }

    fun add_left(x: Float, y: Float) {
      val nv = VertexNode(x, y)

      /* Add vertex nv to the left end of the polygon's vertex list */
      nv.next = proxy.v[LEFT]

      /* Update proxy->[LEFT] to point to nv */
      proxy.v[LEFT] = nv
    }
  }

  private class TopPolygonNode {
    var top_node: PolygonNode? = null
    fun add_local_min(x: Float, y: Float): PolygonNode {
      val existing_min = top_node
      top_node = PolygonNode(existing_min, x, y)
      return top_node!!
    }

    fun merge_left(p: PolygonNode, q: PolygonNode) {
      /* Label contour as a hole */
      q.proxy.hole = true
      if (p.proxy !== q.proxy) {
        /* Assign p's vertex list to the left end of q's list */
        p.proxy.v[RIGHT]!!.next = q.proxy.v[LEFT]
        q.proxy.v[LEFT] = p.proxy.v[LEFT]

        /* Redirect any p.proxy references to q.proxy */
        val target = p.proxy
        var node = top_node
        while (node != null) {
          if (node.proxy === target) {
            node.active = 0
            node.proxy = q.proxy
          }
          node = node.next
        }
      }
    }

    fun merge_right(p: PolygonNode?, q: PolygonNode?) {
      /* Label contour as external */
      q!!.proxy.hole = false
      if (p!!.proxy !== q.proxy) {
        /* Assign p's vertex list to the right end of q's list */
        q.proxy.v[RIGHT]!!.next = p!!.proxy.v[LEFT]
        q.proxy.v[RIGHT] = p.proxy.v[RIGHT]

        /* Redirect any p->proxy references to q->proxy */
        val target = p.proxy
        var node = top_node
        while (node != null) {
          if (node.proxy === target) {
            node.active = 0
            node.proxy = q.proxy
          }
          node = node.next
        }
      }
    }

    fun count_contours(): Int {
      var nc = 0
      var polygon = top_node
      while (polygon != null) {
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
        polygon = polygon.next
      }
      return nc
    }

    fun getResult(polyClass: Class<*>?): RPolygon {
      //RPolygon result = createNewPoly( polyClass );
      var result = RPolygon()
      val num_contours = count_contours()
      if (num_contours > 0) {
        var c = 0
        var npoly_node: PolygonNode?
        var poly_node = top_node
        while (poly_node != null) {
          npoly_node = poly_node.next
          if (poly_node.active != 0) {
            var contour: RContour
            contour = (if (result.contours.size > 0) result.contours[0] else RContour())
            //RPolygon poly = result ;
            if (num_contours > 0) {
              contour = RContour()
              //poly = createNewPoly( polyClass );
            }
            if (poly_node.proxy.hole) {
              contour.isHole = poly_node.proxy.hole
              //poly.setIsHole( poly_node.proxy.hole );
            }

            // ------------------------------------------------------------------------
            // --- This algorithm puts the verticies into the poly in reverse order ---
            // ------------------------------------------------------------------------
            var vtx = poly_node.proxy.v[LEFT]
            while (vtx != null) {
              contour.addPoint(vtx.x, vtx.y)
              vtx = vtx.next
            }
            if (num_contours > 0) {
              result.addContour(contour)
              //result.add( poly );
            }
            c++
          }
          poly_node = npoly_node
        }

        // -----------------------------------------
        // --- Sort holes to the end of the list ---
        // -----------------------------------------
        val orig = RPolygon(result)
        result = RPolygon()
        //result = createNewPoly( polyClass );
        for (i in 0 until orig.contours.size)  //for( int i = 0 ; i < orig.getNumInnerPoly() ; i++ )
        {
          val inner = orig.contours[i]
          //RPolygon inner = orig.getInnerPoly(i);
          if (!inner.isHole) {
            result.addContour(inner)
            //result.add(inner);
          }
        }
        for (i in 0 until orig.contours.size)  //for( int i = 0 ; i < orig.getNumInnerPoly() ; i++ )
        {
          val inner = orig.contours[i]
          //RPolygon inner = orig.getInnerPoly(i);
          if (inner.isHole) {
            result.addContour(inner)
          }
        }
      }
      return result
    }

    fun print() {
      println("---- out_poly ----")
      var c = 0
      var npoly_node: PolygonNode? = null
      var poly_node = top_node
      while (poly_node != null) {
        println("contour=" + c + "  active=" + poly_node.active + "  hole=" + poly_node.proxy.hole)
        npoly_node = poly_node.next
        if (poly_node.active != 0) {
          val v = 0
          var vtx = poly_node.proxy.v[LEFT]
          while (vtx != null) {
            println("v=" + v + "  vtx.x=" + vtx.x + "  vtx.y=" + vtx.y)
            vtx = vtx.next
          }
          c++
        }
        poly_node = npoly_node
      }
    }
  }

  private class EdgeNode(var vertex: RPoint) {
    var bot = RPoint(0, 0) /* Edge lower (x, y) coordinate      */
    var top = RPoint(0, 0) /* Edge upper (x, y) coordinate      */
    var xb /* Scanbeam bottom x coordinate      */ = 0f
    var xt /* Scanbeam top x coordinate         */ = 0f
    var dx /* Change in x for a unit y increase */ = 0f
    var type /* Clip / subject edge flag          */ = 0
    var bundle = Array(2) { IntArray(2) } /* Bundle edge flags                 */
    var bside = IntArray(2) /* Bundle left / right indicators    */
    var bstate = arrayOfNulls<BundleState>(2) /* Edge bundle state                 */
    var outp = arrayOfNulls<PolygonNode>(2) /* Output polygon / tristrip pointer */
    var prev /* Previous edge in the AET          */: EdgeNode? = null
    var next /* Next edge in the AET              */: EdgeNode? = null
    var pred /* Edge connected at the lower end   */: EdgeNode? = null
    var succ /* Edge connected at the upper end   */: EdgeNode? = null
    var next_bound /* Pointer to next bound in LMT      */: EdgeNode? = null
  }

  private class AetTree {
    var top_node: EdgeNode? = null
    fun print() {
      println()
      println("aet")
      var edge = top_node
      while (edge != null) {
        println("edge.vertex.x=" + edge.vertex.x + "  edge.vertex.y=" + edge.vertex.y)
        edge = edge.next
      }
    }
  }

  private class EdgeTable {
    private val m_List: MutableList<EdgeNode> = mutableListOf()
    fun addNode(x: Float, y: Float) {
      val node = EdgeNode(RPoint(x, y))
      m_List.add(node)
    }

    fun getNode(index: Int): EdgeNode {
      return m_List[index]
    }

    fun FWD_MIN(i: Int): Boolean {
      val prev = m_List[PREV_INDEX(i, m_List.size)]
      val next = m_List[NEXT_INDEX(i, m_List.size)]
      val ith = m_List[i]
      return prev.vertex.y >= ith.vertex.y && next.vertex.y > ith.vertex.y
    }

    fun NOT_FMAX(i: Int): Boolean {
      val next = m_List[NEXT_INDEX(i, m_List.size)]
      val ith = m_List[i]
      return next.vertex.y > ith.vertex.y
    }

    fun REV_MIN(i: Int): Boolean {
      val prev = m_List[PREV_INDEX(i, m_List.size)]
      val next = m_List[NEXT_INDEX(i, m_List.size)]
      val ith = m_List[i]
      return prev.vertex.y > ith.vertex.y && next.vertex.y >= ith.vertex.y
    }

    fun NOT_RMAX(i: Int): Boolean {
      val prev = m_List[PREV_INDEX(i, m_List.size)]
      val ith = m_List[i]
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
    var first_bound: EdgeNode? = null

    /* Pointer to next local minimum     */
    var next: LmtNode? = null
  }

  private class LmtTable {
    var top_node: LmtNode? = null
    fun print() {
      var n = 0
      var lmt = top_node
      while (lmt != null) {
        println("lmt($n)")
        var edge = lmt.first_bound
        while (edge != null) {
          println("edge.vertex.x=" + edge.vertex.x + "  edge.vertex.y=" + edge.vertex.y)
          edge = edge.next_bound
        }
        n++
        lmt = lmt.next
      }
    }
  }

  /**
   * Scanbeam tree
   */
  private class ScanBeamTree(
    /* Scanbeam node y value             */
    var y: Float,
  ) {
    var less /* Pointer to nodes with lower y     */: ScanBeamTree? = null
    var more /* Pointer to nodes with higher y    */: ScanBeamTree? = null
  }

  /**
   *
   */
  private class ScanBeamTreeEntries {
    var sbt_entries = 0
    var sb_tree: ScanBeamTree? = null
    fun build_sbt(): FloatArray {
      val sbt = FloatArray(sbt_entries)
      var entries = 0
      entries = inner_build_sbt(entries, sbt, sb_tree)
      check(entries == sbt_entries) { "Something went wrong buildign sbt from tree." }
      return sbt
    }

    private fun inner_build_sbt(entries: Int, sbt: FloatArray, sbt_node: ScanBeamTree?): Int {
      var entries = entries
      if (sbt_node!!.less != null) {
        entries = inner_build_sbt(entries, sbt, sbt_node.less)
      }
      sbt[entries] = sbt_node.y
      entries++
      if (sbt_node.more != null) {
        entries = inner_build_sbt(entries, sbt, sbt_node.more)
      }
      return entries
    }
  }

  /**
   * Intersection table
   */
  private class ItNode(edge0: EdgeNode, edge1: EdgeNode, x: Float, y: Float, next: ItNode?) {
    var ie = mutableListOf<EdgeNode>() /* Intersecting edge (bundle) pair */
    var point = RPoint(x, y) /* Point of intersection */
    var next: ItNode? /* The next intersection table node */

    init {
      ie[0] = edge0
      ie[1] = edge1
      this.next = next
    }
  }

  private class ItNodeTable {
    var top_node: ItNode? = null
    fun build_intersection_table(aet: AetTree, dy: Float) {
      var st: StNode? = null

      /* Process each AET edge */
      var edge = aet.top_node
      while (edge != null) {
        if (edge.bstate[ABOVE] === BundleState.BUNDLE_HEAD || edge.bundle[ABOVE][CLIP] != 0 || edge.bundle[ABOVE][SUBJ] != 0) {
          st = add_st_edge(st, this, edge, dy)
        }
        edge = edge.next
      }
    }
  }

  /**
   * Sorted edge table
   */
  private class StNode(
    /* Pointer to AET edge               */
    var edge: EdgeNode, prev: StNode?,
  ) {
    /* Scanbeam bottom x coordinate      */
    var xb: Float

    /* Scanbeam top x coordinate         */
    var xt: Float

    /* Change in x for a unit y increase */
    var dx: Float

    /* Previous edge in sorted list      */
    var prev: StNode?

    init {
      xb = edge.xb
      xt = edge.xt
      dx = edge.dx
      this.prev = prev
    }
  }
}
