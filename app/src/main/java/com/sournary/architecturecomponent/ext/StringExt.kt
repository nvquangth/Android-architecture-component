package com.sournary.architecturecomponent.ext

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.IOException
import java.net.URL

@Throws(IOException::class)
fun String.getFilterInputBitmap(resolver: ContentResolver, isRemote: Boolean): Bitmap =
    if (isRemote) {
        URL(this).openStream().use { BitmapFactory.decodeStream(it) }
    } else {
        val uri = Uri.parse(this)
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
    }
