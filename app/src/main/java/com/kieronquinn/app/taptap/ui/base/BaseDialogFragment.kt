package com.kieronquinn.app.taptap.ui.base

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.animation.addListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.components.blur.BlurProvider
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.monetcompat.core.MonetCompat
import org.koin.android.ext.android.inject

abstract class BaseDialogFragment<T: ViewBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): DialogFragment() {

    internal val monet by lazy {
        MonetCompat.getInstance()
    }

    internal val navigation by inject<ContainerNavigation>()

    internal val binding: T
        get() = _binding ?: throw NullPointerException("Cannot access binding before onCreate or after onDestroy")

    internal var _binding: T? = null

    private val blurProvider by inject<BlurProvider>()

    private var isBlurShowing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            ViewCompat.setOnApplyWindowInsetsListener(it.decorView) { view, insets ->
                val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(left = navigationInsets.left, right = navigationInsets.right)
                insets
            }
        }
        return dialog
    }

    private var showBlurAnimation: ValueAnimator? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            showBlurAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 250L
                addUpdateListener {
                    val progress = it.animatedValue as Float
                    dialog?.window?.decorView?.alpha = progress
                    applyBlur(progress)
                }
                addListener(onEnd = {
                    isBlurShowing = true
                })
                start()
            }
        }
    }

    fun dismissWithAnimation(){
        showBlurAnimation?.cancel()
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 250L
            addUpdateListener {
                val progress = it.animatedValue as Float
                dialog?.window?.decorView?.alpha = progress
                applyBlur(progress)
            }
            addListener(onEnd = {
                dismiss()
            })
        }.start()
    }

    private fun applyBlur(ratio: Float){
        val dialogWindow = dialog?.window ?: return
        val appWindow = activity?.window ?: return
        blurProvider.applyDialogBlur(dialogWindow, appWindow, ratio)
    }

    override fun onResume() {
        super.onResume()
        if(isBlurShowing){
            view?.post {
                applyBlur(1f)
            }
        }
    }

}