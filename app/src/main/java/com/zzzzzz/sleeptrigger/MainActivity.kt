package com.zzzzzz.sleeptrigger

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this).apply {
                text = getString(R.string.app_status)
                textSize = 18f
                setPadding(32, 32, 32, 32)
            }
        )
    }
}

