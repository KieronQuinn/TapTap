package com.kieronquinn.app.taptap.ui.screens.nogyro

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.databinding.FragmentNoGyroscopeBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment

class NoGyroscopeFragment: BoundFragment<FragmentNoGyroscopeBinding>(FragmentNoGyroscopeBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupAvd()
        setupButton()
    }

    private fun setupMonet() {
        binding.root.setBackgroundColor(monet.getBackgroundColor(requireContext()))
        binding.noGyroscopeClose.setTextColor(monet.getAccentColor(requireContext()))
    }

    private fun setupAvd() {
        (binding.noGyroscopeAvd.drawable as AnimatedVectorDrawable).start()
    }

    private fun setupButton() {
        binding.noGyroscopeClose.setOnClickListener {
            requireActivity().finish()
        }
    }

}