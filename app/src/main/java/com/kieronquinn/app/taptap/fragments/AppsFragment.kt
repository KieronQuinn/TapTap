package com.kieronquinn.app.taptap.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.AppsAdapter
import com.kieronquinn.app.taptap.models.App
import com.kieronquinn.app.taptap.utils.isSystemApp
import dev.chrisbanes.insetter.applySystemGestureInsetsToPadding
import kotlinx.coroutines.*
import kotlinx.android.synthetic.main.fragment_apps.*
import java.util.*

class AppsFragment : Fragment() {

    companion object {
        const val KEY_SELECTED_APP = "selected_app"
        const val KEY_SHOW_ALL = "show_all"
    }

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private var shouldShowSystemApps: Boolean = true
    private var searchTerm: String? = null

    private val shouldShowAll by lazy {
        activity?.intent?.getBooleanExtra(KEY_SHOW_ALL, false) ?: false
    }

    private val apps: List<App>? by lazy {
        context?.packageManager?.run {
            getInstalledApplications(0).filter { shouldShowAll || getLaunchIntentForPackage(it.packageName) != null }.map {
                App(it.packageName, it.loadLabel(this), it.isSystemApp)
            }.sortedBy { it.appName.toString().toLowerCase(Locale.getDefault()) }
        } ?: kotlin.run {
            null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        context?.let { context ->
            recyclerView.layoutManager = LinearLayoutManager(context)
            getApps(shouldShowSystemApps, searchTerm) { apps ->
                swipeRefreshLayout?.post {
                    swipeRefreshLayout.isEnabled = false
                    swipeRefreshLayout.isRefreshing = true
                }
                recyclerView?.adapter = AppsAdapter(context, apps, emptyList()) { packageName ->
                    val intent = Intent()
                    intent.putExtra(KEY_SELECTED_APP, packageName)
                    activity?.setResult(Activity.RESULT_OK, intent)
                    activity?.finish()
                }
                recyclerView?.adapter?.notifyDataSetChanged()
                swipeRefreshLayout?.postDelayed({
                    swipeRefreshLayout?.isRefreshing = false
                }, 500)
            }
        }
        searchBox.setOnEditorActionListener { v, actionId, event ->
            val result = if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                swipeRefreshLayout.isRefreshing = true
                val imm: InputMethodManager = swipeRefreshLayout.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                getApps(shouldShowSystemApps, searchBox.text.toString()) {
                    searchTerm = searchBox.text.toString()
                    val adapter = recyclerView.adapter as AppsAdapter
                    adapter.apps = it
                    adapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                    if (it.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        empty_list.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        empty_list.visibility = View.GONE
                    }
                }
                true
            } else false
            result
        }
        searchBox.addTextChangedListener {
            if (it?.isNotEmpty() == true) {
                search_clear.visibility = View.VISIBLE
            } else {
                search_clear.visibility = View.GONE
            }
        }
        search_clear.setOnClickListener {
            searchTerm = null
            searchBox.editableText.clear()
            swipeRefreshLayout.isRefreshing = true
            val imm: InputMethodManager = swipeRefreshLayout.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            getApps(shouldShowSystemApps, searchTerm) {
                searchTerm = searchBox.text.toString()
                val adapter = recyclerView.adapter as AppsAdapter
                adapter.apps = it
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
                if (it.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    empty_list.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    empty_list.visibility = View.GONE
                }
            }
        }
        search_clear.visibility = View.GONE
    }

    private fun getApps(showSystemApps: Boolean, searchString: String?, callback: ((apps: List<App>) -> Unit)? = null) {
        swipeRefreshLayout.isRefreshing = true
        uiScope.launch {
            withContext(Dispatchers.IO) {
                apps?.let { apps ->
                    val appList = apps.filter {
                        (!it.isSystemApp || showSystemApps) && (searchString == null || it.appName.toString().toLowerCase(Locale.getDefault()).contains(searchString.toLowerCase(Locale.getDefault())))
                    }
                    withContext(Dispatchers.Main) {
                        callback?.invoke(appList)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupDots()
        recyclerView.applySystemGestureInsetsToPadding(bottom = true)
    }

    override fun onPause() {
        super.onPause()
        val dots = activity?.findViewById<ImageView>(R.id.menu)
        dots?.visibility = View.GONE
    }

    private fun toggleSystemApps(shouldShow: Boolean, callback: (() -> Unit)) {
        swipeRefreshLayout.isRefreshing = true
        val adapter = recyclerView.adapter as AppsAdapter
        getApps(shouldShow, searchTerm) {
            adapter.apps = it
            adapter.notifyDataSetChanged()
            shouldShowSystemApps = shouldShow
            swipeRefreshLayout.isRefreshing = false
            callback.invoke()
        }
    }

    private fun setupDots() {
        val dots = activity?.findViewById<ImageView>(R.id.menu)
        dots?.let {
            dots.visibility = View.VISIBLE
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.gravity = Gravity.END
            popupMenu.inflate(R.menu.menu_apps)
            val checkBox = popupMenu.menu.findItem(R.id.menu_show_system)
            checkBox.isChecked = shouldShowSystemApps
            checkBox.setOnMenuItemClickListener {
                checkBox.isEnabled = false
                checkBox.isChecked = !checkBox.isChecked
                toggleSystemApps(checkBox.isChecked) {
                    checkBox.isEnabled = true
                }
                true
            }
            dots.setOnClickListener {
                popupMenu.show()
            }
        }
    }

}