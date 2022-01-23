package com.kieronquinn.app.taptap.components.sui

import com.kieronquinn.app.taptap.BuildConfig
import rikka.sui.Sui

interface SuiProvider {
    val isSui: Boolean
}

class SuiProviderImpl: SuiProvider {

    override val isSui = Sui.init(BuildConfig.APPLICATION_ID)

}