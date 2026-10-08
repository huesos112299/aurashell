package com.aura.shell

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "AuraShell OK"
        tv.textSize = 30f
        tv.gravity = Gravity.CENTER
        setContentView(tv)
    }
}
