package com.kieronquinn.app.taptap.utils.extensions

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.scrollToBottom() {
    smoothScrollToPosition(((adapter?.itemCount ?: return) - 1).coerceAtLeast(0))
}