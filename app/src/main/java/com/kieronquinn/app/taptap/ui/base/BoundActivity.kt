package com.kieronquinn.app.taptap.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import com.kieronquinn.monetcompat.app.MonetCompatActivity

abstract class BoundActivity<T: ViewBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): MonetCompatActivity() {

    private var _binding: T? = null
    protected val binding: T
        get() = _binding ?: throw RuntimeException("Unable to access binding before onCreate or after onDestroy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.whenCreated {
            monet.awaitMonetReady()
            _binding = inflate(layoutInflater, null, false).apply {
                setContentView(root)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}