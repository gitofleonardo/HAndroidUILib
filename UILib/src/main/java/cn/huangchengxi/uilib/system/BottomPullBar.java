package cn.huangchengxi.uilib.system;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import cn.huangchengxi.uilib.R;

/**
 * @author huangchengxi
 * A bottom, which is placed at the bottom of the screen
 * and can be pulled up for functionality.
 */
public class BottomPullBar extends LinearLayout implements GestureDetector.OnGestureListener {
    private static final String TAG="BottomPullBar";

    private static final int DEFAULT_MARGIN_BOTTOM_DP=4;
    private static final int DEFAULT_BAR_HEIGHT_DP=5;
    private static final int DEFAULT_BAR_PULL_MAX_OFFSET_DP=24;
    private static final int DEFAULT_BAR_EXPAND_HIDE_DURATION=300;
    private static final int DEFAULT_BAR_RESTORE_MAX_DURATION=300;

    private static final int MSG_EXPAND_BAR=1;
    private static final int MSG_HIDE_BAR=2;
    private static final int MSG_PERFORMING_LONG_PULL_TOP=3;

    /**
     * 4 seconds to wait for bar closing.
     */
    private final int DEFAULT_BAR_HIDE_WAIT_TIME=4000;
    /**
     * Takes 1 second to wait for a long pull
     */
    private final int DEFAULT_BAR_PULL_TOP_LONG_TIME=1000;
    /**
     * Bar view at the bottom
     */
    private View mBarView;
    /**
     * Height of the @{#mBarView}
     */
    private int mBarHeight;
    private int mMaxPullOffset;
    /**
     * Margin of bar
     */
    private int mBarMarginBottom;
    /**
     * @{#ViewGroup.LayoutParams} for @{#mBarView}
     */
    private LayoutParams mBarParams;
    /**
     * A view placed at the most bottom of this view.
     * This view is transparent and used to received event from user.
     */
    private View mBottomLineView;
    /**
     * For @{#mBottomLineView}
     */
    private LayoutParams mBottomLineParams;
    /**
     * Using to get the default width of this view.
     */
    private BarWidthPolicy mWidthPolicy=new BarWidthPolicy() {
        @Override
        public int getBarWidth(int screenWidth) {
            //Default policy
            return screenWidth/3;
        }
    };
    private WindowManager mWm;
    /**
     * Display metrics for current window
     */
    private final DisplayMetrics mDP=new DisplayMetrics();
    /**
     * Detector for listening drag and pull.
     */
    private GestureDetector mDetector;
    /**
     * Timer for scheduling job to close and hide the bottom bar
     */
    private Timer mHideTimer;
    /**
     * Timer for sending message to tell handler that
     * the bottom bar has been pulled to top for a while.
     */
    private Timer mPullTopLongCounter;
    private final BarStatusCallback mCallback=new BarStatusCallbackImp();
    private final BarStatusHandler mHandler=new BarStatusHandler(mCallback);
    /**
     * Used for expanding or hiding bottom bar.
     */
    private ValueAnimator mBarStatusAnimator;
    /**
     * Animator for restoring bottom bar to its origin position.
     */
    private ValueAnimator mBarRestoreAnimator;
    private final AccelerateDecelerateInterpolator mBarStatusInterpolator=new AccelerateDecelerateInterpolator();
    private int mBarOffset=0;
    private AbstractBottomBarListener mAbstractBottomBarListener;
    /**
     * When gesture detector detects fling velocity which is faster than this value,
     * invoke pull to top.
     */
    private int mAllowFlingVelocity=50;
    /**
     * Indicates whether the bottom bar is expanded.
     */
    private volatile boolean isBottomBarExpanded=true;
    /**
     * One move, set this value to true and invoke onPulling
     */
    private boolean mStartPulling=false;
    /**
     * Whether the bottom bar has already reached the top.
     */
    private boolean mAlreadyTop=false;
    /**
     * Whether pull long has been called, since pull top and pull top long
     * can only be called once totally
     */
    private boolean mLongPullTriggered=false;

    public BottomPullBar(Context context) {
        this(context,null);
    }

    public BottomPullBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public BottomPullBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,-1);
    }

    public BottomPullBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initComponent();
        initThisLayout();
        init();
    }

    /**
     * Set up the bottom bar
     * and bottom line.
     */
    private void init(){
        //Get default height
        mBarHeight=dp2px(DEFAULT_BAR_HEIGHT_DP);
        mBarMarginBottom=dp2px(DEFAULT_MARGIN_BOTTOM_DP);
        mMaxPullOffset=dp2px(DEFAULT_BAR_PULL_MAX_OFFSET_DP);

        //setup layout params
        mBarView=new View(getContext());
        mBottomLineView=new View(getContext());
        mBarParams=new LayoutParams(getBarWidth(),mBarHeight);
        mBottomLineParams=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,10);
        mBarParams.setMargins(0,0,0,mBarMarginBottom);
        mBarParams.gravity= Gravity.CENTER;
        mBarView.setLayoutParams(mBarParams);
        mBottomLineView.setLayoutParams(mBottomLineParams);

        //setup view background
        mBottomLineView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        mBarView.setBackgroundResource(R.drawable.bg_bottom_pull_bar);

        //add views to layout
        addView(mBarView);
        addView(mBottomLineView);

        //schedule hide timer now
        scheduleBottomBarHideTimer();
    }

    /**
     * Init this LinearLayout
     */
    private void initThisLayout(){
        setOrientation(VERTICAL);
        setGravity(Gravity.BOTTOM|Gravity.CENTER);
        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(getBarWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        setBackground(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initComponent(){
        mWm=(WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mWm.getDefaultDisplay().getMetrics(mDP);
        mDetector=new GestureDetector(getContext(),this);
    }
    public void setBarWidthPolicy(BarWidthPolicy policy){
        if (policy==null){
            return;
        }
        this.mWidthPolicy=policy;
        ViewGroup.LayoutParams params=getLayoutParams();
        params.width=getBarWidth();
        invalidate();
    }
    public void setBarHeight(int height){
        this.mBarHeight=height;
        mBarParams.height=mBarHeight;
        mBarView.setLayoutParams(mBarParams);
        invalidate();
    }
    public int getBarWidth(){
        return mWidthPolicy.getBarWidth(mDP.widthPixels);
    }
    private void scheduleBottomBarHideTimer(){
        Log.v(TAG,"Schedule hide bar");
        if (mHideTimer!=null){
            mHideTimer.cancel();
        }
        mHideTimer=new Timer();
        mHideTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_HIDE_BAR);
            }
        },DEFAULT_BAR_HIDE_WAIT_TIME);
    }
    private void cancelScheduledBottomHideTimer(){
        Log.v(TAG,"Cancel hide bar");
        if (mHideTimer!=null){
            mHideTimer.cancel();
            mHideTimer=null;
        }
    }
    private void schedulePullTopLongTimer(){
        if (mPullTopLongCounter!=null){
            mPullTopLongCounter.cancel();
        }
        mPullTopLongCounter=new Timer();
        mPullTopLongCounter.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_PERFORMING_LONG_PULL_TOP);
            }
        },DEFAULT_BAR_PULL_TOP_LONG_TIME);
    }
    private void cancelScheduledPullTopLongTimer(){
        if (mPullTopLongCounter!=null){
            mPullTopLongCounter.cancel();
            mPullTopLongCounter=null;
        }
    }
    private void restoreBarToOrigin(){
        if (mBarRestoreAnimator!=null){
            mBarRestoreAnimator.cancel();
        }
        if (mBarOffset==0){
            return;
        }
        int duration=(int) (DEFAULT_BAR_RESTORE_MAX_DURATION*((float)mBarOffset/mMaxPullOffset));
        mBarRestoreAnimator=ValueAnimator.ofInt(mBarOffset,0);
        mBarRestoreAnimator.setDuration(duration);
        mBarRestoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBarOffset=(int) animation.getAnimatedValue();
                LayoutParams params=(LayoutParams) mBarView.getLayoutParams();
                params.setMargins(0,0,0,mBarMarginBottom+mBarOffset);
                mBarView.setLayoutParams(params);
            }
        });
        mBarRestoreAnimator.start();
    }
    private void expandContainer(){
        int bottomLineHeight=mBottomLineView.getMeasuredHeight();
        int bottomBarHeight=mBarView.getMeasuredHeight();
        int bottomBarMargin=mBarMarginBottom;
        int maxOffset=mMaxPullOffset;
        ViewGroup.LayoutParams params=getLayoutParams();
        params.height=bottomLineHeight+bottomBarHeight+bottomBarMargin+maxOffset;
        setLayoutParams(params);
    }
    private void closeContainer(){
        ViewGroup.LayoutParams params=getLayoutParams();
        params.height= ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(params);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event==null){
            return mDetector.onTouchEvent(event);
        }
        int action=event.getAction();
        int eventX=(int) event.getX();
        int eventY=(int) event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG,"Action down");
                //when touch down, expand the bar
                mAlreadyTop=false;
                mLongPullTriggered=false;
                expandContainer();
                cancelScheduledBottomHideTimer();
                mCallback.onExpandBar();
                return mDetector.onTouchEvent(event);
            case MotionEvent.ACTION_MOVE:
                Log.v(TAG,"Action move, [x,y]=["+eventX+","+eventY+"]");
                if (!mStartPulling){
                    mStartPulling=true;
                    if (mAbstractBottomBarListener !=null){
                        mAbstractBottomBarListener.onBarPulling();
                    }
                }
                return mDetector.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.v(TAG,"Action up/cancel");
                //schedule when released
                closeContainer();
                scheduleBottomBarHideTimer();
                restoreBarToOrigin();
                cancelScheduledPullTopLongTimer();
                if (mAbstractBottomBarListener !=null){
                    mAbstractBottomBarListener.onBarReleased();
                }
                if (mAlreadyTop && !mLongPullTriggered && mAbstractBottomBarListener!=null){
                    mAbstractBottomBarListener.onBarPullTop();
                }
                mStartPulling=false;
                return mDetector.onTouchEvent(event);
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.v(TAG,"On down");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.v(TAG,"On show press");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.v(TAG,"On single tap up");
        if (mAbstractBottomBarListener !=null){
            mAbstractBottomBarListener.onBarClick();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.v(TAG,"On Scrolling, distance y:"+distanceY);
        if (distanceY>=0 && mBarOffset>=mMaxPullOffset){
            return true;
        }
        if (distanceY<=0 && mBarOffset<=0){
            return true;
        }
        int distance=(int) distanceY;
        int shouldScroll;
        if (mBarOffset+distanceY>mMaxPullOffset){
            shouldScroll=mMaxPullOffset-mBarOffset;
        }else if (mBarOffset+distanceY<0){
            shouldScroll=-mBarOffset;
        }else{
            shouldScroll=distance;
        }
        mBarOffset+=shouldScroll;
        LayoutParams params=(LayoutParams) mBarView.getLayoutParams();
        params.setMargins(0,0,0,mBarMarginBottom+mBarOffset);
        mBarView.setLayoutParams(params);
        if (mBarOffset>=mMaxPullOffset){
            mAlreadyTop=true;
            schedulePullTopLongTimer();
        }else{
            if (mAlreadyTop && mAbstractBottomBarListener!=null){
                mAbstractBottomBarListener.onBarPullTop();
            }
            mAlreadyTop=false;
            mLongPullTriggered=false;
            cancelScheduledPullTopLongTimer();
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v(TAG,"On long press");
        if (mAbstractBottomBarListener !=null){
            mAbstractBottomBarListener.onBarLongPress();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
/*        if (velocityX>=mAllowFlingVelocity){
            if (mAbstractBottomBarListener !=null){
                mAbstractBottomBarListener.onBarPullTop();
            }
            return true;
        }*/
        return true;
    }

    private int dp2px(int dpVal){
        float scale=getContext().getResources().getDisplayMetrics().density;
        return (int) (dpVal*scale+0.5f);
    }

    private final class BarStatusCallbackImp implements BarStatusCallback{
        @Override
        public void onExpandBar() {
            if (mBarStatusAnimator!=null && mBarStatusAnimator.isRunning()){
                mBarStatusAnimator.cancel();
            }
            if (isBottomBarExpanded){
                return;
            }
            int currentWidth=mBarView.getWidth();
            mBarStatusAnimator=ValueAnimator.ofInt(currentWidth,getBarWidth());
            mBarStatusAnimator.setDuration(DEFAULT_BAR_EXPAND_HIDE_DURATION);
            mBarStatusAnimator.setInterpolator(mBarStatusInterpolator);
            mBarStatusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value=(int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams params= mBarView.getLayoutParams();
                    params.width=value;
                    mBarView.setLayoutParams(params);
                }
            });
            mBarStatusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mBarView.setVisibility(View.VISIBLE);
                    if (mAbstractBottomBarListener !=null){
                        mAbstractBottomBarListener.onBarExpanding();
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mAbstractBottomBarListener !=null){
                        mAbstractBottomBarListener.onBarExpanded();
                    }
                    isBottomBarExpanded=true;
                }
                @Override
                public void onAnimationCancel(Animator animation) { }
                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
            mBarStatusAnimator.start();
        }
        @Override
        public void onHideBar() {
            if (mBarStatusAnimator!=null && mBarStatusAnimator.isRunning()){
                mBarStatusAnimator.cancel();
            }
            if (!isBottomBarExpanded){
                return;
            }
            int currentWidth=mBarView.getWidth();
            mBarStatusAnimator=ValueAnimator.ofInt(currentWidth,0);
            mBarStatusAnimator.setDuration(DEFAULT_BAR_EXPAND_HIDE_DURATION);
            mBarStatusAnimator.setInterpolator(mBarStatusInterpolator);
            mBarStatusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value=(int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams params= mBarView.getLayoutParams();
                    params.width=value;
                    mBarView.setLayoutParams(params);
                }
            });
            mBarStatusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mAbstractBottomBarListener !=null){
                        mAbstractBottomBarListener.onBarHiding();
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBarView.setVisibility(View.GONE);
                    isBottomBarExpanded=false;
                    if (mAbstractBottomBarListener !=null){
                        mAbstractBottomBarListener.onBarHidden();
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) { }
                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
            mBarStatusAnimator.start();
        }
        @Override
        public void onPullTopLong() {
            mLongPullTriggered=true;
            if (mAbstractBottomBarListener !=null){
                mAbstractBottomBarListener.onBarPullTopLong();
            }
        }
    }

    /**
     * Interface callback for changing status
     * of bottom bar.
     */
    private interface BarStatusCallback{
        /**
         * When bottom bar needs to expand, call this method
         */
        void onExpandBar();

        /**
         * When bottom bar needs to hide, call this method
         */
        void onHideBar();

        /**
         * When bottom bar is pulled to top for a certain period of time
         * call this method
         */
        void onPullTopLong();
    }

    /**
     * Abstract class for monitoring bar status changes.
     * Sometimes users don't want to implement all the method
     * just because they don't need all of them, so we make this class
     * an abstract class instead of an interface.
     */
    public static abstract class AbstractBottomBarListener {
        /**
         * Invoked when bar is being pulled
         */
        public void onBarPulling(){}

        /**
         * Invoked when bar is pulled to top
         * May call more than once
         */
        public void onBarPullTop(){}

        /**
         * Invoked when bar is long clicked
         */
        public void onBarLongPress(){}

        /**
         * Invoked when bar is clicked
         */
        public void onBarClick(){}

        /**
         * Invoked when bar is pulled to top for a certain period of time.
         */
        public void onBarPullTopLong(){}

        /**
         * Invoked when bar is totally expanded.
         */
        public void onBarExpanded(){}

        /**
         * Invoked when bar is totally hidden.
         */
        public void onBarHidden(){}

        /**
         * Invoked when bar is expanding.
         */
        public void onBarExpanding(){}

        /**
         * Invoked when bar is hiding.
         */
        public void onBarHiding(){}

        /**
         * Invoked when bar is released from
         * being dragged.
         */
        public void onBarReleased(){}
    }
    public void setOnBottomBarListener(AbstractBottomBarListener listener){
        this.mAbstractBottomBarListener =listener;
    }

    /**
     * This handler is for handling task from
     * @{#Timer}.
     */
    private static class BarStatusHandler extends Handler{
        /**
         * Weak reference for avoiding memory leak.
         */
        private WeakReference<BarStatusCallback> mCallback;

        public BarStatusHandler(BarStatusCallback callback){
            this.mCallback=new WeakReference<>(callback);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            BarStatusCallback callback= mCallback.get();
            if (callback==null){
                return;
            }
            switch (msg.what){
                case MSG_EXPAND_BAR:
                    callback.onExpandBar();
                    break;
                case MSG_HIDE_BAR:
                    callback.onHideBar();
                    break;
                case MSG_PERFORMING_LONG_PULL_TOP:
                    callback.onPullTopLong();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
