package cn.huangchengxi.uilib.container.nested

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.core.view.*

class RefreshLayout(context: Context,attrs:AttributeSet?,defStyleAttr:Int,defStyleRes:Int):
    ViewGroup(context, attrs,defStyleAttr, defStyleRes),
    NestedScrollingParent3{

    constructor(context: Context,attrs: AttributeSet?,defStyleAttr: Int):this(context,attrs,defStyleAttr,-1)
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,-1)
    constructor(context: Context):this(context,null)

    private val TAG="RefreshLayout"

    private val mNestedParentHelper=NestedScrollingParentHelper(this)
    private val mNestedChildHelper=NestedScrollingChildHelper(this)
    private val mOverScroller=OverScroller(context)
    private val mWidthPadding:Int
        get() = paddingLeft+paddingRight+paddingStart+paddingEnd
    private val mHeightPadding:Int
        get() = paddingTop+paddingBottom
    private var mRestoreAnimator:ValueAnimator?=null

    init {
        isNestedScrollingEnabled=true
    }

    /**
     * This view can only add three views at most(header, nested view and an optional footer), and two views at lease,
     * since there's no need  to use this view if the number of view is less than
     * three.
     */
    override fun addView(child: View?) {
        if (childCount>3){
            throw IllegalArgumentException("Cannot add more than three views.")
        }
        super.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        if (childCount>3){
            throw IllegalArgumentException("Cannot add more than three views.")
        }
        super.addView(child, index)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        if (childCount>3){
            throw IllegalArgumentException("Cannot add more than three views.")
        }
        super.addView(child, width, height)
    }

    override fun addView(child: View?, params: LayoutParams?) {
        if (childCount>3){
            throw IllegalArgumentException("Cannot add more than three views.")
        }
        super.addView(child, params)
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        if (childCount>3){
            throw IllegalArgumentException("Cannot add more than three views.")
        }
        super.addView(child, index, params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount<2){
            return
        }
        //header view is always in the first place
        //so measure it
        val headerView=getChildAt(0)
        measureChild(headerView,widthMeasureSpec,heightMeasureSpec)

        //nested view is in the second place
        //so measure it now
        val nestedChildView=getChildAt(1)
        //nested child view needs to match parent
        val nestedHeightSpec=LayoutParams.MATCH_PARENT
        measureChild(nestedChildView,widthMeasureSpec,heightMeasureSpec)

        if (childCount>2){
            //has footer view
            //measure it now
            val footerView=getChildAt(2)
            measureChild(footerView,widthMeasureSpec,heightMeasureSpec)
        }
        setMeasuredDimension(layoutParams.width,layoutParams.height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount<2){
            return
        }
        //get header view and place it on top
        val headerView=getChildAt(0)
        headerView.layout(paddingLeft+paddingStart,-headerView.measuredHeight,paddingLeft+paddingStart+headerView.measuredWidth,0)
        //get nested view
        val nestedChild=getChildAt(1)
        nestedChild.layout(paddingLeft+paddingStart,0,paddingLeft+paddingStart+nestedChild.measuredWidth,nestedChild.measuredHeight)
        //place footer view
        if (childCount>2){
            val footerView=getChildAt(2)
            footerView.layout(paddingLeft+paddingStart,measuredHeight,paddingLeft+paddingStart+footerView.measuredWidth,measuredHeight+footerView.measuredHeight)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event==null){
            return super.onTouchEvent(event)
        }
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                cancelRestoreAnimator()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL->{
                scheduleRestoreAnimator()
            }
        }
        return true
    }
    private fun scheduleRestoreAnimator(){
        cancelRestoreAnimator()
        val alreadyScrolled=scrollY
        mRestoreAnimator= ValueAnimator.ofInt(alreadyScrolled,0).apply {
            duration=250
            addUpdateListener {
                val value=it.animatedValue as Int
                scrollTo(scrollX,value)
            }
            start()
        }
    }
    private fun cancelRestoreAnimator(){
        mRestoreAnimator?.cancel()
        mRestoreAnimator=null
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        Log.v(TAG,"OnStartNestedScroll")
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        Log.v(TAG,"OnNestedScrollAccepted")
        mNestedParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        Log.v(TAG,"OnStopNestedScroll")
        mNestedParentHelper.onStopNestedScroll(target, type)
        scheduleRestoreAnimator()
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        Log.v(TAG,"OnNestedScroll1:${dyConsumed}")
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        Log.v(TAG,"OnNestedScroll2:${dyConsumed}")
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.v(TAG,"OnNestedPreScroll:${consumed.contentToString()},${dy}")
        scrollBy(0,computeMoveY(dy))
        consumed[1]=dy
    }
    private fun computeMoveY(originDistance:Int):Int{
        return originDistance/2
    }
}