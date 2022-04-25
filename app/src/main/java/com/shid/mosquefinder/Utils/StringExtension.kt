package com.shid.mosquefinder.Utils

val String.hour get() : Int = if (this != "-") this.split(":", " ").first().toInt() else 0
val String.minutes get() : Int = if (this != "-") this.split(":", " ")[1].toInt() else 0