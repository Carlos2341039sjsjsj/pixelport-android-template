package com.pixelport.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

class TouchOverlayView(
    context: Context,
    private val controls: ControlsConfig
) : View(context) {

    private val alphaByte = (controls.opacity.coerceIn(0.1, 1.0) * 255).toInt()

    private val stickBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(alphaByte / 2, 40, 44, 52)
        style = Paint.Style.FILL
    }

    private val stickKnobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(alphaByte, 220, 224, 232)
        style = Paint.Style.FILL
    }

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(alphaByte, 52, 120, 246)
        style = Paint.Style.FILL
    }

    private val btnActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(255, 90, 160, 255)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private var stickCenterX = 0f
    private var stickCenterY = 0f
    private var stickRadius = 110f
    private var knobX = 0f
    private var knobY = 0f
    private var stickPointerId = -1

    private val pressedButtons = mutableSetOf<String>()
    private val buttonRects = mutableMapOf<String, RectF>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        stickRadius = min(w, h) * 0.14f
        stickCenterX = w * 0.17f
        stickCenterY = h * 0.72f
        knobX = stickCenterX
        knobY = stickCenterY
        textPaint.textSize = min(w, h) * 0.036f
        rebuildButtonRects(w.toFloat(), h.toFloat())
    }

    private fun rebuildButtonRects(w: Float, h: Float) {
        buttonRects.clear()
        val r = min(w, h) * 0.075f
        val rightBaseX = w * 0.84f
        val centerBaseY = h * 0.72f

        for (btn in controls.buttons) {
            val (cx, cy) = when (btn.pos) {
                "right_top" -> Pair(rightBaseX - r * 0.8f, centerBaseY - r * 2.1f)
                "right_left" -> Pair(rightBaseX - r * 2.3f, centerBaseY - r * 0.5f)
                "right_bottom" -> Pair(rightBaseX - r * 0.4f, centerBaseY + r * 1.4f)
                "top_right" -> Pair(w * 0.92f, h * 0.12f)
                else -> Pair(rightBaseX, centerBaseY - r * 0.3f)
            }
            buttonRects[btn.id] = RectF(cx - r, cy - r, cx + r, cy + r)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Control izquierdo (analog_stick o dpad)
        canvas.drawCircle(stickCenterX, stickCenterY, stickRadius, stickBasePaint)
        canvas.drawCircle(knobX, knobY, stickRadius * 0.42f, stickKnobPaint)

        // Botones derechos / auxiliares
        for (btn in controls.buttons) {
            val rect = buttonRects[btn.id] ?: continue
            val isPressed = pressedButtons.contains(btn.id)
            canvas.drawOval(rect, if (isPressed) btnActivePaint else btnPaint)
            val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(btn.label, rect.centerX(), rect.centerY() - yOffset, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        val idx = event.actionIndex
        val pointerId = event.getPointerId(idx)
        val x = event.getX(idx)
        val y = event.getY(idx)

        when (actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (stickPointerId == -1 && hypot(x - stickCenterX, y - stickCenterY) <= stickRadius * 1.5f) {
                    stickPointerId = pointerId
                    updateStick(x, y)
                } else {
                    checkButtonsDown(x, y)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val px = event.getX(i)
                    val py = event.getY(i)
                    if (pid == stickPointerId) {
                        updateStick(px, py)
                    }
                }
                pressedButtons.clear()
                for (i in 0 until event.pointerCount) {
                    checkButtonsDown(event.getX(i), event.getY(i))
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (pointerId == stickPointerId) {
                    stickPointerId = -1
                    knobX = stickCenterX
                    knobY = stickCenterY
                }
                pressedButtons.clear()
                for (i in 0 until event.pointerCount) {
                    if (event.getPointerId(i) != pointerId) {
                        checkButtonsDown(event.getX(i), event.getY(i))
                    }
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateStick(x: Float, y: Float) {
        val dx = x - stickCenterX
        val dy = y - stickCenterY
        val dist = hypot(dx, dy)
        val maxDist = stickRadius * 0.65f
        if (dist > maxDist && dist > 0f) {
            knobX = stickCenterX + (dx / dist) * maxDist
            knobY = stickCenterY + (dy / dist) * maxDist
        } else {
            knobX = x
            knobY = y
        }
    }

    private fun checkButtonsDown(x: Float, y: Float) {
        for ((id, rect) in buttonRects) {
            if (rect.contains(x, y)) {
                pressedButtons.add(id)
            }
        }
    }
}
