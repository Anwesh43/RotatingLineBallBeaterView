package com.anwesh.uiprojects.rlbbview

/**
 * Created by anweshmishra on 17/05/20.
 */

import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View

val nodes : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val delay : Long = 20
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val parts : Int = 2
val rFactor : Float = 2.9f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawRotatingLineBall(scale : Float, size : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val r : Float = size / rFactor
    save()
    rotate(-90f * sf1)
    drawLine(0f, 0f, 0f, 2 * size, paint)
    restore()
    drawCircle(2 * size, -r  - (h / 2 - 2 * r) * sf2, r, paint)
}

fun Canvas.drawRLBBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), h / 2)
    drawRotatingLineBall(scale, size, h, paint)
    restore()
}

class RotatingLineBallBeaterView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RLBBNode(var i : Int, val state : State = State()) {

        private var next : RLBBNode? = null
        private var prev : RLBBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RLBBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRLBBNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RLBBNode {
            var curr : RLBBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RotatingLineBall(var i : Int) {

        private val root : RLBBNode = RLBBNode(0)
        private var curr : RLBBNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                    cb(it)
                }
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RotatingLineBallBeaterView) {

        private val animator : Animator = Animator(view)
        private val rlbb : RotatingLineBall = RotatingLineBall(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            rlbb.draw(canvas, paint)
            animator.animate {
                rlbb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rlbb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : RotatingLineBallBeaterView {
            val view : RotatingLineBallBeaterView = RotatingLineBallBeaterView(activity)
            activity.setContentView(view)
            return view
        }
    }
}