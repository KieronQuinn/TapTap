package com.kieronquinn.app.taptap.components.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.utils.autoCleared
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.ui.screens.container.ContainerViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class BoundFragment<T: ViewBinding>(private val viewBindingClass: Class<T>) : BaseFragment() {

    internal var binding by autoCleared<T>()

    open val disableToolbarBackground = false

    private val containerViewModel by sharedViewModel<ContainerViewModel>()

    private val scrollListener by lazy {
        object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                containerViewModel.updateScrollOffset(recyclerView.computeVerticalScrollOffset())
            }
        }
    }

    private val scrollChangeListener by lazy {
        NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ -> containerViewModel.updateScrollOffset(scrollY) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = viewBindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java).invoke(null, inflater, container, false) as T
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        containerViewModel.shouldDisableToolbarBackground.postValue(disableToolbarBackground)
    }

    fun RecyclerView.bindToInsets(bottomPadding: Int? = null){
        containerViewModel.topInsetChange.observe(viewLifecycleOwner){
            updatePadding(top = containerViewModel.getTopInset(context))
            addOnScrollListener(scrollListener)
            smoothScrollToPosition(0)
            containerViewModel.updateScrollOffset(0)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        containerViewModel.navigationBarSize.observe(viewLifecycleOwner){
            //updatePadding messes with MaterialCardView for some reason
            setPadding(0, 0, 0, bottomPadding ?: it)
        }
    }

    fun NestedScrollView.bindToInsets(bottomPadding: Int? = null){
        containerViewModel.topInsetChange.observe(viewLifecycleOwner){
            updatePadding(top = containerViewModel.getTopInset(context))
            setOnScrollChangeListener(scrollChangeListener)
            smoothScrollTo(0, 0)
            containerViewModel.updateScrollOffset(0)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        containerViewModel.navigationBarSize.observe(viewLifecycleOwner){
            //updatePadding messes with MaterialCardView for some reason
            setPadding(0, 0, 0, bottomPadding ?: it)
        }
    }

    fun bindToNothing(){
        containerViewModel.updateScrollOffset(0)
        containerViewModel.toolbarTitle.postValue("")
    }

}