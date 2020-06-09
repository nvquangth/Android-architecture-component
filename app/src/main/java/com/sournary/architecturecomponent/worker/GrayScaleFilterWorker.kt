package com.sournary.architecturecomponent.worker

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.work.WorkerParameters
import com.example.background.ScriptC_grayscale

class GrayScaleFilterWorker(context: Context, params: WorkerParameters) :
    BaseFilterImageWorker(context, params) {

    override fun applyFilter(input: Bitmap): Bitmap {
        var renderScript: RenderScript? = null
        try {
            renderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
            val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
            val inAllocation = Allocation.createFromBitmap(renderScript, input)
            val outAllocation = Allocation.createTyped(renderScript, inAllocation.type)
            ScriptC_grayscale(renderScript).apply {
                _script = this
                _in = inAllocation
                _out = outAllocation
                _width = input.width.toLong()
                _height = input.height.toLong()
                invoke_filter()
            }
            outAllocation.copyTo(output)
            return output
        }finally {
            renderScript?.finish()
        }
    }

}
