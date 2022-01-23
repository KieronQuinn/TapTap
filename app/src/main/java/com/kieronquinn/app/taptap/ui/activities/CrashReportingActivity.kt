package com.kieronquinn.app.taptap.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ActivityCrashReportingBinding
import com.kieronquinn.app.taptap.utils.extensions.themeColor
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *  This activity is intentionally barebones - no fragments, Monet, and minimal library use.
 *  It should be able to handle an incoming crash from the extras, show it and export it.
 */
class CrashReportingActivity : AppCompatActivity() {

    companion object {
        const val KEY_EXCEPTION = "exception"
        private const val CRASH_REPORT_FILE_TEMPLATE = "tap_tap_crash_report_%s.txt"
    }

    private val saveReport = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        if(it == null) return@registerForActivityResult
        saveCrashReport(it)
    }

    private val crashReport by lazy {
        createCrashReport(intent.getStringExtra(KEY_EXCEPTION) ?: run {
            finish()
            ""
        })
    }

    private val binding by lazy {
        ActivityCrashReportingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        setupCrashHeaderText()
        setupCrashReportText()
        setupSaveReportButton()
    }

    private fun setupCrashHeaderText() = with(binding.crashReportingHeader) {
        text = Html.fromHtml(
            getString(R.string.crash_reporting_content_intro),
            Html.FROM_HTML_MODE_LEGACY
        )
        Linkify.addLinks(this, Linkify.ALL)
        movementMethod = BetterLinkMovementMethod.newInstance().setOnLinkClickListener { _, url ->
            openLink(url)
            true
        }
    }

    private fun openLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        })
    }

    private fun setupCrashReportText() {
        binding.crashReportingReport.text = crashReport
    }

    private fun setupSaveReportButton() {
        binding.crashReportingSave.setOnClickListener {
            saveReport.launch(getFilename())
        }
    }

    private fun setupToolbar() {
        window.statusBarColor = themeColor(android.R.attr.colorPrimaryDark)
        binding.crashReportingToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun createCrashReport(exception: String): String {
        val header = getString(R.string.crash_reporting_header)
        val appVersion = getString(
            R.string.crash_reporting_app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
        val device = getString(R.string.crash_reporting_device, Build.MODEL)
        val api = getString(R.string.crash_reporting_api, Build.VERSION.SDK_INT)
        val fingerprint = getString(R.string.crash_reporting_fingerprint, Build.FINGERPRINT)
        val exceptionHeader = getString(R.string.crash_reporting_exception_header)
        return StringBuilder().apply {
            appendLine(header)
            appendLine()
            appendLine(appVersion)
            appendLine(device)
            appendLine(api)
            appendLine(fingerprint)
            appendLine()
            appendLine(exceptionHeader)
            appendLine()
            appendLine(exception)
        }.toString()
    }

    private fun saveCrashReport(uri: Uri) {
        try {
            val file = DocumentFile.fromSingleUri(this, uri) ?: throw Exception()
            val outputStream = contentResolver.openOutputStream(file.uri) ?: throw Exception()
            outputStream.bufferedWriter().use {
                it.write(crashReport)
                it.flush()
            }
            Toast.makeText(this, getString(R.string.crash_reporting_toast_success), Toast.LENGTH_LONG).show()
        }catch (e: Exception){
            Toast.makeText(this, getString(R.string.crash_reporting_toast_failed), Toast.LENGTH_LONG).show()
        }
    }

    private fun getFilename(): String {
        val time = LocalDateTime.now()
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return String.format(CRASH_REPORT_FILE_TEMPLATE, dateTimeFormatter.format(time))
    }

}