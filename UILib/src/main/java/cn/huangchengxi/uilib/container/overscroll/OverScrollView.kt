package cn.huangchengxi.uilib.container.overscroll

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.OverScroller
import android.widget.Scroller

/**
 * @author HuangChengxi
 * This view is an extending view of {@see android.widget.ScrollView}, which supports
 * overscroll.
 */
class OverScrollView(context: Context,attrs:AttributeSet?,defStyle:Int,defRes:Int):FrameLayout(context, attrs,defStyle,defRes){
    constructor(context: Context,attrs: AttributeSet?,defStyle: Int):this(context,attrs,defStyle,-1)
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,-1)
    constructor(context: Context):this(context,null)

    private val TAG="OverScrollView"
    private val mOverScroller = Scroller(context)
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
    private var mScrollDirection:ScrollDirection=ScrollDirection.VERTICAL

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
            val childWidthSpec= MeasureSpec.makeMeasureSpec(desiredWidth,MeasureSpec.EXACTLY)
            val childHeightSpec= MeasureSpec.makeMeasureSpec(desiredHeight,MeasureSpec.EXACTLY)
            Log.v(TAG,"Child measure width height before:${child.measuredWidth},${child.measuredHeight}")
            child.measure(childWidthSpec,desiredHeight)
            Log.v(TAG,"Child measure width height after:${child.measuredWidth},${child.measuredHeight}")
        }
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
                    ScrollDirection.ALL->{
                        if (mLastX==curX && mLastY==curY){
                            return true
                        }
                    }
                }
                val moveX=mLastX-curX
                val moveY=mLastY-curY
                //Log.v(TAG,"Move:${moveX},${moveY}")
                when (mScrollDirection){
                    ScrollDirection.ALL->{
                        scrollBy(moveX,moveY)
                    }
                    ScrollDirection.VERTICAL->{
                        scrollBy(0,moveY)
                    }
                    ScrollDirection.HORIZONTAL->{
                        scrollBy(moveX,0)
                    }
                }
                mTracker?.addMovement(event)
                mTracker?.computeCurrentVelocity(1000)
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
                val velocityX=mTracker?.xVelocity?.toInt()?:0
                val velocityY=mTracker?.yVelocity?.toInt()?:0
                processFling(velocityX,velocityY)
                return true
            }
            else->{
                return super.onTouchEvent(event)
            }
        }
    }

    /**
     * 说的什么鸡巴玩意这个傻逼scroller
     * 注释说的是什么鸡巴东西不清不楚傻逼东西
     */
    private fun processFling(xVelocity:Int,yVelocity:Int){
        var xv=xVelocity
        var yv=yVelocity
        xv=if (xv>=MIN_FLING_VELOCITY) xv else 0
        yv=if (yv>=MIN_FLING_VELOCITY) yv else 0
        val availableVerticalScroll=mAvailableScrollAreaHeight-mVisibleAreaHeight
        val availableHorizontalScroll=mAvailableScrollAreaWidth-mVisibleAreaWidth
        when (mScrollDirection){
            ScrollDirection.ALL->{
                mOverScroller.fling(scrollX,scrollY,-xv,-yv,0,availableHorizontalScroll,0,availableVerticalScroll)
            }
            ScrollDirection.VERTICAL->{
                mOverScroller.fling(0,scrollY,0,-yv,0,0,-10000,availableVerticalScroll)
            }
            ScrollDirection.HORIZONTAL->{
                mOverScroller.fling(scrollX,0,-xv,0,0,availableHorizontalScroll,0,0)
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
            (scrollY>0 && scrollY<mAvailableScrollAreaHeight-mVisibleAreaHeight) ||
            (mAvailableScrollAreaHeight<=mVisibleAreaHeight)){
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
            (scrollX>0 && scrollX<mAvailableScrollAreaWidth-mVisibleAreaWidth) ||
            (mAvailableScrollAreaWidth<=mVisibleAreaWidth)){
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
        super.computeScroll()
        val compute=mOverScroller.computeScrollOffset();
        if (compute){
            val scrollX=mOverScroller.currX
            val scrollY=mOverScroller.currY
            Log.v(TAG,"Scroll to ${scrollX},${scrollY}")
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

        /**
         * Supports scroll in all direction
         */
        ALL(2)
    }
}