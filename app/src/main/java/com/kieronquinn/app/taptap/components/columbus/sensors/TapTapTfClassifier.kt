package com.kieronquinn.app.taptap.components.columbus.sensors

import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import com.google.android.columbus.sensors.TfClassifier
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.utils.extensions.runOnClose
import org.koin.core.scope.Scope
import org.tensorflow.lite.Delegate
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.channels.FileChannel

class TapTapTfClassifier(
    assetManager: AssetManager,
    private val tapModel: TapModel,
    scope: Scope,
    settings: TapTapSettings
) : TfClassifier() {

    companion object {
        private const val TAG = "Columbus"
    }

    private val delegate by lazy {
        getSupportedDelegate(settings)
    }

    private val options by lazy {
        Interpreter.Options().apply {
            delegate?.let {
                addDelegate(it)
            }
        }
    }

    private val interpreter by lazy {
        try {
            assetManager.openFd(tapModel.path).let {
                Triple(
                    FileInputStream(it.fileDescriptor).channel,
                    it.startOffset,
                    it.declaredLength
                )
            }.run {
                Interpreter(first.map(FileChannel.MapMode.READ_ONLY, second, third), options)
            }.apply {
                Log.d(TAG, "tflite file loaded: ${tapModel.path}")
            }
        } catch (e: Exception) {
            Log.d(TAG, "load tflite file error: ${tapModel.path}")
            Log.d(TAG, "tflite file: $e")
            null
        }
    }

    override fun predict(input: ArrayList<Float>, size: Int): ArrayList<ArrayList<Float>> {
        val interpreter = interpreter ?: return ArrayList()
        return when (tapModel.modelType) {
            TapModel.ModelType.NEW -> predict12(interpreter, input, size)
            TapModel.ModelType.LEGACY -> predict11(interpreter, input, size)
        }
    }

    private fun getSupportedDelegate(settings: TapTapSettings): Delegate? {
        return when {
            //Low power disabled
            !settings.advancedTensorLowPower.getSync() -> null
            //Use NNAPI if it's supported
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                getNNAPIDelegate()
            }
            else -> null
        }
    }

    private fun getNNAPIDelegate(): NnApiDelegate {
        val options = NnApiDelegate.Options().apply {
            setExecutionPreference(NnApiDelegate.Options.EXECUTION_PREFERENCE_LOW_POWER)
            //Required as models aren't able to be fully processed by an accelerator
            setUseNnapiCpu(true)
        }
        return NnApiDelegate(options)
    }

    init {
        scope.runOnClose {
            (delegate as? NnApiDelegate)?.close()
            interpreter?.close()
        }
    }

}