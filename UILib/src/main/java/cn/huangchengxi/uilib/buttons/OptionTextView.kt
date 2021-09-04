package cn.huangchengxi.uilib.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import cn.huangchengxi.uilib.utils.dp2px

class OptionTextView(context: Context,attrs:AttributeSet?,defStyle:Int):AppCompatTextView(context, attrs,defStyle) {
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,-1)
    constructor(context: Context):this(context,null)

    init {
        //layoutParams= ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
        gravity=Gravity.CENTER
    }
}