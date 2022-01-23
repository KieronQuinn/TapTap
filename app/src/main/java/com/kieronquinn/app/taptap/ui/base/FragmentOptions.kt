package com.kieronquinn.app.taptap.ui.base

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

interface BackAvailable
interface LockCollapsed
interface CanShowSnackbar

interface ProvidesBack {
    fun onBackPressed(): Boolean
}

interface ProvidesTitle {
    fun getTitle(): CharSequence?
}

interface ProvidesOverflow {
    fun inflateMenu(menuInflater: MenuInflater, menu: Menu)
    fun onMenuItemSelected(menuItem: MenuItem): Boolean
}