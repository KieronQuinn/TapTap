package com.kieronquinn.app.taptap.ui.screens.picker.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentAppsBinding
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.viewModel

class AppPickerFragment: BoundFragment<FragmentAppsBinding>(FragmentAppsBinding::class.java) {

    companion object {
        const val KEY_APP = "app"
    }

    private val viewModel by viewModel<AppPickerViewModel>()
    private val adapter by lazy {
        AppPickerAdapter(requireContext(), emptyList()){
            setResultAndClose(it)
        }
    }

    private val textChangeListener = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.setSearchTerm(s?.toString() ?: "")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel){
            state.observe(viewLifecycleOwner){
                when(it){
                    is AppPickerViewModel.State.Loading -> {
                        binding.recyclerView.isVisible = false
                        binding.emptyList.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is AppPickerViewModel.State.Empty -> {
                        binding.recyclerView.isVisible = false
                        binding.emptyList.isVisible = true
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    is AppPickerViewModel.State.Loaded -> {
                        binding.recyclerView.isVisible = true
                        binding.emptyList.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = false
                        adapter.apps = it.apps
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            shouldShowClearButton.observe(viewLifecycleOwner){
                binding.searchClear.isVisible = it
            }
            getApps(requireContext())
        }
        with(binding){
            recyclerView.run {
                layoutManager = LinearLayoutManager(context)
                adapter = this@AppPickerFragment.adapter
                applySystemWindowInsetsToPadding(bottom = true)
            }
            searchClear.setOnClickListener {
                searchBox.text.clear()
            }
            menu.setOnClickListener {
                it.showOverflowMenu()
            }
            appsToolbar.applySystemWindowInsetsToPadding(top = true)
            home.setOnClickListener {
                activity?.finish()
            }
            swipeRefreshLayout.isEnabled = false
        }
    }

    private fun View.showOverflowMenu(){
        PopupMenu(context, this).run {
            menuInflater.inflate(R.menu.menu_apps, menu)
            setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menu_show_system -> {
                        viewModel.toggleShowAllApps()
                        menu.close()
                    }
                }
                true
            }
            menu.findItem(R.id.menu_show_system).isChecked = viewModel.showAllApps.value
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchBox.addTextChangedListener(textChangeListener)
    }

    override fun onPause() {
        super.onPause()
        binding.searchBox.removeTextChangedListener(textChangeListener)
    }

    private fun setResultAndClose(app: AppPickerViewModel.App) = with(requireActivity()) {
        setResult(Activity.RESULT_OK, Intent().putExtra(KEY_APP, app.packageName))
        finish()
    }

}