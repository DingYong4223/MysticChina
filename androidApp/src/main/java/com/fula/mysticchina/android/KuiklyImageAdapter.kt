package com.fula.mysticchina.android

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.kuikly.core.render.android.KuiklyRenderViewContext
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter

internal class KuiklyImageAdapter(private val context: Context) : IKRImageAdapter {
    override val shouldWaitViewDidLoad = false

    override fun fetchDrawable(option: HRImageLoadOption, callback: (Drawable?) -> Unit) {
        val source = if (option.isAssets()) {
            "file:///android_asset/${option.src.removePrefix(HRImageLoadOption.SCHEME_ASSETS)}"
        } else {
            option.src
        }
        val request = Glide.with(context).asDrawable().load(source)
        if (option.needResize && option.requestWidth > 0 && option.requestHeight > 0) {
            request.override(option.requestWidth, option.requestHeight)
            when (option.scaleType) {
                ImageView.ScaleType.CENTER_CROP -> request.centerCrop()
                ImageView.ScaleType.FIT_CENTER -> request.fitCenter()
                else -> Unit
            }
        }
        request.into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) = callback(resource)
            override fun onLoadFailed(errorDrawable: Drawable?) = callback(null)
            override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
    }

    override fun getDrawableWidth(context: KuiklyRenderViewContext, drawable: Drawable) =
        drawable.intrinsicWidth.toFloat()

    override fun getDrawableHeight(context: KuiklyRenderViewContext, drawable: Drawable) =
        drawable.intrinsicHeight.toFloat()
}
