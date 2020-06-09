package com.sournary.architecturecomponent.worker

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.work.WorkerParameters
import com.example.background.ScriptC_waterColorEffect

class WaterColorFilterWorker(context: Context, params: WorkerParameters) :
    BaseFilterImageWorker(context, params) {

    override fun applyFilter(input: Bitmap): Bitmap {
        val renderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val inAllocation = Allocation.createFromBitmap(renderScript, input)
        val outAllocation = Allocation.createTyped(renderScript, inAllocation.type)
        ScriptC_waterColorEffect(renderScript).apply {
            _script = this
            _in = inAllocation
            _out = outAllocation
            _width = input.width.toLong()
            _height = input.height.toLong()
            invoke_filter()
        }
        outAllocation.copyTo(output)
        renderScript.finish()
        return output
    }

}
