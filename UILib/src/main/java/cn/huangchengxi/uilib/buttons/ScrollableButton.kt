package cn.huangchengxi.uilib.buttons

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * @author HuangChengxi
 * A scrollable button widget, which can be swipe
 * to toggle the expand state.
 */
class ScrollableButton(context: Context,attrs:AttributeSet?,defStyle:Int,defStyleRes:Int) :
    LinearLayout(context, attrs,defStyle,defStyleRes),GestureDetector.OnGestureListener{
    constructor(context: Context,attrs: AttributeSet?,defStyle: Int):this(context,attrs,defStyle,0);
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,0)
    constructor(context: Context):this(context,null)

    /**
     * Whether button is currently expanded
     */
    var expanded=false
        set(value) {
            if (field!=value){
                field=value
                setToExpanded(value)
            }
        }

    /**
     * True if this widget didn't receive any
     * ACTION_MOVE event.
     */
    private var mFirstMove=true
    private val mGestureDetector by lazy { GestureDetector(context,this) }
    private var mLastTouchX=0.0f
    private var mLastTouchY=0.0f
    private var mAnimator:ValueAnimator?=null
    private var mOffset:Float=0.0f
    private var mTotalWidth:Float=0.0f

    /**
     * set button to non-expanded if click the action buttons
     */
    var closeOnAction=true

    /**
     * Content is clickable when true, else false
     */
    var contentClickableOnExpanded=false

    private var mOnContentClickListener:OnContentClickListener?=null
    private var mOnContentLongClickListener:OnContentLongClickListener?=null

    init {
        orientation= HORIZONTAL
        //Measure total available width
        viewTreeObserver.addOnGlobalLayoutListener {
            mTotalWidth=0.0f
            for (child in children){
                mTotalWidth+=child.measuredWidth
            }
        }
    }

    override fun addView(child: View) {
        if (childCount==0){
            measureContentParam(child)
        }else{
            measureActionParam(child,null)
        }
        super.addView(child)
    }

    override fun addView(child: View, index: Int) {
        if (childCount==0){
            measureContentParam(child)
        }else{
            measureActionParam(child,null)
        }
        super.addView(child,index)
    }

    override fun addView(child: View, width: Int, height: Int) {
        if (childCount==0){
            measureContentParam(child)
            super.addView(child,width, height)
        }else{
            val params=ViewGroup.LayoutParams(width, height)
            measureActionParam(child,params)
            super.addView(child, params.width, params.height)
        }
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        if (childCount==0){
            measureContentParam(child)
        }else{
            measureActionParam(child,params)
        }
        super.addView(child,params)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (childCount==0){
            measureContentParam(child)
        }else{
            measureActionParam(child,params)
        }
        super.addView(child,index,params)
    }

    /**
     * Measure the spec of child, set measure mode to EXACTLY instead of AT_MOST
     * since the LinearLayout will set the child view's width or height to 0 when
     * they are out of bound.
     */
    private fun measureActionParam(child: View,params: ViewGroup.LayoutParams?){
        val param=ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
        child.layoutParams=param
        measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
        child.measure(measuredWidth,measuredHeight)
        val width=child.measuredWidth
        if (params!=null){
            params.width=width
            params.height=ViewGroup.LayoutParams.MATCH_PARENT
            child.layoutParams=params
        }else{
            child.layoutParams= ViewGroup.LayoutParams(width,ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    /**
     * Set content's LayoutParam to MATCH_PARENT
     */
    private fun measureContentParam(child: View){
        val param=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        child.layoutParams=param
        setupContentListener(child)
    }
    private fun setupContentListener(child: View){
        child.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View) {
                if ((expanded && contentClickableOnExpanded) || !expanded){
                    mOnContentClickListener?.onClick(v)
                }else{
                    expanded=false
                }
            }
        })
        child.setOnLongClickListener(object : OnLongClickListener{
            override fun onLongClick(v: View): Boolean {
                if ((expanded && contentClickableOnExpanded) || !expanded){
                    mOnContentLongClickListener?.onLongClick(v)
                }else{
                    expanded=false
                }
                return true
            }
        })
    }
    fun addOption(view:OptionTextView,onOptionClickListener:OnOptionClickListener){
        addOption(view,childCount,onOptionClickListener)
    }
    fun addOption(view: OptionTextView,index: Int,onOptionClickListener: OnOptionClickListener){
        view.setOnClickListener {
            if (closeOnAction && expanded){
                expanded=!expanded
            }else{
                onOptionClickListener.onClick(view)
            }
        }
        addView(view,index, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT))
        requestLayout()
    }

    /**
     * Animate to expand
     */
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mFirstMove=true
                mLastTouchX=event.x
                mLastTouchY=event.y
                return mGestureDetector.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE->{
                if (!mFirstMove){
                    return mGestureDetector.onTouchEvent(event)
                }
                val curX=event.x
                val curY=event.y
                if (curX==mLastTouchX && curY==mLastTouchY){
                    return false
                }
                mFirstMove=false
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
                if (!mFirstMove){
                    if (mOffset>=(mTotalWidth-measuredWidth)/2){
                        setToExpanded(true)
                    }else{
                        setToExpanded(false)
                    }
                    return mGestureDetector.onTouchEvent(event)
                }
                return false
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * If the ACTION_MOVE does not occur, this widget will not
     * proceed interception
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var intercepted=false
        when (ev.action){
            MotionEvent.ACTION_DOWN->{
                intercepted=false
                onTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE->{
                intercepted = !(ev.x==mLastTouchX && ev.y==mLastTouchY)
            }
            MotionEvent.ACTION_UP->{
                intercepted=!mFirstMove
                onTouchEvent(ev)
            }
        }
        return intercepted
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        mAnimator?.cancel()
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {
        super.onTouchEvent(p0)
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return false
    }

    /**
     * Scroll by specific distance.
     * The offset is ranging from 0.0f to mTotalWidth-measureWidth
     */
    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
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
        return true
    }
    override fun onLongPress(p0: MotionEvent?) {
    }

    /**
     * Set to expanded or non-expanded when the
     * velocity is faster then 30.
     */
    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, velX: Float, velY: Float): Boolean {
        if (velX>=30){
            expanded=false
        }else if (velX<=-30){
            expanded=true
        }
        return true
    }
    fun setOnContentClickListener(listener: OnContentClickListener){
        mOnContentClickListener=listener
        setupContentListener(getChildAt(0))
    }
    fun setOnContentLongClickListener(listener: OnContentLongClickListener){
        mOnContentLongClickListener=listener
        setupContentListener(getChildAt(0))
    }
    interface OnOptionClickListener{
        fun onClick(view: OptionTextView)
    }
    interface OnContentClickListener{
        fun onClick(view:View)
    }
    interface OnContentLongClickListener{
        fun onLongClick(view: View)
    }
}