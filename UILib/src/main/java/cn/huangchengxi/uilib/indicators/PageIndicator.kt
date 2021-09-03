package cn.huangchengxi.uilib.indicators

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import cn.huangchengxi.uilib.R
import cn.huangchengxi.uilib.utils.dp2px
import kotlin.math.abs
import kotlin.math.min

/**
 * @author HuangChengxi
 * This view can be used as an indicator for a ViewPager or
 * any view that can snap scroll.
 */
class PageIndicator(context: Context,attrs:AttributeSet?,defStyle:Int,defStyleRes:Int):View(context, attrs,defStyle,defStyleRes) {
    constructor(context: Context,attrs: AttributeSet?,defStyle: Int):this(context,attrs,defStyle,0);
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,0);
    constructor(context: Context):this(context,null)

    /**
     * page item count
     */
    var itemCount:Int=0
        set(value) {
            if (value<0) return
            field=value
            invalidate()
        }
    /**
     * current selected page position
     */
    var position:Int=0
        set(value) {
            if (value<0 || value>=itemCount || value==field) return
            val old=field
            field=value
            scrollFromTo(old,value)
        }

    /**
     * Background's color
     */
    var backgroundDotColor:Int= Color.rgb(0,0,0)
        set(value) {
            field=value
        }

    /**
     * The color of the indicator bar
     */
    var foregroundDotColor:Int=Color.rgb(0,0,0)
        set(value) {
            field=value
        }

    /**
     * Radius of indicator bar
     */
    var radius:Float= dp2px(context,6.0f)
        set(value) {
            field=value
        }

    /**
     * Size of indicator background dot
     */
    var dotSize:Float= dp2px(context,12.0f)
        set(value) {
            field=value
        }

    /**
     * Current scroll offset,ranging from 0.0f to 1.0f
     */
    private var mScrollOffset:Float=0.0f
        set(value) {
            field=value
            invalidate()
        }
    private val mBackgroundPainter by lazy { Paint().apply {
        color=backgroundDotColor
    } }
    private val mForegroundPaint by lazy { Paint().apply {
        color=foregroundDotColor
    } }

    private val mDrawTop
        get() = paddingTop.toFloat()-paddingBottom.toFloat()+height/2-dotSize/2
    private val mDrawBottom
        get() = mDrawTop+dotSize

    private var mFromIndex=0
    private var mToIndex=0

    /**
     * Scroll state
     */
    private var mScrollDirection= ScrollDirection.NONE
        get(){
            return if (mFromIndex==mToIndex) ScrollDirection.NONE
            else if (mFromIndex>mToIndex) ScrollDirection.LEFT
            else ScrollDirection.RIGHT
        }
    private var mScrollOffsetAnimator:Animator?=null
    private var mOnScrollListener: OnScrollListener?=null

    init {
        val arr=context.obtainStyledAttributes(attrs, R.styleable.PageIndicator)
        backgroundDotColor=arr.getColor(
            R.styleable.PageIndicator_backgroundDotColor,resources.getColor(
                R.color.color_page_indicator_default_background
            ))
        foregroundDotColor=arr.getColor(
            R.styleable.PageIndicator_foregroundColor,resources.getColor(
                R.color.color_page_indicator_default_foreground
            ))
        dotSize= dp2px(context,arr.getDimension(R.styleable.PageIndicator_dotSize,12.0f))
        radius= dp2px(context,arr.getDimension(R.styleable.PageIndicator_dotRadius,6.0f))
        arr.recycle()
    }
    override fun onDraw(canvas: Canvas) {
        drawBackgroundDots(canvas)
        drawForegroundIndicator(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode=MeasureSpec.getMode(widthMeasureSpec)
        val heightMode=MeasureSpec.getMode(heightMeasureSpec)
        val hei=if (heightMode==MeasureSpec.EXACTLY){
            MeasureSpec.getSize(heightMeasureSpec)
        }else{
            measureHeight()
        }
        val wid=if (widthMode==MeasureSpec.EXACTLY){
            MeasureSpec.getSize(widthMeasureSpec)
        }else{
            measureWidth()
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(wid,widthMode),MeasureSpec.makeMeasureSpec(hei,heightMode))
    }
    private fun measureWidth():Int{
        if (itemCount==0) return 0
        return (dotSize*(itemCount*2-1)).toInt()
    }
    private fun measureHeight():Int{
        return dotSize.toInt()+paddingTop+paddingBottom
    }
    private fun drawBackgroundDots(canvas: Canvas){
        if (itemCount==0) return
        var startPos=paddingLeft.toFloat()
        for (i in 0 until itemCount){
            canvas.drawRoundRect(startPos,mDrawTop,startPos+dotSize,mDrawBottom,radius,radius,mBackgroundPainter)
            startPos+=dotSize*2
        }
    }
    private fun drawForegroundIndicator(canvas: Canvas){
        if (itemCount==0) return
        when (mScrollDirection){
            ScrollDirection.NONE ->{
                val left=calculateLeftOffsetByPosition(position)
                canvas.drawRoundRect(left,mDrawTop,left+dotSize,mDrawBottom,radius,radius,mForegroundPaint)
            }
            ScrollDirection.LEFT ->{
                val curLength=calculateIndicatorLengthByOffset()
                val mCurOffset=mScrollOffset
                if (mCurOffset<=0.5){
                    val right=calculateRightOffsetByPosition(mFromIndex)
                    val left=right-curLength
                    canvas.drawRoundRect(left,mDrawTop,right,mDrawBottom,radius,radius,mForegroundPaint)
                }else{
                    val left=calculateLeftOffsetByPosition(mToIndex)
                    val right=left+curLength
                    canvas.drawRoundRect(left,mDrawTop,right,mDrawBottom,radius,radius,mForegroundPaint)
                }
            }
            ScrollDirection.RIGHT ->{
                val curLength=calculateIndicatorLengthByOffset()
                val mCurOffset=mScrollOffset
                if (mCurOffset<=0.5){
                    val left=calculateLeftOffsetByPosition(mFromIndex)
                    val right=left+curLength
                    canvas.drawRoundRect(left,mDrawTop,right,mDrawBottom,radius,radius,mForegroundPaint)
                }else{
                    val right=calculateRightOffsetByPosition(mToIndex)
                    val left=right-curLength
                    canvas.drawRoundRect(left,mDrawTop,right,mDrawBottom,radius,radius,mForegroundPaint)
                }
            }
        }
    }
    private fun calculateLeftOffsetByPosition(position:Int):Float{
        return dotSize*(position*2)
    }
    private fun calculateRightOffsetByPosition(position:Int):Float{
        return dotSize*(position*2+1)
    }
    private fun calculateIndicatorLengthByOffset(): Float {
        if (mScrollDirection == ScrollDirection.NONE) return dotSize
        val from = mFromIndex
        val to = mToIndex
        val nodeCount = abs(from - to) + 1
        val offset = abs(checkValidateOffset())
        //max length
        //min length is zero
        val ds = dotSize * (nodeCount * 2 - 1)
        val res = -6.0f * ds * offset * offset + 6.0f * ds * offset+dotSize
        return when {
            res < 0 -> 0.0f
            res > ds -> ds
            else -> res
        }
    }
    private fun checkValidateOffset():Float{
        var offset=mScrollOffset
        if (offset<0) offset=0.0f
        else if (offset>1.0f) offset=1.0f
        return offset
    }
    private fun scrollFromTo(from:Int,to:Int){
        mFromIndex=from
        mToIndex=to
        mScrollOffsetAnimator?.cancel()
        //start animation
        mScrollOffsetAnimator=ValueAnimator.ofFloat(0.0f,1.0f).apply {
            duration=350
            interpolator=FastOutSlowInInterpolator()
            addUpdateListener {
                val curValue= min(it.animatedValue as Float,1.0f)
                mScrollOffset= curValue
                mOnScrollListener?.onScroll(mFromIndex,mToIndex,mScrollOffset)
                if (curValue==1.0f){
                    mScrollDirection= ScrollDirection.NONE
                }
            }
        }
        mScrollOffsetAnimator?.start()
    }
    interface OnScrollListener{
        fun onScroll(from:Int,to:Int,offset:Float)
    }
    fun setOnScrollListener(listener: OnScrollListener){
        this.mOnScrollListener=listener
    }
    enum class ScrollDirection{
        LEFT,
        RIGHT,
        NONE
    }
}