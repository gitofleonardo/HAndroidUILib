package cn.huangchengxi.uilib.utils

import android.content.Context

/**
 * Convert dp value to px value.
 */
fun dp2px(context: Context, dpValue:Float):Float{
    val scale = context.resources.displayMetrics.density
    return dpValue * scale + 0.5f
}