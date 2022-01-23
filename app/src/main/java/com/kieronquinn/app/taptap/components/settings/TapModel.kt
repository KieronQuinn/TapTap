package com.kieronquinn.app.taptap.components.settings

import com.kieronquinn.app.taptap.R

enum class TapModel(val modelType: ModelType, val path: String, val nameRes: Int, val descriptionRes: Int, val deviceHeight: Double) {

    //New models, from the Android 12 impl
    REDFIN(ModelType.NEW, "columbus/12/tap7cls_redfin.tflite", R.string.tap_model_redfin_name, R.string.tap_model_redfin_desc, 144.7),
    FLAME(ModelType.NEW, "columbus/12/tap7cls_flame.tflite", R.string.tap_model_flame_name, R.string.tap_model_flame_desc, 147.1),
    BRAMBLE(ModelType.NEW, "columbus/12/tap7cls_bramble.tflite", R.string.tap_model_bramble_name, R.string.tap_model_bramble_desc, 153.9),
    CROSSHATCH(ModelType.NEW, "columbus/12/tap7cls_crosshatch.tflite", R.string.tap_model_crosshatch_name, R.string.tap_model_crosshatch_desc, 158.0),
    CORAL(ModelType.NEW, "columbus/12/tap7cls_coral.tflite", R.string.tap_model_coral_name, R.string.tap_model_coral_desc, 160.4),

    //Old models, from the Android 11 impl
    PIXEL4(ModelType.LEGACY, "columbus/11/tap7cls_pixel4.tflite", R.string.tap_model_pixel4_name, R.string.tap_model_pixel4_desc, 147.1),
    PIXEL3_XL(ModelType.LEGACY, "columbus/11/tap7cls_pixel3xl.tflite", R.string.tap_model_pixel3xl_name, R.string.tap_model_pixel3xl_desc, 158.0),
    PIXEL4_XL(ModelType.LEGACY, "columbus/11/tap7cls_pixel4xl.tflite", R.string.tap_model_pixel4xl_name, R.string.tap_model_pixel4xl_desc, 160.4);

    enum class ModelType {
        NEW, LEGACY
    }
}