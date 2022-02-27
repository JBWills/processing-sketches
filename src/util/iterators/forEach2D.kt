package util.iterators

fun <T> List<List<T>>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: T) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T, K> List<List<T>>.map2D(block: (rowIndex: Int, colIndex: Int, item: T) -> K): List<List<K>> =
  mapIndexed { rowIndex, row ->
    row.mapIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T> Array<List<T>>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: T) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T, K> Array<List<T>>.map2D(block: (rowIndex: Int, colIndex: Int, item: T) -> K): List<List<K>> =
  mapIndexed { rowIndex, row ->
    row.mapIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T> Array<Array<T>>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: T) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T, K> Array<Array<T>>.map2D(block: (rowIndex: Int, colIndex: Int, item: T) -> K): List<List<K>> =
  mapIndexed { rowIndex, row ->
    row.mapIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun Array<DoubleArray>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: Double) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun Array<IntArray>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: Int) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun Array<FloatArray>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: Float) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }
