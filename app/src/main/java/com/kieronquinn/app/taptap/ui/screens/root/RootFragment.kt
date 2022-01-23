package com.kieronquinn.app.taptap.ui.screens.root

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.navigation.setupWithNavigation
import com.kieronquinn.app.taptap.databinding.FragmentRootBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RootFragment: BoundFragment<FragmentRootBinding>(FragmentRootBinding::inflate) {

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.fragment_container_root) as NavHostFragment
    }

    private val navigation by inject<RootNavigation>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        launch {
            navHostFragment.setupWithNavigation(navigation)
        }
    }

}