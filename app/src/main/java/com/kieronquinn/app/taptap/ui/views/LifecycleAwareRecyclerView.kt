package com.kieronquinn.app.taptap.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 *  A simple [RecyclerView] whose [ViewHolder]s have a very basic [Lifecycle].
 *
 *  The lifecycle is as follows:
 *  - When the ViewHolder is created -> [Lifecycle.Event.ON_START] and [Lifecycle.Event.ON_CREATE]
 *  - When the ViewHolder is bound -> [Lifecycle.Event.ON_RESUME]
 *  - When the ViewHolder is recycled -> [Lifecycle.Event.ON_PAUSE]
 *  - When the RecyclerView is detached -> [Lifecycle.Event.ON_PAUSE], [Lifecycle.Event.ON_DESTROY]
 *  and [Lifecycle.Event.ON_STOP]
 *
 *  **Note: Only supports [LinearLayoutManager], and assumes that the [Adapter] will be un-set when
 *  the fragment is destroyed**
 */
class LifecycleAwareRecyclerView : RecyclerView {

    constructor(context: Context, attributeSet: AttributeSet? = null, defStyleRes: Int):
            super(context, attributeSet, defStyleRes)
    constructor(context: Context, attributeSet: AttributeSet?):
            this(context, attributeSet, 0)
    constructor(context: Context):
            this(context, null, 0)

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), LifecycleOwner {

        private val lifecycleRegistry by lazy { LifecycleRegistry(this@ViewHolder) }

        init {
            handleLifecycleEvent(Lifecycle.Event.ON_START)
            handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        final override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        internal fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

    }

    abstract class Adapter<VH: ViewHolder>(private val recyclerView: RecyclerView): RecyclerView.Adapter<VH>() {

        private val layoutManager
            get() = recyclerView.layoutManager as LinearLayoutManager

        override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
            super.onBindViewHolder(holder, position, payloads)
            holder.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        override fun onViewRecycled(holder: VH) {
            super.onViewRecycled(holder)
            holder.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            getCreatedViewHolders().forEach {
                it.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                it.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                it.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        }

        private fun getCreatedViewHolders(): List<ViewHolder> {
            val firstItem = layoutManager.findFirstVisibleItemPosition()
            val lastItem = layoutManager.findLastVisibleItemPosition()
            val viewHolders = ArrayList<ViewHolder>()
            for(i in firstItem until lastItem){
                if(!recyclerView.isAttachedToWindow) continue
                viewHolders.add(recyclerView.getChildViewHolder(recyclerView.getChildAt(i)) as ViewHolder)
            }
            return viewHolders
        }

    }

}