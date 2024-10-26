package com.kieronquinn.app.taptap.ui.screens.settings.advanced.wallpapercolorpicker

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.databinding.FragmentSettingsWallpaperColorPickerBinding
import com.kieronquinn.app.taptap.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import org.koin.android.ext.android.inject

class SettingsWallpaperColorPickerBottomSheetFragment: BaseBottomSheetFragment<FragmentSettingsWallpaperColorPickerBinding>(FragmentSettingsWallpaperColorPickerBinding::inflate) {

    private val settings by inject<TapTapSettings>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view){ _, insets ->
            val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraPadding = resources.getDimension(R.dimen.margin_16).toInt()
            view.updatePadding(left = navigationInsets.left, right = navigationInsets.right, bottom = navigationInsets.bottom + extraPadding)
            insets
        }
        whenResumed {
            with(binding){
                val availableColors = monet.getAvailableWallpaperColors() ?: emptyList()
                //No available colors = likely using a live wallpaper, show a toast and dismiss
                if(availableColors.isEmpty()){
                    Toast.makeText(requireContext(), getString(R.string.color_picker_unavailable), Toast.LENGTH_LONG).show()
                    dismiss()
                    return@whenResumed
                }
                root.backgroundTintList = ColorStateList.valueOf(monet.getBackgroundColor(requireContext()))
                colorPickerList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                colorPickerList.adapter = SettingsWallpaperColorPickerAdapter(requireContext(), monet.getSelectedWallpaperColor(), availableColors){
                    onColorPicked(it)
                }
                colorPickerOk.setOnClickListener {
                    dialog?.dismiss()
                }
                colorPickerOk.setTextColor(monet.getAccentColor(requireContext()))
            }
        }
    }

    private fun onColorPicked(color: Int) = whenResumed {
        settings.monetColor.set(color)
        //Trigger a manual update
        monet.updateMonetColors()
    }

}