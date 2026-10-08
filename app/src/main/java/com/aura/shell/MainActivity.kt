package com.aura.shell

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    data class AppInfo(val label: String, val icon: Drawable, val packageName: String)

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var clock: TextView
    private lateinit var dateView: TextView
    private val handler = Handler()
    private var use24h = true
    private var styleIndex = 0
    private var sizeIndex = 2
    private var colorIndex = 0

    private val styles = listOf(
        Triple("Ultra Thin", "sans-serif-thin", Typeface.NORMAL),
        Triple("Thin", "sans-serif-light", Typeface.NORMAL),
        Triple("Regular", "sans-serif", Typeface.NORMAL),
        Triple("Bold", "sans-serif", Typeface.BOLD)
    )
    private val sizes = listOf(60f, 76f, 88f, 110f)
    private val sizeNames = listOf("S", "M", "L", "XL")

    private val colorNames = listOf(
        "Белый", "Голубой", "Розовый", "Фиолетовый",
        "Зелёный", "Жёлтый", "Оранжевый", "Красный",
        "Синий", "Бирюзовый", "Лаймовый", "Серый"
    )
    private val colorValues = listOf(
        0xFFFFFFFF.toInt(), 0xFF5AC8FA.toInt(), 0xFFFF2D95.toInt(), 0xFFAF52DE.toInt(),
        0xFF34C759.toInt(), 0xFFFFCC00.toInt(), 0xFFFF9500.toInt(), 0xFFFF3B30.toInt(),
        0xFF0A84FF.toInt(), 0xFF00C7BE.toInt(), 0xFFA3E635.toInt(), 0xFF8E8E93.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("aura", Context.MODE_PRIVATE)
        use24h = prefs.getBoolean("use24h", true)
        styleIndex = prefs.getInt("style", 0)
        sizeIndex = prefs.getInt("size", 2)
        colorIndex = prefs.getInt("color", 0)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF1A1A2E.toInt(), 0xFF0A0A0F.toInt(), 0xFF000000.toInt())
            )
        }

        clock = TextView(this).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.02f
            setPadding(0, 120, 0, 0)
            setOnLongClickListener {
                showClockSettings()
                true
            }
        }
        root.addView(clock)

        dateView = TextView(this).apply {
            textSize = 18f
            setTextColor(0xCCFFFFFF.toInt())
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 80)
        }
        root.addView(dateView)

        applyClockStyle()

        val locales = Locale("ru", "RU")
        val dayFormat = SimpleDateFormat("EEEE", locales)
        val dateFormat = SimpleDateFormat("d MMMM", locales)

        val runnable = object : Runnable {
            override fun run() {
                val now = Date()
                val fmt = SimpleDateFormat(if (use24h) "HH:mm" else "h:mm", locales)
                clock.text = fmt.format(now)
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

        // Свайп влево → меню кастомизации
        val gesture = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (kotlin.math.abs(dx) > kotlin.math.abs(dy) && kotlin.math.abs(dx) > 150) {
                    if (dx < 0) {
                        showClockSettings()
                        return true
                    }
                }
                return false
            }
        })

        root.setOnTouchListener { _, event ->
            gesture.onTouchEvent(event)
            false
        }

        setContentView(root)
    }

    private fun applyClockStyle() {
        val (_, family, style) = styles[styleIndex]
        clock.textSize = sizes[sizeIndex]
        clock.typeface = Typeface.create(family, style)
        clock.setTextColor(colorValues[colorIndex])
    }

    private fun showClockSettings() {
        val options = arrayOf(
            "🎨 Цвет часов: ${colorNames[colorIndex]}",
            "✏️ Стиль: ${styles[styleIndex].first}",
            "📏 Размер: ${sizeNames[sizeIndex]}",
            "🕐 Формат: ${if (use24h) "24 часа" else "12 часов"}",
            "🔄 Сбросить всё"
        )
        AlertDialog.Builder(this)
            .setTitle("Кастомизация")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showColorPicker()
                    1 -> showStylePicker()
                    2 -> showSizePicker()
                    3 -> {
                        use24h = !use24h
                        prefs.edit().putBoolean("use24h", use24h).apply()
                        applyClockStyle()
                    }
                    4 -> {
                        styleIndex = 0; sizeIndex = 2; colorIndex = 0; use24h = true
                        prefs.edit().clear().apply()
                        applyClockStyle()
                    }
                }
            }
            .show()
    }

    private fun showColorPicker() {
        AlertDialog.Builder(this)
            .setTitle("Цвет часов")
            .setSingleChoiceItems(colorNames.toTypedArray(), colorIndex) { dialog, which ->
                colorIndex = which
                prefs.edit().putInt("color", colorIndex).apply()
                applyClockStyle()
                dialog.dismiss()
            }
            .show()
    }

    private fun showStylePicker() {
        val names = styles.map { it.first }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Стиль часов")
            .setSingleChoiceItems(names, styleIndex) { dialog, which ->
                styleIndex = which
                prefs.edit().putInt("style", styleIndex).apply()
                applyClockStyle()
                dialog.dismiss()
            }
            .show()
    }

    private fun showSizePicker() {
        AlertDialog.Builder(this)
            .setTitle("Размер часов")
            .setSingleChoiceItems(sizeNames.toTypedArray(), sizeIndex) { dialog, which ->
                sizeIndex = which
                prefs.edit().putInt("size", sizeIndex).apply()
                applyClockStyle()
                dialog.dismiss()
            }
            .show()
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
                gravity = Gravity.CENTER
                maxLines = 1
                setPadding(0, 10, 0, 0)
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            layout.addView(label)
            layout.setOnClickListener {
                packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
            }
            return layout
        }
    }
}
