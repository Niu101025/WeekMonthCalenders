package com.niu.weekmonthcalenders.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.niu.weekmonthcalenders.ScrollLayout;
import com.niu.weekmonthcalenders.calenders.DPCManager;
import com.niu.weekmonthcalenders.calenders.DPCalendar;
import com.niu.weekmonthcalenders.calenders.DPTManager;
import com.niu.weekmonthcalenders.beans.DPInfo;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WeekView extends View {

    //保存日历每一天的位置
    private final Region[][] monthRegionsFour = new Region[4][7];
    private final Region[][] monthRegionsFive = new Region[5][7];
    private final Region[][] monthRegionsSix = new Region[6][7];
    //保存日历的基本信息
    private final DPInfo[][] infoFour = new DPInfo[4][7];
    private final DPInfo[][] infoFive = new DPInfo[5][7];
    private final DPInfo[][] infoSix = new DPInfo[6][7];
    //被选择的区域
    //日历和日历主题管理器
    private DPCManager mCManager = DPCManager.getInstance();
    private DPTManager mTManager = DPTManager.getInstance();
    //画笔的属性
    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG
            | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
    protected Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG
            | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
    private Scroller mScroller;
    //private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private OnWeekViewChangeListener onWeekViewChangeListener;
    //和月份可以实现联动
    private MonthView.OnDatePickedListener onDatePickedListener;
    private OnLineChooseListener1 onLineChooseListener;
    private OnWeekDateClick onWeekClick;
    private ScaleAnimationListener scaleAnimationListener;

    private SlideMode mSlideMode;
    private int mNowYear, mNowMonth;
    private int circleRadius;
    private int indexYear, indexMonth;
    private int centerYear, centerMonth;
    private int leftYear, leftMonth;
    private int rightYear, rightMonth;
    private int width, height;
    private int lastPointX;
    private int lastMoveX;
    private int criticalWidth;

    private float sizeTextGregorian;
    private int num = 5;
    private int count = 5;
    //被选择0;的那个的位置,在monthView中被选择的话 也应该响应事件
    public static int mSelectX = 0;
    public static int mSelectY = 0;
    // 点击选中的day
    private boolean isNewEvent, isTodayDisplay = true;
    private Map<String, BGCircle> cirApr = new HashMap<>();

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scaleAnimationListener = new ScaleAnimationListener();
        }
        mNowYear = Calendar.getInstance().get(Calendar.YEAR);
        mNowMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        mScroller = new Scroller(context);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mSlideMode = null;
                isNewEvent = true;
                lastPointX = (int) event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isNewEvent) {
                    if (Math.abs(lastPointX - event.getX()) > 100) {
                        mSlideMode = SlideMode.HOR;
                        isNewEvent = false;
                    }
                }
                if (mSlideMode == SlideMode.HOR) {
                    int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
                    if (centerYear > mNowYear || (centerYear == mNowYear && centerMonth > mNowMonth)
                            || (centerYear == mNowYear && centerMonth == mNowMonth && num >= 1)) {
                        smoothScrollTo(totalMoveX, indexYear * height);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mSlideMode == SlideMode.VER) {
                } else if (mSlideMode == SlideMode.HOR) {
                    DPInfo[][] infos = DPCManager.getInstance().obtainDPInfo(centerYear, centerMonth);
                    if (TextUtils.isEmpty(infos[4][0].strG)) {
                        count = 4;
                    } else if (TextUtils.isEmpty(infos[5][0].strG)) {
                        count = 5;
                    } else {
                        count = 6;
                    }
                    if (Math.abs(lastPointX - event.getX()) > 25) {
                        if (lastPointX > event.getX()
                                && Math.abs(lastPointX - event.getX()) >= criticalWidth) {
                            indexMonth++;
                            //如果这个月的最后一天是周六，那么下一个月的第一天是周日

                            if (num == count - 1) {
                                movtToNext();
                            } else if (num == count - 2 && getSelectPosition(mSelectX, mSelectY) > DPCalendar.getLastDayWeek(centerYear, centerMonth) - 1) {
                                movtToNextMonthByWeek();
                            } else {
                                //在这里设置清除当前被选中的状态，然后让下周所对应的日期被选中
                                num++;
                            }

                        } else if (lastPointX < event.getX()
                                && Math.abs(lastPointX - event.getX()) >= criticalWidth) {
                            if (centerYear > mNowYear || (centerYear == mNowYear && centerMonth > mNowMonth)
                                    || (centerYear == mNowYear && centerMonth == mNowMonth && num >= 1)) {
                                indexMonth--;
                                //判断当前被选中的那个日期，是否在本月的范围内
                                if (num == 1 && getSelectPosition(mSelectX, mSelectY) < DPCalendar.getFirstDayWeek(centerYear, centerMonth) - 1) {
                                    movePreMonthByWeek();
                                } else if (num <= 0) {
                                    movePreMonth();
                                } else {
                                    //往左滑动，在这里设置清除当前被选中的状态，然后让下周所对应的日期被选中
                                    num--;
                                }
                            }
                        }
                        computeDate();
                        smoothScrollTo(width * indexMonth, 0);
                        defineRegion1(mSelectX, mSelectY);
                        lastMoveX = width * indexMonth;
                        if (onLineChooseListener != null) {
                            onLineChooseListener.onLineChange1(num);
                        }
                    } else {

                        defineRegion((int) event.getX(), (int) event.getY());
                    }
                } else {
                    defineRegion((int) event.getX(), (int) event.getY());
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void movtToNext() {
        centerMonth = (centerMonth + 1) % 13;
        if (centerMonth == 0) {
            centerMonth = 1;
            centerYear++;
        }
        if (null != onWeekViewChangeListener) {
            onWeekViewChangeListener.onWeekViewChange(true);
        }
        //表示下次从第几行开始
        if (DPCalendar.getFirstDayWeek(centerYear, centerMonth) == 1) {
            num = 0;
        } else {
            num = 1;
        }
    }

    private void movtToNextMonthByWeek() {
        centerMonth = (centerMonth + 1) % 13;
        if (centerMonth == 0) {
            centerMonth = 1;
            centerYear++;
        }
        if (null != onWeekViewChangeListener) {
            onWeekViewChangeListener.onWeekViewChange(true);
        }
        num = 0;
    }

    private void movePreMonth() {
        centerMonth = (centerMonth - 1) % 12;
        if (centerMonth == 0) {
            centerMonth = 12;
            centerYear--;
        }
        if (null != onWeekViewChangeListener) {
            onWeekViewChangeListener.onWeekViewChange(false);
        }
        DPInfo[][] infos1 = DPCManager.getInstance().obtainDPInfo(centerYear, centerMonth);
        if (DPCalendar.getLastDayWeek(centerYear, centerMonth) == 7) {
            if (TextUtils.isEmpty(infos1[4][0].strG)) {
                num = 3;
            } else if (TextUtils.isEmpty(infos1[5][0].strG)) {
                num = 4;
            } else {
                num = 5;
            }
        } else {
            if (TextUtils.isEmpty(infos1[4][0].strG)) {
                num = 2;
            } else if (TextUtils.isEmpty(infos1[5][0].strG)) {
                num = 3;
            } else {
                num = 4;
            }
        }
    }

    private void movePreMonthByWeek() {
        centerMonth = (centerMonth - 1) % 12;
        if (centerMonth == 0) {
            centerMonth = 12;
            centerYear--;
        }
        if (null != onWeekViewChangeListener) {
            onWeekViewChangeListener.onWeekViewChange(false);
        }
        DPInfo[][] infos1 = DPCManager.getInstance().obtainDPInfo(centerYear, centerMonth);
        if (TextUtils.isEmpty(infos1[4][0].strG)) {
            num = 3;
        } else if (TextUtils.isEmpty(infos1[5][0].strG)) {
            num = 4;
        } else {
            num = 5;
        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(measureWidth, (int) (measureWidth * 5F / 7F) / count);
    }

    public void moveForwad() {
        indexMonth++;
        centerMonth = (centerMonth + 1) % 13;
        if (centerMonth == 0) {
            centerMonth = 1;
            centerYear++;
        }
        computeDate();
        smoothScrollTo(width * indexMonth, indexYear * height);
        lastMoveX = width * indexMonth;
        requestLayout();
    }

    //滑动back
    public void moveBack() {
        indexMonth--;
        centerMonth = (centerMonth - 1) % 12;
        if (centerMonth == 0) {
            centerMonth = 12;
            centerYear--;
        }
        computeDate();
        smoothScrollTo(width * indexMonth, indexYear * height);
        lastMoveX = width * indexMonth;
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;

        criticalWidth = (int) (1F / 5F * width);


        int cellW = (int) (w / 7F);
        int cellH4 = (int) (h / 4F);
        int cellH5 = (int) (h / 5F);
        int cellH6 = (int) (h / 6F);

        circleRadius = cellW * 88 / 150;
        sizeTextGregorian = width / 27F;
        mPaint.setTextSize(sizeTextGregorian);

        //设置每个日期所对应的位置
        for (int i = 0; i < monthRegionsFour.length; i++) {
            for (int j = 0; j < monthRegionsFour[i].length; j++) {
                Region region = new Region();
                region.set(j * cellW, 0 * cellH4, cellW + (j * cellW),
                        cellW + (0 * cellH4));
                monthRegionsFour[i][j] = region;
            }
        }
        for (int i = 0; i < monthRegionsFive.length; i++) {
            for (int j = 0; j < monthRegionsFive[i].length; j++) {
                Region region = new Region();
                region.set(j * cellW, 0 * cellH5, cellW + (j * cellW),
                        cellW + (0 * cellH5));
                monthRegionsFive[i][j] = region;
            }
        }
        for (int i = 0; i < monthRegionsSix.length; i++) {
            for (int j = 0; j < monthRegionsSix[i].length; j++) {
                Region region = new Region();
                region.set(j * cellW, 0, cellW + (j * cellW),
                        cellW + (0 * cellH6));
                monthRegionsSix[i][j] = region;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //每滑动一次，调用一次ondraw()
        //设置日历的背景
        canvas.drawColor(mTManager.colorBG());
        drawBGCircle(canvas);
        //为了流畅，绘制了三组view
        DPInfo[][] infoLeft, infoCenter, infoRifgt;
        Region[][] tmpleft, tmpCenter, tmpRight;
        int leftCount;
        infoCenter = mCManager.obtainDPInfo(centerYear, centerMonth);
        if (TextUtils.isEmpty(infoCenter[4][0].strG)) {
            count = 4;
        } else if (TextUtils.isEmpty(infoCenter[5][0].strG)) {
            count = 5;
        } else {
            count = 6;
        }
        if (num <= 0) {
            infoLeft = mCManager.obtainDPInfo(leftYear, leftMonth);
            if (TextUtils.isEmpty(infoLeft[4][0].strG)) {
                tmpleft = monthRegionsFour;
                leftCount = 4;
                arrayClear(infoFour);
            } else if (TextUtils.isEmpty(infoLeft[5][0].strG)) {
                tmpleft = monthRegionsFive;
                leftCount = 5;
                arrayClear(infoFive);
            } else {
                tmpleft = monthRegionsSix;
                leftCount = 6;
                arrayClear(infoSix);
            }
            if (DPCalendar.getLastDayWeek(leftCount, leftCount) == 7) {
                DPInfo[] leftLine = mCManager.obtainWeekDPInfo(leftYear, leftMonth, leftCount - 1);
                draw(canvas, width * (indexMonth - 1), 0, tmpleft[leftCount - 1], leftLine);
            } else {
                DPInfo[] leftLine = mCManager.obtainWeekDPInfo(leftYear, leftMonth, leftCount - 2);
                draw(canvas, width * (indexMonth - 1), 0, tmpleft[leftCount - 2], leftLine);
            }

            infoCenter = mCManager.obtainDPInfo(centerYear, centerMonth);
            if (TextUtils.isEmpty(infoCenter[4][0].strG)) {
                tmpCenter = monthRegionsFour;
                arrayClear(infoFour);
            } else if (TextUtils.isEmpty(infoCenter[5][0].strG)) {
                tmpCenter = monthRegionsFive;
                arrayClear(infoFive);
            } else {
                tmpCenter = monthRegionsSix;
                arrayClear(infoSix);
            }
            DPInfo[] RightLine1 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num);
            draw(canvas, width * (indexMonth), 0, tmpCenter[num], RightLine1);
            DPInfo[] RightLine2 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num + 1);
            draw(canvas, width * (indexMonth + 1), 0, tmpCenter[num], RightLine2);
        } else if (num < count - 1) {
            infoCenter = mCManager.obtainDPInfo(centerYear, centerMonth);
            if (TextUtils.isEmpty(infoCenter[4][0].strG)) {
                tmpCenter = monthRegionsFour;
                arrayClear(infoFour);

            } else if (TextUtils.isEmpty(infoCenter[5][0].strG)) {
                tmpCenter = monthRegionsFive;
                arrayClear(infoFive);
            } else {
                tmpCenter = monthRegionsSix;
                arrayClear(infoSix);
            }
            DPInfo[] RightLine1 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num);
            draw(canvas, width * (indexMonth), 0, tmpCenter[num], RightLine1);
            DPInfo[] RightLine2 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num + 1);
            draw(canvas, width * (indexMonth + 1), 0, tmpCenter[num], RightLine2);
            DPInfo[] RightLine3 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num - 1);
            draw(canvas, width * (indexMonth - 1), 0, tmpCenter[num], RightLine3);
        } else {
            infoCenter = mCManager.obtainDPInfo(centerYear, centerMonth);
            if (TextUtils.isEmpty(infoCenter[4][0].strG)) {
                tmpCenter = monthRegionsFour;
                arrayClear(infoFour);
            } else if (TextUtils.isEmpty(infoCenter[5][0].strG)) {
                tmpCenter = monthRegionsFive;
                arrayClear(infoFive);
            } else {
                tmpCenter = monthRegionsSix;
                arrayClear(infoSix);
            }
            DPInfo[] RightLine1 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, tmpCenter.length - 2);
            draw(canvas, width * (indexMonth - 1), 0, tmpCenter[tmpCenter.length - 2], RightLine1);
            DPInfo[] RightLine2 = mCManager.obtainWeekDPInfo(centerYear, centerMonth, tmpCenter.length - 1);
            draw(canvas, width * (indexMonth), 0, tmpCenter[tmpCenter.length - 1], RightLine2);

            infoRifgt = mCManager.obtainDPInfo(rightYear, rightMonth);
            if (TextUtils.isEmpty(infoRifgt[4][0].strG)) {
                tmpRight = monthRegionsFour;
                arrayClear(infoFour);
            } else if (TextUtils.isEmpty(infoRifgt[5][0].strG)) {
                tmpRight = monthRegionsFive;
                arrayClear(infoFive);
            } else {
                tmpRight = monthRegionsSix;
                arrayClear(infoSix);
            }
            DPInfo[] RightLine;
            if (DPCalendar.getLastDayWeek(centerYear, centerMonth) == 7) {
                RightLine = mCManager.obtainWeekDPInfo(rightYear, rightMonth, 0);
                draw(canvas, width * (indexMonth + 1), 0, tmpRight[0], RightLine);
            } else {
                RightLine = mCManager.obtainWeekDPInfo(rightYear, rightMonth, 1);
                draw(canvas, width * (indexMonth + 1), 0, tmpRight[1], RightLine);
            }
        }

    }

    private void drawBGCircle(Canvas canvas) {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            for (String s : cirDpr.keySet()) {
                BGCircle circle = cirDpr.get(s);
                drawBGCircle(canvas, circle);
            }
        }*/
        for (String s : cirApr.keySet()) {
            BGCircle circle = cirApr.get(s);
            drawBGCircle(canvas, circle);
        }
    }

    private void drawBGCircle(Canvas canvas, BGCircle circle) {
        canvas.save();
        canvas.translate(circle.getX() - circle.getRadius() / 2, circle.getY()
                - circle.getRadius() / 2);
        circle.getShape().getShape()
                .resize(circle.getRadius(), circle.getRadius());
        circle.getShape().draw(canvas);
        canvas.restore();
    }

    private void draw(Canvas canvas, int x, int y, Region[] tmp, DPInfo[] leftLine) {
        canvas.save();
        canvas.translate(x, y);

        for (int j = 0; j < tmp.length; j++) {
            draw(canvas, tmp[j].getBounds(), leftLine[j]);
        }

        canvas.restore();
    }

    public void setLine(int num) {
        this.num = num;
    }

    public void setCount(int count) {
        this.count = count;
        requestLayout();
    }

    private void draw(Canvas canvas, Rect rect, DPInfo info) {
        drawBG(canvas, rect, info);
        drawGregorian(canvas, rect, info.strG, info.isChoosed, true, info.isToday);
    }

    private void drawBG(Canvas canvas, Rect rect, DPInfo info) {
        if (info.isToday && isTodayDisplay) {
            drawBGToday(canvas, rect);
        }
    }

    // 因为今天的样式需要自定义所以 重新换了Paint
    private void drawBGToday(Canvas canvas, Rect rect) {
        /*todayPaint.setColor(mTManager.colorToday());
        todayPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(rect.centerX(), rect.centerY() + rect.height() / 2, 12,
                todayPaint);*/
    }

    private void drawGregorian(Canvas canvas, Rect rect, String str,
                               boolean isChoosed, boolean isSelect, boolean isToday) {
        mPaint.setTextSize(sizeTextGregorian);
        if (isChoosed) {
            mPaint.setColor(mTManager.colorChooseText());
        } else if (isSelect) {
            mPaint.setColor(mTManager.colorWeekend());
        } else {
            mPaint.setColor(mTManager.colorG());
        }
        float y = rect.centerY() + (mPaint.descent());
        if (isToday) {
            canvas.drawText("今天", rect.centerX(), y, mPaint);
        } else {
            canvas.drawText(str, rect.centerX(), y, mPaint);
        }
    }

    public void setOnWeekViewChangeListener(OnWeekViewChangeListener onWeekViewChangeListener) {
        this.onWeekViewChangeListener = onWeekViewChangeListener;
    }

    public void setOnWeekClickListener(OnWeekDateClick onWeekClick) {
        this.onWeekClick = onWeekClick;
    }


    public void setOnDatePickedListener(
            MonthView.OnDatePickedListener onDatePickedListener) {
        this.onDatePickedListener = onDatePickedListener;
    }


    public void setDate(int year, int month) {
        centerYear = year;
        centerMonth = month;
        indexYear = 0;
        indexMonth = 0;
        computeDate();
        requestLayout();
        invalidate();
    }


    public void setTodayDisplay(boolean isTodayDisplay) {
        this.isTodayDisplay = isTodayDisplay;
    }

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
                dy, 500);
        invalidate();
    }

    private BGCircle createCircle(float x, float y) {
        OvalShape circle = new OvalShape();
        circle.resize(0, 0);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        BGCircle circle1 = new BGCircle(drawable);
        circle1.setX(x);
        circle1.setY(y);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            circle1.setRadius(circleRadius);
        }
        drawable.getPaint().setColor(mTManager.colorBGCircle());
        return circle1;
    }


    private void arrayClear(DPInfo[][] info) {
        for (DPInfo[] anInfo : info) {
            Arrays.fill(anInfo, null);
        }
    }


    /**
     * 判断哪个被选中了
     *
     * @param x
     * @param y
     */
    @SuppressLint("NewApi")
    public void defineRegion(final int x, final int y) {
        if (num == 0 && getSelectPosition(x, y) < DPCalendar.getFirstDayWeek(centerYear, centerMonth) - 1) {
            movePreMonthByWeek();
        } else if (num == count - 1 && getSelectPosition(x, y) > DPCalendar.getLastDayWeek(centerYear, centerMonth) - 1) {
            movtToNextMonthByWeek();
        }
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        DPInfo dpInfo;
        final Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
        if (num >= tmp.length) {
            num = tmp.length - 1;
        }

        for (int j = 0; j < tmp[num].length; j++) {
            Region region = tmp[0][j];

            if (region.contains(x, y)) {
                if (onLineChooseListener != null) {
                    onLineChooseListener.onLineChange1(num);
                }
                cirApr.clear();
                final String date = centerYear + "." + centerMonth + "." +
                        mCManager.obtainWeekDPInfo(centerYear, centerMonth, num)[j].strG;
                ScrollLayout.selectDate = date;
                dpInfo = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num)[j];
                BGCircle circle = createCircle(
                        region.getBounds().centerX() + indexMonth * width,
                        region.getBounds().centerY() + indexYear * height);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    ValueAnimator animScale1 =
                            ObjectAnimator.ofInt(circle, "radius", 0, circleRadius);
                    animScale1.setDuration(10);
                    animScale1.addUpdateListener(scaleAnimationListener);
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.playSequentially(animScale1);
                    final DPInfo finalDpInfo = dpInfo;
                    animSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (null != onDatePickedListener) {
                                onDatePickedListener.onDatePicked(finalDpInfo);
                            }
                            if (null != onWeekClick) {
                                onWeekClick.onWeekDateClick(x, y, tmp.length, num);
                            }
                            mSelectX = x;
                            mSelectY = y;
                        }
                    });
                    animSet.start();
                }
                cirApr.put(date, circle);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    invalidate();
                    if (null != onDatePickedListener) {
                        onDatePickedListener.onDatePicked(dpInfo);
                    }
                    if (null != onWeekClick) {
                        onWeekClick.onWeekDateClick(x, y, tmp.length, num);
                    }
                }
            }

        }
    }

    /**
     * 判断哪个被选中了
     *
     * @param x
     * @param y
     */
    @SuppressLint("NewApi")
    public void defineRegion1(final int x, final int y) {
        /*if (num == 0 && getSelectPosition(x, y) < DPCalendar.getFirstDayWeek(centerYear, centerMonth) - 1) {
            movePreMonthByWeek();
        } else if (num == count - 1 && getSelectPosition(x, y) > DPCalendar.getLastDayWeek(centerYear, centerMonth) - 1) {
            movtToNextMonthByWeek();
        }*/
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        DPInfo dpInfo;
        final Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
        if (num >= tmp.length) {
            num = tmp.length - 1;
        }

        for (int j = 0; j < tmp[num].length; j++) {
            Region region = tmp[0][j];

            if (region.contains(x, y)) {
                if (onLineChooseListener != null) {
                    onLineChooseListener.onLineChange1(num);
                }
                cirApr.clear();
                final String date = centerYear + "." + centerMonth + "." +
                        mCManager.obtainWeekDPInfo(centerYear, centerMonth, num)[j].strG;
                ScrollLayout.selectDate = date;
                dpInfo = mCManager.obtainWeekDPInfo(centerYear, centerMonth, num)[j];
                BGCircle circle = createCircle(
                        region.getBounds().centerX() + indexMonth * width,
                        region.getBounds().centerY() + indexYear * height);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    ValueAnimator animScale1 =
                            ObjectAnimator.ofInt(circle, "radius", 0, circleRadius);
                    animScale1.setDuration(10);
                    animScale1.addUpdateListener(scaleAnimationListener);
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.playSequentially(animScale1);
                    final DPInfo finalDpInfo = dpInfo;
                    animSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (null != onDatePickedListener) {
                                onDatePickedListener.onDatePicked(finalDpInfo);
                            }
                            if (null != onWeekClick) {
                                onWeekClick.onWeekDateClick(x, y, tmp.length, num);
                            }
                            mSelectX = x;
                            mSelectY = y;
                        }
                    });
                    animSet.start();
                }
                cirApr.put(date, circle);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    invalidate();
                    if (null != onDatePickedListener) {
                        onDatePickedListener.onDatePicked(dpInfo);
                    }
                    if (null != onWeekClick) {
                        onWeekClick.onWeekDateClick(x, y, tmp.length, num);
                    }
                }
            }

        }
    }

    @SuppressLint("NewApi")
    public int getSelectPosition(final int x, final int y) {
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        final Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
        if (num >= tmp.length) {
            num = tmp.length - 1;
        }
        int position = -1;
        for (int j = 0; j < tmp[num].length; j++) {
            Region region = tmp[0][j];
            if (region.contains(x, y)) {
                position = j;
            }
        }
        return position;
    }


    /**
     * 判断本月第一个日期的坐标
     */
    @SuppressLint("NewApi")
    public Region getFirstDayOfMonth() {
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        final Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
        if (num >= tmp.length) {
            num = tmp.length - 1;
        }
        int position = DPCalendar.getFirstDayWeek(centerYear, centerMonth);
        return tmp[num][position];
    }

    @SuppressLint("NewApi")
    public void changeChooseDate(int x, int y) {
        defineRegion(x, y);
    }

    private void computeDate() {
        rightYear = leftYear = centerYear;

        rightMonth = centerMonth + 1;
        leftMonth = centerMonth - 1;

        if (centerMonth == 12) {
            rightYear++;
            rightMonth = 1;
        }
        if (centerMonth == 1) {
            leftYear--;
            leftMonth = 12;
        }
    }


    public interface OnWeekViewChangeListener {
        void onWeekViewChange(boolean isForward);
    }

    public interface OnWeekDateClick {
        void onWeekDateClick(int x, int y, int count, int num);
    }

    private enum SlideMode {
        VER, HOR
    }

    private class BGCircle {
        private float x, y;
        private int radius;

        private ShapeDrawable shape;

        public BGCircle(ShapeDrawable shape) {
            this.shape = shape;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public ShapeDrawable getShape() {
            return shape;
        }

        public void setShape(ShapeDrawable shape) {
            this.shape = shape;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class ScaleAnimationListener implements
            ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            WeekView.this.invalidate();
        }
    }


    public void setOnLineChooseListener1(
            OnLineChooseListener1 onLineChooseListener) {
        this.onLineChooseListener = onLineChooseListener;
    }

    public interface OnLineChooseListener1 {
        void onLineChange1(int line);
    }
}