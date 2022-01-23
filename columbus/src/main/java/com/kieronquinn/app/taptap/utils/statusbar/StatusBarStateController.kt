package com.kieronquinn.app.taptap.utils.statusbar

/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Sends updates to [StateListener]s about changes to the status bar state and dozing state
 */
interface StatusBarStateController {

    /**
     * Is device dozing. Dozing is when the screen is in AOD or asleep given that
     * [com.android.systemui.doze.DozeService] is configured.
     */
    val isDozing: Boolean

    /**
     * Adds a state listener
     */
    fun addCallback(listener: StateListener?)

    /**
     * Listener for StatusBarState updates
     */
    interface StateListener {

        /**
         * Callback to be notified when Dozing changes. Dozing is stored separately from state.
         */
        fun onDozingChanged(isDozing: Boolean) {}

    }
}