package cn.huangchengxi.uilib.buttons

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class ScrollableButton(context: Context,attrs:AttributeSet?,defStyle:Int,defStyleRes:Int) :
    ScrollView(context, attrs,defStyle,defStyleRes),GestureDetector.OnGestureListener{
    constructor(context: Context,attrs: AttributeSet?,defStyle: Int):this(context,attrs,defStyle,0);
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,0)
    constructor(context: Context):this(context,null)

    var expanded=false
        set(value) {
            if (field!=value){
                setToExpanded(value)
            }
            field=value
        }
    private var mFirstMove=true
    private val mGestureDetector by lazy { GestureDetector(context,this) }
    private var mLastTouchX=0.0f
    private var mLastTouchY=0.0f
    private var mAnimator:ValueAnimator?=null
    private var mOffset:Float=0.0f
    private var mTotalWidth:Float=0.0f
    private val mLinearContainer by lazy { LinearLayout(context).apply {
        orientation=LinearLayout.HORIZONTAL
        layoutParams= LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
    } }

    var closeOnAction=true
    var contentClickableOnExpanded=false

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            mTotalWidth=0.0f
            for (child in children){
                mTotalWidth+=child.measuredWidth
            }
            val child=mLinearContainer.getChildAt(0)
            child.layoutParams=LayoutParams(measuredWidth,measuredHeight)
        }
    }

    override fun addView(child: View) {
        if (childCount==0){
            super.addView(child)
        }else{
            mLinearContainer.addView(child)
        }
    }

    override fun addView(child: View, index: Int) {
        if (childCount==0){
            super.addView(child,index)
        }else{
            mLinearContainer.addView(child,index)
        }
    }

    override fun addView(child: View, width: Int, height: Int) {
        if (childCount==0){
            super.addView(child,width, height)
        }else{
            mLinearContainer.addView(child,width,height)
        }
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams?) {
        if (childCount==0){
            super.addView(child,params)
        }else{
            mLinearContainer.addView(child,params)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (childCount==0){
            super.addView(child,index,params)
        }else{
            mLinearContainer.addView(child,index,params)
        }
    }
    private fun setChildParam(child: View){
        if (childCount==0)
            child.layoutParams= LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        else
            child.layoutParams= LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun setToExpanded(expanded:Boolean){
        mAnimator= if (expanded){
            if (mOffset==mTotalWidth-measuredWidth){
                return
            }
            ValueAnimator.ofFloat(mOffset,mTotalWidth-measuredWidth)
        }else{
            if (mOffset==0.0f){
                return
            }
            ValueAnimator.ofFloat(mOffset,0.0f)
        }.apply {
            interpolator=FastOutSlowInInterpolator()
            duration=100
            addUpdateListener {
                val cur=it.animatedValue as Float
                mOffset=cur.toInt().toFloat()
                scrollTo(cur.toInt(),0)
            }
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                mLastTouchX=event.x
                mLastTouchY=event.y
                return mGestureDetector.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE->{
                if (!mFirstMove){
                    return mGestureDetector.onTouchEvent(event)
                }
                mFirstMove=false
                val curX=event.x
                val curY=event.y
                if (curX==mLastTouchX && curY==mLastTouchY){
                    return mGestureDetector.onTouchEvent(event)
                }
                if (curY==mLastTouchY){
                    return mGestureDetector.onTouchEvent(event)
                }
                val ang=(curX-mLastTouchX)/(curY-mLastTouchY)
                return if (ang<=1){
                    false
                }else{
                    mGestureDetector.onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP->{
                mFirstMove=true
                if (mOffset>=(mTotalWidth-measuredWidth)/2){
                    setToExpanded(true)
                }else{
                    setToExpanded(false)
                }
                return mGestureDetector.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return !mFirstMove
    }
    private fun isDownOnContentView(event: MotionEvent):Boolean{
        val x=event.x
        val y=event.y
        val content=getChildAt(0)
        return (y>=content.bottom && x<=content.top) && (x>=content.left && x<=content.right)
    }

    override fun onDown(p0: MotionEvent): Boolean {
        mAnimator?.cancel()
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        super.onTouchEvent(p0)
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        Log.e("Tap","T")
        return false
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        val availableScrollWidth=mTotalWidth-measuredWidth
        if ((mOffset>=availableScrollWidth && distanceX>=0) || (distanceX<=0 && mOffset<=0.0f)){
            return true
        }
        var scroll=distanceX.toInt()
        if (mOffset+scroll>availableScrollWidth){
            scroll=(availableScrollWidth-mOffset).toInt()
            mOffset=availableScrollWidth
        }else if (mOffset+scroll<0){
            scroll=-mOffset.toInt()
            mOffset=0.0f
        }else{
            mOffset+=scroll
        }
        scrollBy(scroll,0)
        Log.e("Scroll","$scroll")
        return true
    }
    override fun onLongPress(p0: MotionEvent) {
        Log.e("Long press","lp")
        super.onTouchEvent(p0)
    }
    override fun onFling(p0: MotionEvent, p1: MotionEvent, velX: Float, velY: Float): Boolean {
        if (velX>=30){
            setToExpanded(false)
        }else if (velX<=-30){
            setToExpanded(true)
        }
        return true
    }
}
