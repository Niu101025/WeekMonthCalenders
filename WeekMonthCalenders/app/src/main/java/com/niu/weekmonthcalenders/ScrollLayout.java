package com.niu.weekmonthcalenders;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.niu.weekmonthcalenders.app.CalenderApp;
import com.niu.weekmonthcalenders.view.MonthView;
import com.niu.weekmonthcalenders.view.WeekView;
import com.niu.weekmonthcalenders.utils.SysUtil;


public class ScrollLayout extends FrameLayout implements MonthView.OnLineCountChangeListener,
        MonthView.OnLineChooseListener, MonthView.OnMonthViewChangeListener,
        WeekView.OnWeekViewChangeListener, WeekView.OnWeekDateClick, MonthView.OnMonthDateClick, WeekView.OnLineChooseListener1 {

    private ViewDragHelper mViewDragHelper;
    private MonthView mMonthView;
    private WeekView mWeekView;
    private LinearLayout mainLayout;
    private LinearLayout contentLayout;
    private ListView listView;
    //记录month calendar 行数和选择的哪一行的数字的变化
    private int line;
    private int lineCount;
    //初始的Y坐标
    private int orignalY;
    //滑动的过程中记录顶部坐标
    private int layoutTop;
    private int dragRang;
    private int marginTop;
    private int mTouchSlop;
    private int lastX;
    private int lastY;
    public static String selectDate = "";
    private MoveTopCallBack moveTopCallBack;
    private MoveUpCallBack moveUpCallBack;

    private int scrollPaddingTop; // scrollview的顶部内边距
    private int scrollPaddingLeft;// scrollview的左侧内边距
    private int[] scrollLoaction = new int[2]; // scrollview在窗口中的位置
    private final static int UPGLIDE = 0;
    private final static int DOWNGLIDE = 1;
    private int glideState;
    private int downY = 0;
    private int moveY = 0;
    private int width;
    private int height;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                //System.out.println("actiondown" + ev.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = (int) ev.getY();
                //System.out.println("move" + moveY + "down" + downY);
                if ((moveY - downY) >= 0) {
                    //System.out.println("'''''''''DOWNGLIDE'''''''''''");
                    glideState = DOWNGLIDE;
                } else {
                    //System.out.println("'''''''''UPGLIDE'''''''''''");
                    glideState = UPGLIDE;
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        width = listView.getWidth();
        height = listView.getHeight();
        int[] location = new int[2];
        listView.getLocationOnScreen(location);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getRawX();
                lastY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 该事件的xy是以scrollview的左上角为00点而不是以窗口为00点
                int x = (int) ev.getRawX() + scrollLoaction[0];
                int y = (int) ev.getRawY() + scrollLoaction[1];
                //如果触摸被按下的时间是在listview中
                if (!(lastX >= location[0] + scrollPaddingLeft
                        && lastX <= location[0] + scrollPaddingLeft + width
                        && lastY >= location[1] + scrollPaddingTop
                        && lastY <= location[1] + scrollPaddingTop + height)) {
                    if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                        if ((int) Math.abs(lastX - ev.getRawX()) >= mTouchSlop) {
                            return false;
                        }
                    }
                }
                // 在listview的位置之内则可以滑动
                if (x >= location[0] + scrollPaddingLeft
                        && x <= location[0] + scrollPaddingLeft + width
                        && y >= location[1] + scrollPaddingTop
                        && y <= location[1] + scrollPaddingTop + height) {
                    if (mWeekView.getVisibility() == VISIBLE) {
                        if (((listView.getFirstVisiblePosition() == 0) && (glideState == DOWNGLIDE))) {
                            //如果想要修改刷新状态的话，可以修成ture
                            if (Math.abs(lastX - ev.getRawX()) < 3 && Math.abs(lastY - ev.getRawY()) < 3) {
                                return false;
                            } else {
                                return true;
                            }
                        } else {
                            return false;
                        }

                    }
                    if (Math.abs(lastX - ev.getRawX()) < 3 && Math.abs(lastY - ev.getRawY()) < 3) {
                        return false;
                    } else {
                        return true;
                    }
                }
                break;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }


    public ScrollLayout(Context context) {
        this(context, null);
    }

    public ScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, getResources().getDisplayMetrics());
        mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mainLayout;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {

                if (top >= orignalY) {
                    return orignalY;
                } else {
                    if (contentLayout.getMeasuredHeight() <= mMonthView.getHeight() * (lineCount - 1) / lineCount) {
                        return Math.max(top, -mMonthView.getHeight() * (lineCount - 1) / lineCount);
                    } else {
                        return Math.max(top, -contentLayout.getMeasuredHeight());
                    }
                }
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                //不要让顶部坐标在weekview的上方，这样会导致 mainlayout 被weekview遮挡
                if (dy <= 0) {
                    if (moveTopCallBack != null) {
                        moveTopCallBack.moveTopCallback(mMonthView.getHeight(), -top);
                    }
                } else if (dy > 0) {
                    if (moveUpCallBack != null) {
                        moveUpCallBack.moveUpCallback(mMonthView.getHeight(), -top);
                    }
                }
                layoutTop = top <= -mMonthView.getHeight() * (lineCount - 1) / lineCount ? -mMonthView.getHeight() * (lineCount - 1) / lineCount : top;
                if (top <= -mMonthView.getHeight() * line / lineCount && dy < 0) {
                    mWeekView.setVisibility(View.VISIBLE);

                } else if (top >= -mMonthView.getHeight() * line / lineCount && dy > 0) {
                    mWeekView.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if (releasedChild.getTop() > -mMonthView.getHeight() * (lineCount - 1) / lineCount && yvel >= 0) {
                    mViewDragHelper.settleCapturedViewAt(0, orignalY);
                    invalidate();
                } else if (releasedChild.getTop() > -mMonthView.getHeight() * (lineCount - 1) / lineCount && yvel < 0) {
                    mViewDragHelper.settleCapturedViewAt(0, -mMonthView.getHeight() * (lineCount - 1) / lineCount);
                    invalidate();
                }
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return mMonthView.getHeight();
            }
        });
    }

    public void setMoveTopCallBack(MoveTopCallBack moveTopCallBack) {
        this.moveTopCallBack = moveTopCallBack;
    }

    public void setMoveUpCallBack(MoveUpCallBack moveUpCallBack) {
        this.moveUpCallBack = moveUpCallBack;
    }

    @Override
    public void onLineChange(int line) {
        this.line = line;
        mWeekView.setLine(line);
    }

    @Override
    public void onLineCountChange(int lineCount) {
        this.lineCount = lineCount;
        if (lineCount == 6) {
            mWeekView.setCount(lineCount);
        }
    }

    @Override
    public void onMonthViewChange(boolean isforward) {
        if (isforward) {
            mWeekView.moveForwad();
        } else {
            mWeekView.moveBack();
        }
    }

    @Override
    public void onMonthDateClick(int x, int y) {
        mWeekView.changeChooseDate(x, y - (mMonthView.getHeight() * (line) / lineCount));
    }


    @Override
    public void onWeekViewChange(boolean isForward) {
        if (isForward) {
            mMonthView.moveForwad();
        } else {
            mMonthView.moveBack();
        }
    }

    public void onWeekViewChange(boolean isForward, boolean hasSet) {
        if (isForward) {
            mMonthView.moveForwad(true);
        } else {
            mMonthView.moveBack(true);
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            postInvalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mMonthView = (MonthView) findViewById(R.id.month_calendar);
        mMonthView.setOnLineChooseListener(this);
        mMonthView.setOnLineCountChangeListener(this);
        mMonthView.setOnMonthDateClickListener(this);
        mMonthView.setOnMonthViewChangeListener(this);
        mWeekView = (WeekView) findViewById(R.id.week_calendar);
        mWeekView.setOnWeekViewChangeListener(this);
        mWeekView.setOnWeekClickListener(this);
        mWeekView.setOnLineChooseListener1(this);
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        listView = (ListView) findViewById(R.id.listview);
        orignalY = mMonthView.getTop();
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels
                - 80 * getResources().getDisplayMetrics().density
                - SysUtil.getStatusHeight(CalenderApp.mContext)
                - mWeekView.getMeasuredHeight());
        listView.setLayoutParams(layoutParams);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWeekView.layout(0, 0, mWeekView.getMeasuredWidth(), mWeekView.getMeasuredHeight());
        mainLayout.layout(0, layoutTop, mainLayout.getMeasuredWidth(), mainLayout.getMeasuredHeight());
    }

  /*  @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((int) Math.abs(lastX - ev.getX()) >= mTouchSlop) {
                    return false;
                }
                break;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }


    //重写 onMeasure 支持 wrap_content
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //计算所有childview的宽高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //计算warp_content的时候的高度
        int wrapHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            wrapHeight += childHeight;
        }
        setMeasuredDimension(widthSize, heightMode == MeasureSpec.EXACTLY ? heightSize : wrapHeight);

    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight(), lp.width);

        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec, lp.leftMargin + lp.rightMargin + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public void onWeekDateClick(int x, int y, int count, int num) {
        mMonthView.changeChooseDate(x, y + (mMonthView.getHeight() * (num) / lineCount));
    }

    @Override
    public void onLineChange1(int line) {
        this.line = line;
    }


    public interface MoveTopCallBack {
        public void moveTopCallback(int count, int index);
    }

    public interface MoveUpCallBack {
        public void moveUpCallback(int count, int index);
    }
}
