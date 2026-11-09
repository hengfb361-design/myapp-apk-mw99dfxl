package com.example.myapp
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }

        val btnStartMod = Button(this).apply {
            text = "1. Enable Floating MOD Lines"
            setOnClickListener {
                if (checkOverlayPermission()) {
                    val serviceIntent = Intent(this@MainActivity, PoolModService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    Toast.makeText(this@MainActivity, "Floating Overlay & MOD Button Started!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnOpenGame = Button(this).apply {
            text = "2. Launch 8 Ball Pool Game"
            setOnClickListener {
                open8BallPoolGame()
            }
        }

        layout.addView(btnStartMod)
        layout.addView(btnOpenGame)
        setContentView(layout)
    }

    private fun checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "Please allow 'Display over other apps'", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun open8BallPoolGame() {
        val packageName = "com.miniclip.eightballpool"
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "8 Ball Pool App is not installed!", Toast.LENGTH_LONG).show()
        }
    }
}
