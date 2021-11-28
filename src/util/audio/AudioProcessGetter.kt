package util.audio


class AudioProcessGetter<T> {
  private val results: MutableList<T> = mutableListOf()

  fun addProcessedResult(result: T) = results.add(result)

  fun getAudioResults(): List<T> {
    if (results.isEmpty()) {
      throw Exception("Cannot get audio results before dispatcher is run.")
    }

    return results
  }
}
