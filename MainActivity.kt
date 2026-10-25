package com.example.scrollstop

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val pad = (24 * resources.displayMetrics.density).toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(pad, pad * 2, pad, pad)
        }

        layout.addView(TextView(this).apply {
            text = "ScrollStop"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })

        layout.addView(TextView(this).apply {
            text = "يوقف السكرول في إنستغرام ويترك الرسائل تعمل بشكل عادي."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, pad / 2, 0, pad)
        })

        status = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, pad)
        }
        layout.addView(status)

        layout.addView(Button(this).apply {
            text = "تفعيل الخدمة (إعدادات إمكانية الوصول)"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        layout.addView(makeSwitch("حجب الريلز", KEY_REELS, prefs))
        layout.addView(makeSwitch("حجب الصفحة الرئيسية (الفيد)", KEY_FEED, prefs))

        setContentView(layout)
    }

    private fun makeSwitch(label: String, key: String, prefs: android.content.SharedPreferences) =
        Switch(this).apply {
            text = label
            textSize = 16f
            isChecked = prefs.getBoolean(key, true)
            setPadding(0, 32, 0, 32)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        }

    override fun onResume() {
        super.onResume()
        status.text = if (isServiceEnabled()) "✅ الخدمة مفعّلة" else "⚠️ الخدمة غير مفعّلة بعد"
    }

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains("$packageName/${ScrollBlockService::class.java.name}") ||
            enabled.contains("$packageName/.ScrollBlockService")
    }

    companion object {
        const val PREFS = "scrollstop_prefs"
        const val KEY_REELS = "block_reels"
        const val KEY_FEED = "block_feed"
    }
}
