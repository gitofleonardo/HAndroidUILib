package cn.huangchengxi.uilib.container.overscroll

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.OverScroller
import android.widget.Scroller
import cn.huangchengxi.uilib.R
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.sqrt

/**
 * @author HuangChengxi
 * This view is an extending view of {@see android.widget.ScrollView}, which supports
 * overscroll.
 *
 * UGLY CODE
 */
class OverScrollView(context: Context,attrs:AttributeSet?,defStyle:Int,defRes:Int):FrameLayout(context, attrs,defStyle,defRes){
    constructor(context: Context,attrs: AttributeSet?,defStyle: Int):this(context,attrs,defStyle,-1)
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,-1)
    constructor(context: Context):this(context,null)

    private val TAG="OverScrollView"
    private val mOverScroller = OverScroller(context)
    private var mTracker:VelocityTracker?=null
    private var mLastX=0
    private var mLastY=0
    private val MIN_FLING_VELOCITY=30

    /**
     * Animator for restoring over scroll view to its right place(top or bottom) on vertical direction.
     */
    private var mVerticalRestoreAnimator:ValueAnimator?=null

    /**
     * Animator for restoring over scroll view to its right place(left or right) ont horizontal direction.
     */
    private var mHorizontalRestoreAnimator:ValueAnimator?=null

    /**
     * All available scroll area height on vertical direction
     */
    private val mAvailableScrollAreaHeight:Int
        get() = if (childCount==0) 0 else getChildAt(0).measuredHeight

    /**
     * All available scroll area width on horizontal direction
     */
    private val mAvailableScrollAreaWidth:Int
        get() = if (childCount==0) 0 else getChildAt(0).measuredWidth

    /**
     * This ViewGroup's height
     */
    private val mVisibleAreaHeight:Int
        get() = measuredHeight

    /**
     * This ViewGroup's width
     */
    private val mVisibleAreaWidth:Int
        get() = measuredWidth

    /**
     * Whether child view already overscroll in vertical direction
     */
    private val mVerticalAlreadyOverScroll:Boolean
        get() =when{
            scrollY<0->{
                true
            }
            scrollY>0->{
                if (mAvailableScrollAreaHeight<=mVisibleAreaHeight){
                    true
                }else scrollY>mAvailableScrollAreaHeight-mVisibleAreaHeight
            }
            else->{
                false
            }
        }

    /**
     * Whether child view already overscroll in horizontal direction
     */
    private val mHorizontalAlreadyOverScroll:Boolean
        get() = when{
            scrollX<0->{
                true
            }
            scrollX>0->{
                if (mAvailableScrollAreaWidth<=mVisibleAreaWidth){
                    true
                }else scrollX>mAvailableScrollAreaWidth-mVisibleAreaWidth
            }
            else-> {
                false
            }
        }
    /**
     * Supports scroll direction
     */
    private var mScrollDirection:ScrollDirection=ScrollDirection.HORIZONTAL

    init {
        val arr=context.obtainStyledAttributes(attrs,R.styleable.OverScrollView)
        val direction=arr.getInt(R.styleable.OverScrollView_direction,0)
        mScrollDirection = if (direction==0){
            ScrollDirection.VERTICAL
        }else if (direction==1){
            ScrollDirection.HORIZONTAL
        }else{
            throw IllegalArgumentException("Wrong direction type")
        }
        arr.recycle()
        clipChildren=false
    }

    /**
     * We want user to define his own layout style,
     * so this view group can only add a child.
     */
    override fun addView(child: View?) {
        if (childCount>0){
            throw IllegalArgumentException("Only one child can be added to this view.")
        }
        super.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        if (childCount>0){
            throw IllegalArgumentException("Only one child can be added to this view.")
        }
        super.addView(child, index)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        if (childCount>0){
            throw IllegalArgumentException("Only one child can be added to this view.")
        }
        super.addView(child, width, height)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        if (childCount>0){
            throw IllegalArgumentException("Only one child can be added to this view.")
        }
        super.addView(child, params)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (childCount>0){
            throw IllegalArgumentException("Only one child can be added to this view.")
        }
        super.addView(child, index, params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount>0){
            val child=getChildAt(0)
            val widthPadding:Int
            val heightPadding:Int
            val targetApi=context.applicationInfo.targetSdkVersion
            val params=child.layoutParams as LayoutParams
            if (targetApi>=Build.VERSION_CODES.M){
                widthPadding=paddingLeft+paddingRight+params.leftMargin+params.rightMargin
                heightPadding=paddingTop+paddingBottom+params.topMargin+params.bottomMargin
            }else{
                widthPadding=paddingLeft+paddingRight
                heightPadding=paddingTop+paddingBottom
            }
            val desiredHeight=measuredHeight-heightPadding
            val desiredWidth=measuredWidth-widthPadding
            when(mScrollDirection){
                ScrollDirection.VERTICAL->{
                    if (child.measuredHeight<desiredHeight){
                        val widthSpec= getChildMeasureSpec(widthMeasureSpec,widthPadding,params.width)
                        val heightSpec=MeasureSpec.makeMeasureSpec(desiredHeight,MeasureSpec.UNSPECIFIED)
                        child.measure(widthSpec,heightSpec)
                    }
                }
                ScrollDirection.HORIZONTAL->{
                    if (child.measuredWidth<desiredWidth){
                        val widthSpec=MeasureSpec.makeMeasureSpec(desiredWidth,MeasureSpec.UNSPECIFIED)
                        val heightSpec= getChildMeasureSpec(heightMeasureSpec,heightPadding,params.height)
                        child.measure(widthSpec,heightSpec)
                    }
                }
            }
        }
    }

    override fun measureChild(
        child: View, parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int
    ) {
        val lp = child.layoutParams
        val childWidthMeasureSpec: Int
        val childHeightMeasureSpec: Int
        childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec, paddingLeft
                    + paddingRight, lp.width
        )
        val verticalPadding: Int = paddingTop + paddingBottom
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
            Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - verticalPadding),
            MeasureSpec.UNSPECIFIED
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }
    override fun measureChildWithMargins(
        child: View, parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin
                    + widthUsed, lp.width
        )
        val usedTotal: Int = (paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin +
                heightUsed)
        val childHeightMeasureSpec: Int = MeasureSpec.makeMeasureSpec(
            Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - usedTotal),
            MeasureSpec.UNSPECIFIED
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event==null){
            return super.onTouchEvent(event)
        }
        val curX=event.x.toInt()
        val curY=event.y.toInt()
        when (event.action){
            MotionEvent.ACTION_DOWN->{
                //Cancel all the animators
                cancelAllRestoreAnimator()
                mOverScroller.forceFinished(true)
                mTracker= VelocityTracker.obtain()
                mLastX=curX
                mLastY=curY
                return true
            }
            MotionEvent.ACTION_MOVE->{
                when(mScrollDirection){
                    ScrollDirection.VERTICAL->{
                        if (mLastY==curY){
                            return true
                        }
                    }
                    ScrollDirection.HORIZONTAL->{
                        if (mLastX==curX){
                            return true
                        }
                    }
                }
                var moveX=mLastX-curX
                var moveY=mLastY-curY
                if (mHorizontalAlreadyOverScroll){
                    moveX=computeOverScrollValue(moveX)
                }
                if (mVerticalAlreadyOverScroll){
                    moveY=computeOverScrollValue(moveY)
                }
                when (mScrollDirection){
                    ScrollDirection.VERTICAL->{
                        scrollBy(0,moveY)
                    }
                    ScrollDirection.HORIZONTAL->{
                        scrollBy(moveX,0)
                    }
                }
                mTracker?.addMovement(event)
                mLastX=curX
                mLastY=curY
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL->{
                //When user disable vertical scroll, we don't restore it
                if (mVerticalAlreadyOverScroll && mScrollDirection!=ScrollDirection.HORIZONTAL){
                    scheduleVerticalRestoreAnimator()
                }
                //When user disable horizontal scroll, we don't restore it
                if (mHorizontalAlreadyOverScroll && mScrollDirection!=ScrollDirection.VERTICAL){
                    scheduleHorizontalRestoreAnimator()
                }
                mTracker?.computeCurrentVelocity(1000)
                val velocityX=mTracker?.xVelocity?.toInt()?:0
                val velocityY=mTracker?.yVelocity?.toInt()?:0
                processFling(velocityX,velocityY)
                mTracker?.recycle()
                mTracker=null
                return true
            }
            else->{
                return super.onTouchEvent(event)
            }
        }
    }
    private fun computeOverScrollValue(distance:Int):Int{
        return distance/2
    }
    private fun processFling(xVelocity:Int,yVelocity:Int){
        var xv=xVelocity
        var yv=yVelocity
        xv=if (abs(xv) >=MIN_FLING_VELOCITY) xv else 0
        yv=if (abs(yv) >=MIN_FLING_VELOCITY) yv else 0
        val scrollMaxHeight=if (mAvailableScrollAreaHeight<=mVisibleAreaHeight) 0 else mAvailableScrollAreaHeight-mVisibleAreaHeight
        val scrollMaxWidth=if (mAvailableScrollAreaWidth<=mVisibleAreaWidth) 0 else mAvailableScrollAreaWidth-mVisibleAreaWidth
        //we don't want to process a fling when over scrolling
        when (mScrollDirection){
            ScrollDirection.VERTICAL->{
                if (mVerticalAlreadyOverScroll){
                    return
                }
                mOverScroller.fling(0,scrollY,0,-yv,0,0,0,scrollMaxHeight)
            }
            ScrollDirection.HORIZONTAL->{
                if (mHorizontalAlreadyOverScroll){
                    return
                }
                mOverScroller.fling(scrollX,0,-xv,0,0,scrollMaxWidth,0,0)
            }
        }
    }

    /**
     * When user touching down, we want to stop the animators which restore
     * the child views to is place.
     */
    private fun cancelAllRestoreAnimator(){
        mVerticalRestoreAnimator?.cancel()
        mHorizontalRestoreAnimator?.cancel()
        mVerticalRestoreAnimator=null
        mHorizontalRestoreAnimator=null
    }

    private fun scheduleVerticalRestoreAnimator(){
        val shouldRestoreTo = if (scrollY<0 ||
            (scrollY>0 && mAvailableScrollAreaHeight<=mVisibleAreaHeight)){
            //restore to top
            0
        }else{
            //restore to bottom
            mAvailableScrollAreaHeight-mVisibleAreaHeight
        }
        val currentScroll=scrollY
        mVerticalRestoreAnimator=ValueAnimator.ofInt(currentScroll,shouldRestoreTo).apply {
            duration=300
            addUpdateListener {
                val curVal=it.animatedValue as Int
                scrollTo(scrollX,curVal)
            }
        }
        mVerticalRestoreAnimator?.start()
    }
    private fun scheduleHorizontalRestoreAnimator(){
        val shouldRestoreTo=if (scrollX<0 ||
            (scrollX>0 && mAvailableScrollAreaWidth<=mVisibleAreaWidth)){
            //restore to top
            0
        }else{
            //restore to bottom
            mAvailableScrollAreaWidth-mVisibleAreaWidth
        }
        val currentScroll=scrollX
        mHorizontalRestoreAnimator= ValueAnimator.ofInt(currentScroll,shouldRestoreTo).apply {
            duration=300
            addUpdateListener {
                val curVal=it.animatedValue as Int
                scrollTo(curVal,scrollY)
            }
        }
        mHorizontalRestoreAnimator?.start()
    }

    override fun computeScroll() {
        val compute=mOverScroller.computeScrollOffset()
        if (compute){
            val scrollX=mOverScroller.currX
            val scrollY=mOverScroller.currY
            scrollTo(scrollX,scrollY)
            invalidate()
        }
    }

    enum class ScrollDirection(val direction:Int){
        /**
         * Supports vertical scroll
         */
        VERTICAL(0),

        /**
         * Supports horizontal scroll
         */
        HORIZONTAL(1),
    }
}