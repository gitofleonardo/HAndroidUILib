package cn.huangchengxi.uilib.container.nested;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;

/**
 * @author huangchengxi
 */
public class NestedRefreshLayout extends ViewGroup implements NestedScrollingParent3 {
    private static final String TAG="NestedRefreshLayout";
    private NestedScrollingParentHelper mParentHelper=new NestedScrollingParentHelper(this);
    private OverScroller mOverScroller=new OverScroller(getContext());

    public NestedRefreshLayout(Context context) {
        this(context,null);
    }

    public NestedRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public NestedRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,-1);
    }

    public NestedRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init(){
        setNestedScrollingEnabled(true);
    }

    @Override
    public void addView(View child) {
        if (getChildCount()>3){
            throw new IllegalArgumentException("Cannot add more than three views.");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount()>3){
            throw new IllegalArgumentException("Cannot add more than three views.");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (getChildCount()>3){
            throw new IllegalArgumentException("Cannot add more than three views.");
        }
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (getChildCount()>3){
            throw new IllegalArgumentException("Cannot add more than three views.");
        }
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount()>3){
            throw new IllegalArgumentException("Cannot add more than three views.");
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {

    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return false;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {

    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {

    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }
}
