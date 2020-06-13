package com.sournary.architecturecomponent.worker

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.work.WorkerParameters

class BlurFilterWorker(context: Context, params: WorkerParameters) :
    BaseFilterImageWorker(context, params) {

    override fun applyFilter(input: Bitmap): Bitmap {
        val renderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val inAllocation = Allocation.createFromBitmap(renderScript, input)
        val outAllocation = Allocation.createTyped(renderScript, inAllocation.type)
        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).apply {
            setRadius(25f)
            setInput(inAllocation)
            forEach(outAllocation)
        }
        outAllocation.copyTo(output)
        renderScript.finish()
        return output
    }

}
