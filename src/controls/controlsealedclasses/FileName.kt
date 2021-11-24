package controls.controlsealedclasses

import BaseSketch
import controlP5.Button
import controlP5.ControlP5
import controls.utils.selectFile

class FileName(
  fieldName: String,
  defaultPath: String?,
  onChange: BaseSketch.(String?) -> Unit,
) : Control<Button>(
  fieldName,
  ControlP5::addButton,
  { sketch, _ ->
    fun updateLabel(path: String?) {
      val noFileSelected = path == null || path.isEmpty()
      label = fieldName
      setCaptionLabel(if (noFileSelected) "Select File" else path)
      isLabelVisible = true
    }

    updateLabel(defaultPath)
    onClick {
      sketch.selectFile { file ->
        val path = file?.path
        updateLabel(path)
        sketch.onChange(path)
      }
    }
  },
)
