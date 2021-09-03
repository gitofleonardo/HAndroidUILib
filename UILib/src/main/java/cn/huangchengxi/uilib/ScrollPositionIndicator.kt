package cn.huangchengxi.uilib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import cn.huangchengxi.uilib.utils.dp2px

/**
 * @author Huang Chengxi
 * This view is a simple imitation of Taobao's progress indicator in its homepage.
 * This view can be associated with another view which can be finitely scrolled.
 * For example, you can use it to bind with a RecyclerView with limited items.
 * And when the RecyclerView scrolled, this view will update the progress.
 */
class ScrollPositionIndicator(context: Context,attrs:AttributeSet?,defStyle:Int):View(context,attrs,defStyle),GestureDetector.OnGestureListener{
    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,-1)
    constructor(context: Context):this(context,null)

    //Default background color
    private var mBackgroundColor=Color.argb(255,240,240,240)
    //Default block color
    private var mBlockColor=Color.argb(255,255,87,34)
    private val mDetector=GestureDetector(context,this)
    private var mCurrentPercent=0.0f

    /**
     * The percent of visible content of the entire view(or ViewGroup).
     * For example, if a RecyclerView holds 20 items, and now is 5 items are visible,
     * The percent is 5/20=0.25
     */
    private var mScrollablePercent=0.5f
    private var mPaint=Paint()
    private val mRectF=RectF()
    private val mBackgroundRectF=RectF()
    private var mListener:OnPercentChangeListener?=null

    var scrollable=true

    /**
     * Exposed percent for user to set and get
     */
    var percent:Float = 0.0f
        get() = mCurrentPercent
        set(value) {
            field = value
            val old=mCurrentPercent
            mCurrentPercent=value
            mListener?.onChange(old,mCurrentPercent,false)
            invalidate()
        }
    var scrollablePercent:Float=0.0f
        get() = mScrollablePercent
        set(value) {
            field=value
            mScrollablePercent=value
            if (mScrollablePercent<=0.0f){
                visibility=GONE
            }
            invalidate()
        }
    var blockHeight:Float=0.0f
        set(value) {
            field=value
            invalidate()
        }

    private val blockTop:Float
        get() = paddingTop.toFloat()-paddingBottom.toFloat()+height/2-blockHeight/2
    private val blockLeft:Float
        get() = paddingLeft.toFloat()+percent*remainWidth()
    private val blockRight:Float
        get() = blockLeft+blockWidth()
    private val blockBottom:Float
        get() = blockTop+blockHeight

    init {
        val array=context.obtainStyledAttributes(attrs,R.styleable.ScrollPositionIndicator)
        mBlockColor=array.getColor(R.styleable.ScrollPositionIndicator_blockColor,mBlockColor)
        mBackgroundColor=array.getColor(R.styleable.ScrollPositionIndicator_blockBackground,mBackgroundColor)
        scrollable=array.getBoolean(R.styleable.ScrollPositionIndicator_scrollable,true)
        blockHeight=array.getDimension(R.styleable.ScrollPositionIndicator_blockWidth, dp2px(context,10.0f))
        array.recycle()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mDetector.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpec=getDefaultSize(suggestedMinimumWidth,widthMeasureSpec)
        val mode=MeasureSpec.getMode(heightMeasureSpec)
        val size=MeasureSpec.getSize(heightMeasureSpec)
        val result = when (mode){
            MeasureSpec.AT_MOST->{
                (blockHeight+paddingTop+paddingBottom).toInt()
            }
            MeasureSpec.EXACTLY->{
                size
            }
            MeasureSpec.UNSPECIFIED->{
                suggestedMinimumHeight
            }
            else->{
                throw IllegalArgumentException("Invalid Argument")
            }
        }
        val heightSpec=MeasureSpec.makeMeasureSpec(result,mode)
        setMeasuredDimension(widthSpec,heightSpec)
    }

    override fun onDraw(canvas: Canvas) {
        //draw rect block
        canvas.drawRoundRect(backgroundRectF(),blockHeight/2,blockHeight/2,backgroundPaint())
        canvas.drawRoundRect(blockRectF(),blockHeight/2,blockHeight/2,blockPaint())
    }

    private fun blockRectF():RectF{
        return mRectF.apply {
            top=blockTop
            left=blockLeft
            right=blockRight
            bottom=blockBottom
        }
    }
    private fun backgroundRectF():RectF{
        return mBackgroundRectF.apply {
            top=blockTop
            left=paddingLeft.toFloat()
            right=width.toFloat()-paddingRight
            bottom=blockBottom
        }
    }
    private fun remainWidth():Float{
        return width-paddingLeft-paddingRight-blockWidth()
    }
    private fun blockWidth():Float{
        return (width-paddingLeft-paddingRight)*mScrollablePercent
    }

    /**
     * Get line paint for drawing baseline
     */
    private fun backgroundPaint():Paint{
        return mPaint.apply {
            color=mBackgroundColor
        }
    }

    /**
     * Get block paint for drawing progress block
     */
    private fun blockPaint():Paint{
        return mPaint.apply {
            color=mBlockColor
        }
    }

    /**
     * Only detect event down when user is touching inside the progress block.
     */
    override fun onDown(e: MotionEvent): Boolean {
        val ex=e.x
        val ey=e.y
        return ex in blockLeft..blockRight && ey in blockTop..blockBottom
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        performClick()
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!scrollable) return false

        val distancePercent=distanceX/(width-paddingLeft-paddingRight)
        val old=percent
        percent-=distancePercent
        return if (percent<0){
            percent=0.0f
            mListener?.onChange(old,percent,true)
            false
        }else if (percent>1){
            percent=1.0f
            mListener?.onChange(old,percent,true)
            false
        }else{
            mListener?.onChange(old,percent,true)
            invalidate()
            true
        }
    }

    override fun onLongPress(e: MotionEvent?) {
        performLongClick()
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    fun setOnPercentChangeListener(listener:OnPercentChangeListener){
        mListener=listener
    }

    /**
     * Lamda version
     */
    fun setOnPercentChangeListener(listener:(Float,Float,Boolean)->Unit){
        mListener=object : OnPercentChangeListener{
            override fun onChange(old: Float, new: Float, fromUser: Boolean) {
                listener.invoke(old,new,fromUser)
            }
        }
    }

    /**
     * Call back interface for handling percentage change
     * Of this view.
     */
    interface OnPercentChangeListener{
        /**
         * @param old The previous value of percent
         * @param new The new value of percent
         * @param fromUser Indicates whether the change action is triggered by user or not.For example, when user
         * Touch the slide block and drag it, it means that the change action is triggered by user. But when the percent
         * Is assigned to another value, it means the change action is not triggered by the user, by the code instead.
         */
        fun onChange(old:Float,new:Float,fromUser:Boolean)
    }
}