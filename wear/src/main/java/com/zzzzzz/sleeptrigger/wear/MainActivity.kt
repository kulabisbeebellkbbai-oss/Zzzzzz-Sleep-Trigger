package com.zzzzzz.sleeptrigger.wear

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this).apply {
                text = getString(R.string.wear_status)
                textSize = 14f
                setPadding(16, 16, 16, 16)
            }
        )
    }
}

