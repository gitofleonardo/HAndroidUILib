package cn.huangchengxi.uilib

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import cn.huangchengxi.uilib.utils.dp2px
import kotlin.math.abs
import kotlin.math.min

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
    var backgroundDotColor:Int= Color.rgb(0,0,0)
        set(value) {
            field=value
        }
    var foregroundDotColor:Int=Color.rgb(0,0,0)
        set(value) {
            field=value
        }
    var radius:Float= dp2px(context,6.0f)
        set(value) {
            field=value
        }
    var dotSize:Float= dp2px(context,12.0f)
        set(value) {
            field=value
        }
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
    private var mScrollDirection=ScrollDirection.NONE
        get(){
            return if (mFromIndex==mToIndex) ScrollDirection.NONE
            else if (mFromIndex>mToIndex) ScrollDirection.LEFT
            else ScrollDirection.RIGHT
        }
    private var mScrollOffsetAnimator:Animator?=null

    init {
        val arr=context.obtainStyledAttributes(attrs,R.styleable.PageIndicator)
        backgroundDotColor=arr.getColor(R.styleable.PageIndicator_backgroundDotColor,resources.getColor(R.color.color_page_indicator_default_background))
        foregroundDotColor=arr.getColor(R.styleable.PageIndicator_foregroundColor,resources.getColor(R.color.color_page_indicator_default_foreground))
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
        return (dotSize*(itemCount*2-1)).toInt()
    }
    private fun measureHeight():Int{
        return dotSize.toInt()+paddingTop+paddingBottom
    }
    private fun drawBackgroundDots(canvas: Canvas){
        var startPos=paddingLeft.toFloat()
        for (i in 0 until itemCount){
            canvas.drawRoundRect(startPos,mDrawTop,startPos+dotSize,mDrawBottom,radius,radius,mBackgroundPainter)
            startPos+=dotSize*2
        }
    }
    private fun drawForegroundIndicator(canvas: Canvas){
        Log.e("Draw","Foreground")
        when (mScrollDirection){
            ScrollDirection.NONE->{
                val left=calculateLeftOffsetByPosition()
                canvas.drawRoundRect(left,mDrawTop,left+dotSize,mDrawBottom,radius,radius,mForegroundPaint)
            }
            ScrollDirection.LEFT->{
                val curLength=calculateIndicatorLengthByOffset()
                val mCurOffset=mScrollOffset
                val right:Float
                val left:Float
                if (mCurOffset<=0.5){
                    right=calculateRightOffsetByPosition()
                    left=right-curLength
                }else{
                    left=calculateLeftOffsetByPosition()
                    right=left+curLength
                }
                canvas.drawRoundRect(left,mDrawTop,right,mDrawBottom,radius,radius,mForegroundPaint)
            }
            ScrollDirection.RIGHT->{
                val curLength=calculateIndicatorLengthByOffset()
                val mCurOffset=mScrollOffset
                val right:Float
                val left:Float
                if (mCurOffset<=0.5){
                    left=calculateLeftOffsetByPosition()
                    right=left+curLength
                }else{
                    right=calculateRightOffsetByPosition()
                    left=right-curLength
                }
                canvas.drawRoundRect(left,mDrawTop,right,mDrawBottom,radius,radius,mForegroundPaint)
            }
        }
    }
    private fun calculateLeftOffsetByPosition():Float{
        return dotSize*(position*2)
    }
    private fun calculateRightOffsetByPosition():Float{
        return dotSize*(position*2+1)
    }
    private fun calculateIndicatorLengthByOffset():Float{
        if (mScrollDirection==ScrollDirection.NONE) return dotSize
        val from=mFromIndex
        val to=mToIndex
        val nodeCount= abs(from-to)+1
        val offset= abs(checkValidateOffset())
        //max length
        //min length is zero
        val ds=dotSize*(nodeCount*2-1)
        val res=-6.0f*ds*offset*offset+6.0f*ds*offset
        Log.e("Res","$res")
        val len= when{
            res<0->0.0f
            res>ds->ds
            else->res
        }
        Log.e("len:","$len")
        return len
    }
    private fun checkValidateOffset():Float{
        var offset=mScrollOffset
        if (offset<0) offset=0.0f
        else if (offset>1.0f) offset=1.0f
        Log.e("Offset","$offset")
        return offset
    }
    private fun scrollFromTo(from:Int,to:Int){
        mFromIndex=from
        mToIndex=to
        //start animation
        mScrollOffsetAnimator=ValueAnimator.ofFloat(0.0f,1.0f).apply {
            duration=300
            addUpdateListener {
                val curValue= min(it.animatedValue as Float,1.0f)
                mScrollOffset= curValue
                if (curValue==1.0f){
                    mScrollDirection=ScrollDirection.NONE
                    mFromIndex=0
                    mToIndex=0
                }
            }
        }
        mScrollOffsetAnimator?.start()
    }
    interface OnScrollListener{
        fun onScroll(from:Int,to:Int,offset:Float)
    }
    private enum class ScrollDirection{
        LEFT,
        RIGHT,
        NONE
    }
}