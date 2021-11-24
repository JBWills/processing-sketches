package controls.controlsealedclasses

import controlP5.ControlP5
import controlP5.Textfield
import controls.panels.LabelAlign
import controls.panels.LabelAlign.Companion.alignCaptionAndLabel
import controls.panels.LabelAlignHorizontal.Left
import controls.panels.LabelAlignVertical.Center
import controls.panels.LabelAlignVertical.Top

class TextInput(
  fieldName: String,
  defaultValue: String = "",
) : Control<Textfield>(
  fieldName,
  ControlP5::addTextfield,
  { _, _ ->
    setValue(defaultValue)

    alignCaptionAndLabel(
      valueAlign = LabelAlign(Left, Center),
      captionAlign = LabelAlign(Left, Top),
    )
  },
)
