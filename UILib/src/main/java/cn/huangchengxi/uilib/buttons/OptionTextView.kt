package cn.huangchengxi.uilib.buttons

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import cn.huangchengxi.uilib.utils.dp2px

class OptionTextView(context: Context,attrs:AttributeSet?,defStyle:Int):AppCompatTextView(context, attrs,defStyle) {
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,-1)
    constructor(context: Context):this(context,null)

    init {
        gravity=Gravity.CENTER
    }

    class Builder(private val context: Context){
        private var mText=""
        private var mBackgroundColor:Int=Color.RED
        private var mTextColor:Int=Color.WHITE
        private var mPaddingLeft= dp2px(context,5.0f).toInt()
        private var mPaddingRight= dp2px(context,5.0f).toInt()
        private var mPaddingTop=0
        private var mPaddingBottom=0
        private var mListener:OnClickListener?=null

        fun setText(text:String):Builder{
            mText=text
            return this
        }
        fun setBackgroundColor(color: Int):Builder{
            mBackgroundColor=color
            return this
        }
        fun setTextColor(color: Int):Builder{
            mTextColor=color
            return this
        }
        fun setPadding(l:Int,t:Int,r:Int,b:Int):Builder{
            mPaddingLeft=l
            mPaddingRight=r
            mPaddingTop=t
            mPaddingBottom=b
            return this
        }
        fun setOnClickListener(listener: OnClickListener){
            mListener=listener
        }
        fun setOnClickListener(listener:(View)->Unit){
            mListener= OnClickListener { p0 -> listener.invoke(p0) }
        }
        fun build():OptionTextView{
            return OptionTextView(context).apply {
                text=SpannableString(mText)
                setTextColor(mTextColor)
                setBackgroundColor(mBackgroundColor)
                setPadding(mPaddingLeft,mPaddingTop,mPaddingRight,mPaddingBottom)
                setOnClickListener(mListener)
            }
        }
    }
}