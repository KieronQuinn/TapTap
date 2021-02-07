package com.kieronquinn.app.taptap.providers

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.utils.extensions.boolPrefToUri
import com.kieronquinn.app.taptap.utils.extensions.intPrefToUri
import com.kieronquinn.app.taptap.utils.extensions.stringPrefToUri

//Based off https://code.highspec.ru/Mikanoshi/CustoMIUIzer
class SharedPrefsProvider : ContentProvider(), SharedPreferences.OnSharedPreferenceChangeListener {
    var prefs: SharedPreferences? = null

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.sharedprefs"
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(AUTHORITY, "string/*", 1)
            uriMatcher.addURI(AUTHORITY, "integer/*", 2)
            uriMatcher.addURI(AUTHORITY, "boolean/*", 3)
        }
    }

    override fun onCreate(): Boolean {
        return try {
            prefs = context?.getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE)
            prefs?.registerOnSharedPreferenceChangeListener(this)
            true
        } catch (throwable: Throwable) {
            false
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val parts = uri.pathSegments
        val cursor = MatrixCursor(arrayOf("data"))
        when (uriMatcher.match(uri)) {
            1 -> {
                cursor.newRow().add("data", prefs!!.getString(parts[1], ""))
                return cursor
            }
            2 -> {
                cursor.newRow().add("data", prefs!!.getInt(parts[1], -1))
                return cursor
            }
            3 -> {
                cursor.newRow().add("data", if (prefs!!.getBoolean(parts[1], false)) 1 else 0)
                return cursor
            }
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val uri = when(sharedPreferences.all[key]){
            is String -> stringPrefToUri(
                key
            )
            is Int -> intPrefToUri(
                key
            )
            is Boolean -> boolPrefToUri(
                key
            )
            else -> null
        }
        if(uri != null) notifyChange(uri)
    }

    private fun notifyChange(uri: Uri){
        context?.contentResolver?.notifyChange(uri, null)
    }

}