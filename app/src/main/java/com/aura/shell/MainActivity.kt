package com.aura.shell

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    data class AppInfo(val label: String, val icon: Drawable, val packageName: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF1A1A2E.toInt(), 0xFF0A0A0F.toInt(), 0xFF000000.toInt())
            )
        }

        val clock = TextView(this).apply {
            textSize = 88f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
            gravity = Gravity.CENTER
            letterSpacing = 0.02f
            setPadding(0, 120, 0, 0)
        }
        root.addView(clock)

        val dateView = TextView(this).apply {
            textSize = 18f
            setTextColor(0xCCFFFFFF.toInt())
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 80)
        }
        root.addView(dateView)

        val handler = Handler()
        val locales = Locale("ru", "RU")
        val timeFormat = SimpleDateFormat("HH:mm", locales)
        val dayFormat = SimpleDateFormat("EEEE", locales)
        val dateFormat = SimpleDateFormat("d MMMM", locales)

        val runnable = object : Runnable {
            override fun run() {
                val now = Date()
                clock.text = timeFormat.format(now)
                val day = dayFormat.format(now).replaceFirstChar { it.uppercase() }
                dateView.text = "$day, ${dateFormat.format(now)}"
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        val apps = loadApps()
        val grid = GridView(this).apply {
            numColumns = 4
            horizontalSpacing = 4
            verticalSpacing = 24
            setPadding(40, 0, 40, 60)
            adapter = AppAdapter(apps)
            isVerticalScrollBarEnabled = false
        }
        root.addView(grid, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        setContentView(root)
    }

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map {
                AppInfo(
                    it.loadLabel(pm).toString(),
                    it.loadIcon(pm),
                    it.activityInfo.packageName
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    inner class AppAdapter(val apps: List<AppInfo>) : BaseAdapter() {
        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val app = apps[position]
            val layout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            val icon = ImageView(this@MainActivity).apply {
                setImageDrawable(app.icon)
                layoutParams = LinearLayout.LayoutParams(140, 140)
            }
            layout.addView(icon)

            val label = TextView(this@MainActivity).apply {
                text = app.label
                textSize = 11f
                setTextColor(0xE6FFFFFF.toInt())
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                gravity = Gravity.CENTER
                maxLines = 1
                setPadding(0, 10, 0, 0)
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            layout.addView(label)

            layout.setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) startActivity(launchIntent)
            }

            return layout
        }
    }
}
