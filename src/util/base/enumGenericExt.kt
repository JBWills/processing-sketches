package util.generics

import kotlin.reflect.KMutableProperty0

fun <E : Enum<E>> E.getValues(): List<E> = declaringClass.enumConstants.toList()

fun <E : Enum<E>> KMutableProperty0<E>.getValues(): List<E> = get().getValues()
