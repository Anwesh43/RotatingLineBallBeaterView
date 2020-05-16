package com.anwesh.uiprojects.rlbbview

/**
 * Created by anweshmishra on 17/05/20.
 */

import android.graphics.Color

val nodes : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val delay : Long = 20
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

