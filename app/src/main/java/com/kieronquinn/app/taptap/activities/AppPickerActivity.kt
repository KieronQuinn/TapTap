package com.kieronquinn.app.taptap.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.taptap.R
import kotlinx.android.synthetic.main.activity_app_picker.*

class AppPickerActivity : AppCompatActivity() {

    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)
        if (isLightTheme) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).or(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = ""
        }
        toolbar_title.text = getString(R.string.select_an_app)
        home.visibility = View.VISIBLE
        home.setOnClickListener {
            finish()
        }
    }

}