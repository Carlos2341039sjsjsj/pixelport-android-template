package com.pixelport.player

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val portConfig = PortConfigLoader.loadPortConfig(this)
        val controlsConfig = PortConfigLoader.loadControlsConfig(this)

        if (portConfig.orientation == "landscape") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#12151C"))
        }

        val infoLabel = TextView(this).apply {
            text = "${portConfig.appName} | ${portConfig.detectedEngine} | ${controlsConfig.profile}"
            setTextColor(Color.LTGRAY)
            textSize = 13f
            setPadding(24, 18, 24, 18)
        }

        val overlayView = TouchOverlayView(this, controlsConfig)

        rootLayout.addView(infoLabel)
        rootLayout.addView(overlayView)
        setContentView(rootLayout)
    }
}
