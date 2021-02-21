package annotations

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.FIELD

@Target(FIELD)
@Retention(SOURCE)
annotation class BackingField() {
  
}
