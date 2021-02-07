package com.kieronquinn.app.taptap.models

import android.content.Context
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.utils.extensions.getPhysicalScreenSize
import com.kieronquinn.app.taptap.utils.extensions.sharedPreferences

enum class TfModel(val model: String, internal val screenSizeLowerBound: Double, internal val screenSizeUpperBound: Double) {
    //Screen size: 6.3in
    PIXEL4XL("tap7cls_pixel4xl.tflite", 6.24, Double.MAX_VALUE),
    //Screen size: 6.3in
    PIXEL3XL("tap7cls_pixel3xl.tflite", 6.24, Double.MAX_VALUE),
    //Screen size: 6.18in
    PIXEL4("tap7cls_pixel4.tflite", 0.0, 6.24)
}

fun TfModel.getStringValue(context: Context): String {
    val array = context.resources.getStringArray(R.array.device_model_keys)
    return array[ordinal]
}

fun Context.getDefaultTfModel(screenSize: Double = getPhysicalScreenSize()): TfModel {
    /*
        Screen size is not really a very good way to check this. It doesn't take into account bezels - the 3XL and 4XL have the same screen size.
        The ideal solution would be to look up the model in a database and compare dimensions, but the only API I could find that was suitable (https://github.com/shakee93/fonoapi) is currently offline.
        So for now we'll have to use screen size and simply never default to the 3XL, but allowing the user to switch if they want
     */
    for(model in TfModel.values()){
        if(screenSize >= model.screenSizeLowerBound && screenSize < model.screenSizeUpperBound) return model
    }
    //Default to 4XL (Seems to work best for most)
    return TfModel.PIXEL4XL
}

fun Context.getCurrentTapModel(): TfModel {
    val default = getDefaultTfModel().name
    return TfModel.valueOf(sharedPreferences?.getString(SHARED_PREFERENCES_KEY_MODEL, default) ?: default)
}