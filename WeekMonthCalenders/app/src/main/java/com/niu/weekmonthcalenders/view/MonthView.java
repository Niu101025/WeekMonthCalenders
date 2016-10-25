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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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

;


/**
 * MonthView
 *
 * @author AigeStudio 2015-06-29
 */
public class MonthView extends View {
    private final Region[][] monthRegionsFour = new Region[4][7];
    private final Region[][] monthRegionsFive = new Region[5][7];
    private final Region[][] monthRegionsSix = new Region[6][7];

    private final DPInfo[][] infoFour = new DPInfo[4][7];
    private final DPInfo[][] infoFive = new DPInfo[5][7];
    private final DPInfo[][] infoSix = new DPInfo[6][7];

    private DPCManager mCManager = DPCManager.getInstance();
    private DPTManager mTManager = DPTManager.getInstance();

    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG
            | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
    protected Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG
            | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
    private Scroller mScroller;
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private OnLineCountChangeListener onLineCountChangeListener;
    private OnDateChangeListener onDateChangeListener;
    private OnLineChooseListener onLineChooseListener;
    private OnMonthViewChangeListener onMonthViewChangeListener;
    private OnMonthDateClick onMonthClick;
    private OnDatePickedListener onDatePickedListener;
    private ScaleAnimationListener scaleAnimationListener;

    private SlideMode mSlideMode;

    private int circleRadius;
    private int indexYear, indexMonth;
    private int centerYear, centerMonth;
    private int leftYear, leftMonth;
    private int rightYear, rightMonth;
    private int width, height;
    private int lastPointX, lastPointY;
    private int lastMoveX, lastMoveY;
    private int criticalWidth, criticalHeight;
    private int mNowYear, mNowMonth;
    private float sizeTextGregorian, sizeTextFestival;
    private float offsetYFestival1, offsetYFestival2;
    private int num = -1;
    // 记录日历的总行数： 5行，6行
    private int lineCount;
    // 点击选中的day
    //为了实现点击改变文本颜色
    //自定义一个文本size
    private int recordLine;
    private boolean isNewEvent, isTodayDisplay = true;
    private boolean isSelectDay = false;
    private String mSetDay = "";
    private Map<String, BGCircle> cirApr = new HashMap<>();

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scaleAnimationListener = new ScaleAnimationListener();
        }
        mNowYear = Calendar.getInstance().get(Calendar.YEAR);
        mNowMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        mScroller = new Scroller(context);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()));
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
                lastPointY = (int) event.getY();
//			break;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isNewEvent) {
                    if (Math.abs(lastPointX - event.getX()) > 100) {
                        mSlideMode = SlideMode.HOR;
                        isNewEvent = false;
                    }
                }
                if (mSlideMode == SlideMode.HOR) {
                    if (centerYear > mNowYear || (centerYear == mNowYear && centerMonth > mNowMonth)) {
                        int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
                        smoothScrollTo(totalMoveX, indexYear * height);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mSlideMode == SlideMode.VER) {
                } else if (mSlideMode == SlideMode.HOR) {
                    if (Math.abs(lastPointX - event.getX()) > 25) {
                        if (lastPointX > event.getX()
                                && Math.abs(lastPointX - event.getX()) >= criticalWidth) {
                            indexMonth++;
                            centerMonth = (centerMonth + 1) % 13;
                            if (centerMonth == 0) {
                                centerMonth = 1;
                                centerYear++;
                            }
                            //翻到下一页
                            if (null != onMonthViewChangeListener) {
                                onMonthViewChangeListener.onMonthViewChange(true);
                            }

                        } else if (lastPointX < event.getX()
                                && Math.abs(lastPointX - event.getX()) >= criticalWidth) {
                            if (centerYear > mNowYear || (centerYear == mNowYear && centerMonth > mNowMonth)) {
                                indexMonth--;
                                centerMonth = (centerMonth - 1) % 12;
                                if (centerMonth == 0) {
                                    centerMonth = 12;
                                    centerYear--;
                                }
                                if (null != onMonthViewChangeListener) {
                                    onMonthViewChangeListener.onMonthViewChange(false);
                                }
                            }
                        }

                        computeDate();
                        smoothScrollTo(width * indexMonth, indexYear * height);
                        String[] dates = ScrollLayout.selectDate.split("\\.");
                        int day;
                        if (dates != null && dates.length == 3) {
                            day = Integer.parseInt(dates[2]);
                            if (day <= 28) {
                                ScrollLayout.selectDate = centerYear + "." + centerMonth + "." + day;
                            } else {
                                if (day > DPCalendar.getMonthDays(centerYear, centerMonth)) {
                                    day = DPCalendar.getMonthDays(centerYear, centerMonth);
                                }
                                ScrollLayout.selectDate = centerYear + "." + centerMonth + "." + day;
                            }
                        }
                        lastMoveX = width * indexMonth;
                        defineRegion1(ScrollLayout.selectDate);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(measureWidth, (int) (measureWidth * 5F / 7F));
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
    }

    // 滑动back
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
    }

    public void moveForwad(boolean hasSet) {
        indexMonth++;
        centerMonth = (centerMonth + 1) % 13;
        if (centerMonth == 0) {
            centerMonth = 1;
            centerYear++;
        }
        if (null != onMonthViewChangeListener) {
            onMonthViewChangeListener.onMonthViewChange(true);
        }
        computeDate();
        smoothScrollTo(width * indexMonth, indexYear * height);

        String[] dates = ScrollLayout.selectDate.split("\\.");
        int day;
        if (dates != null && dates.length == 3) {
            day = Integer.parseInt(dates[2]);
            if (day <= 28) {
                ScrollLayout.selectDate = centerYear + "." + centerMonth + "." + day;
            } else {
                if (day > DPCalendar.getMonthDays(centerYear, centerMonth)) {
                    day = DPCalendar.getMonthDays(centerYear, centerMonth);
                }
                ScrollLayout.selectDate = centerYear + "." + centerMonth + "." + day;
            }
        }
        lastMoveX = width * indexMonth;
        defineRegion1(ScrollLayout.selectDate);
    }

    // 滑动back
    public void moveBack(boolean hasSet) {
        indexMonth--;
        centerMonth = (centerMonth - 1) % 12;
        if (centerMonth == 0) {
            centerMonth = 12;
            centerYear--;
        }
        if (null != onMonthViewChangeListener) {
            onMonthViewChangeListener.onMonthViewChange(false);
        }
        computeDate();
        smoothScrollTo(width * indexMonth, indexYear * height);
        String[] dates = ScrollLayout.selectDate.split("\\.");
        int day;
        if (dates != null && dates.length == 3) {
            day = Integer.parseInt(dates[2]);
            if (day <= 28) {
                ScrollLayout.selectDate = centerYear + "." + centerMonth + "." + day;
            } else {
                if (day > DPCalendar.getMonthDays(centerYear, centerMonth)) {
                    day = DPCalendar.getMonthDays(centerYear, centerMonth);
                }
                ScrollLayout.selectDate = centerYear + "." + centerMonth + "." + day;
            }
        }
        lastMoveX = width * indexMonth;
        defineRegion1(ScrollLayout.selectDate);
    }

    public void moveToMonth(String date) {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;

        criticalWidth = (int) (1F / 5F * width);
        criticalHeight = (int) (1F / 5F * height);

        int cellW = (int) (w / 7F);
        int cellH4 = (int) (h / 4F);
        int cellH5 = (int) (h / 5F);
        int cellH6 = (int) (h / 6F);
        //设置半径
        circleRadius = cellW * 88 / 150;

        sizeTextGregorian = width / 27F;
        mPaint.setTextSize(sizeTextGregorian);

        float heightGregorian = mPaint.getFontMetrics().bottom
                - mPaint.getFontMetrics().top;
        sizeTextFestival = width / 40F;
        mPaint.setTextSize(sizeTextFestival);

        float heightFestival = mPaint.getFontMetrics().bottom
                - mPaint.getFontMetrics().top;
        offsetYFestival1 = (((Math.abs(mPaint.ascent() + mPaint.descent())) / 2F)
                + heightFestival / 2F + heightGregorian / 2F) / 2F;
        offsetYFestival2 = offsetYFestival1 * 2F;

        for (int i = 0; i < monthRegionsFour.length; i++) {
            for (int j = 0; j < monthRegionsFour[i].length; j++) {
                Region region = new Region();
                region.set(j * cellW, i * cellH4, cellW + (j * cellW),
                        cellW + (i * cellH4));
                monthRegionsFour[i][j] = region;
            }
        }
        for (int i = 0; i < monthRegionsFive.length; i++) {
            for (int j = 0; j < monthRegionsFive[i].length; j++) {
                Region region = new Region();
                region.set(j * cellW, i * cellH5, cellW + (j * cellW),
                        cellW + (i * cellH5));
                monthRegionsFive[i][j] = region;
            }
        }
        for (int i = 0; i < monthRegionsSix.length; i++) {
            for (int j = 0; j < monthRegionsSix[i].length; j++) {
                Region region = new Region();
                region.set((j * cellW), (i * cellH6), cellW + (j * cellW),
                        cellW + (i * cellH6));
                monthRegionsSix[i][j] = region;
            }
        }
        if (isSelectDay) {
            defineRegion1(centerYear + "." + centerMonth + "." + mSetDay);
            isSelectDay = false;
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mTManager.colorBG());

        drawBGCircle(canvas);
        draw(canvas, width * (indexMonth - 1), height * indexYear, leftYear,
                leftMonth);
        draw(canvas, width * indexMonth, indexYear * height, centerYear,
                centerMonth);
        draw(canvas, width * (indexMonth + 1), height * indexYear, rightYear,
                rightMonth);
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
        //invalidate();
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

    @SuppressLint("NewApi")
    private void draw(Canvas canvas, int x, int y, int year, int month) {
        canvas.save();
        canvas.translate(x, 0);
        DPInfo[][] info = mCManager.obtainDPInfo(year, month);
        DPInfo[][] result;
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
            arrayClear(infoFour);
            result = arrayCopy(info, infoFour);
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
            arrayClear(infoFive);
            result = arrayCopy(info, infoFive);
        } else {
            tmp = monthRegionsSix;
            arrayClear(infoSix);
            result = arrayCopy(info, infoSix);
        }
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                recordLine = i;
                draw(canvas, tmp[i][j].getBounds(), info[i][j]);
            }
        }
        if (month == centerMonth && year == centerYear) {
            lineCount = result.length;
            changDateListener();
        }
        canvas.restore();
    }

    private void draw(Canvas canvas, Rect rect, DPInfo info) {
        drawBG(canvas, rect, info);
        drawGregorian(canvas, rect, info.strG, info.isChoosed, true, info.isToday);
    }

    private void drawBG(Canvas canvas, Rect rect, DPInfo info) {
        if (info.isToday && isTodayDisplay) {
            //只在 第一次初始化时 更新week的line 来悬挂当前日期
            if (null != onLineChooseListener && num == -1) {
                onLineChooseListener.onLineChange(recordLine);
            }
        }
    }

    /**
     * 绘制字的颜色
     *
     * @param canvas
     * @param rect
     * @param str
     * @param isChoosed
     * @param canSelect
     */
    private void drawGregorian(Canvas canvas, Rect rect, String str,
                               boolean isChoosed, boolean canSelect, boolean isToday) {
        mPaint.setTextSize(sizeTextGregorian);
        if (isChoosed) {
            mPaint.setColor(mTManager.colorChooseText());
        } else if (canSelect) {
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


    // 月份左右滑动切换
    public void setOnLineCountChangeListener(OnLineCountChangeListener onLineCountChangeListener) {
        this.onLineCountChangeListener = onLineCountChangeListener;
    }

    // 月份点击
    public void setOnMonthDateClickListener(OnMonthDateClick onMonthClick) {
        this.onMonthClick = onMonthClick;
    }

    // 通过MOnthView的变化来判断如何滑动weekview
    public void setOnMonthViewChangeListener(
            OnMonthViewChangeListener onWeekViewChangeListener) {
        this.onMonthViewChangeListener = onWeekViewChangeListener;
    }

    // 日期选择监听
    public void setOnDateChangeListener(
            OnDateChangeListener onDateChangeListener) {
        this.onDateChangeListener = onDateChangeListener;
    }

    public void setOnLineChooseListener(
            OnLineChooseListener onLineChooseListener) {
        this.onLineChooseListener = onLineChooseListener;
    }

    public void setOnDatePickedListener(
            OnDatePickedListener onDatePickedListener) {
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

    public void setDay(int year, int month, String day) {
        centerYear = year;
        centerMonth = month;
        mSetDay = Integer.parseInt(day) + "";
        isSelectDay = true;
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

    private DPInfo[][] arrayCopy(DPInfo[][] src, DPInfo[][] dst) {
        for (int i = 0; i < dst.length; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
        }
        return dst;
    }

    @SuppressLint("NewApi")
    public void defineRegion(final int x, final int y) {
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[i].length; j++) {
                Region region = tmp[i][j];
                if (TextUtils.isEmpty(mCManager.obtainDPInfo(centerYear,
                        centerMonth)[i][j].strG)) {
                    continue;
                }
                if (region.contains(x, y)) {
                    cirApr.clear();
                    num = i;
                    final String date = centerYear
                            + "."
                            + centerMonth
                            + "."
                            + mCManager.obtainDPInfo(centerYear,
                            centerMonth)[i][j].strG;
                    final DPInfo dpInfo = mCManager.obtainDPInfo(centerYear,
                            centerMonth)[i][j];
                    ScrollLayout.selectDate = date;
                    BGCircle circle = createCircle(region.getBounds()
                            .centerX() + indexMonth * width, region
                            .getBounds().centerY() + indexYear * height);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ValueAnimator animScale1 = ObjectAnimator.ofInt(
                                circle, "radius", 0, circleRadius);
                        animScale1.setDuration(10);
                        animScale1.setInterpolator(decelerateInterpolator);
                        animScale1
                                .addUpdateListener(scaleAnimationListener);

                        AnimatorSet animSet = new AnimatorSet();
                        animSet.playSequentially(animScale1);
                        animSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (null != onDatePickedListener) {
                                    onDatePickedListener.onDatePicked(dpInfo);
                                }
                                if (null != onLineChooseListener) {
                                    onLineChooseListener.onLineChange(num);
                                }
                                if (null != onMonthClick) {
                                    onMonthClick.onMonthDateClick(x, y);
                                }
                                WeekView.mSelectX = x;
                                WeekView.mSelectY = y;
                            }
                        });
                        animSet.start();
                    }
                    cirApr.put(date, circle);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        if (null != onDatePickedListener) {
                            onDatePickedListener.onDatePicked(dpInfo);
                        }
                        if (null != onLineChooseListener) {
                            onLineChooseListener.onLineChange(num);
                        }
                        if (null != onMonthClick) {
                            onMonthClick.onMonthDateClick(x, y);
                        }
                    }
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    invalidate();
                }
            }

        }
    }

    @SuppressLint("NewApi")
    public void defineRegion1(String dates) {
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        DPInfo dpInfo;
        DPInfo[][] result;
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
            arrayClear(infoFour);
            result = arrayCopy(info, infoFour);
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
            arrayClear(infoFive);
            result = arrayCopy(info, infoFive);
        } else {
            tmp = monthRegionsSix;
            arrayClear(infoSix);
            result = arrayCopy(info, infoSix);
        }
        int x, y;
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[i].length; j++) {
                Region region = tmp[i][j];
                dpInfo = result[i][j];
                if (TextUtils.equals(dpInfo.year + "." + dpInfo.month + "." + dpInfo.strG, dates)) {
                    x = region.getBounds().centerX();
                    y = region.getBounds().centerY();
                    defineRegion(x, y);
                    break;
                 /*   cirApr.clear();
                    num = i;
                    BGCircle circle = createCircle(region.getBounds()
                            .centerX() + indexMonth * width, region
                            .getBounds().centerY() + indexYear * height);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ValueAnimator animScale1 = ObjectAnimator.ofInt(
                                circle, "radius", 0, circleRadius);
                        animScale1.setDuration(10);
                        animScale1.setInterpolator(decelerateInterpolator);
                        animScale1
                                .addUpdateListener(scaleAnimationListener);

                        if (null != onDatePickedListener) {
                            onDatePickedListener.onDatePicked(dates);
                        }
                        if (null != onLineChooseListener) {
                            onLineChooseListener.onLineChange(num);
                        }
                        if (null != onMonthClick) {
                            onMonthClick.onMonthDateClick(x, y);
                        }
                        WeekView.mSelectX = x;
                        WeekView.mSelectY = y;
                    }
                    cirApr.put(dates, circle);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        if (null != onDatePickedListener) {
                            onDatePickedListener.onDatePicked(dates);
                        }
                        if (null != onLineChooseListener) {
                            onLineChooseListener.onLineChange(num);
                        }
                        if (null != onMonthClick) {
                            onMonthClick.onMonthDateClick(x, y);
                        }
                    }*/
                }
              /*  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    invalidate();
                }*/
            }

        }
    }

    @SuppressLint("NewApi")
    public void changeChooseDate(int x, int y) {
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[i].length; j++) {
                Region region = tmp[i][j];
                if (TextUtils.isEmpty(mCManager.obtainDPInfo(centerYear,
                        centerMonth)[i][j].strG)) {
                    continue;
                }
                if (region.contains(x, y)) {
                    final String date = centerYear + "." + centerMonth + "."
                            + mCManager.obtainDPInfo(centerYear,
                            centerMonth)[i][j].strG;
                    ScrollLayout.selectDate = date;

                    if (cirApr.get(date) == null) {
                        cirApr.clear();
                        num = i;
                        BGCircle circle = createCircle(region.getBounds()
                                .centerX() + indexMonth * width, region
                                .getBounds().centerY() + indexYear * height);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            ValueAnimator animScale1 = ObjectAnimator.ofInt(
                                    circle, "radius", 0, circleRadius);
                            animScale1.setDuration(10);
                            animScale1.setInterpolator(decelerateInterpolator);
                            animScale1
                                    .addUpdateListener(scaleAnimationListener);
                            AnimatorSet animSet = new AnimatorSet();
                            animSet.playSequentially(animScale1);
                            animSet.start();
                        }
                        cirApr.put(date, circle);
                    } else {
                        info[i][j].isChoosed = false;
                    }
                }

            }
        }
        invalidate();
    }

    private void computeDate() {
        rightYear = leftYear = centerYear;
        // topYear = centerYear - 1;
        // bottomYear = centerYear + 1;
        //
        // topMonth = centerMonth;
        // bottomMonth = centerMonth;

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

        if (null != onDateChangeListener) {
            onDateChangeListener.onDateChange(centerYear, centerMonth);
        }

    }

    public void changDateListener() {
        if (null != onLineCountChangeListener) {
            onLineCountChangeListener.onLineCountChange(lineCount);
        }
    }

    public interface OnLineCountChangeListener {
        void onLineCountChange(int lineCount);
    }

    public interface OnMonthDateClick {
        void onMonthDateClick(int x, int y);
    }

    public interface OnMonthViewChangeListener {
        void onMonthViewChange(boolean isforward);
    }

    public interface OnDateChangeListener {
        void onDateChange(int year, int month);
    }

    public interface OnLineChooseListener {
        void onLineChange(int line);
    }

    public interface OnDatePickedListener {
        void onDatePicked(DPInfo dpInfo);
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

    // 获取日历总行数
    public int getLineCount() {
        return lineCount;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class ScaleAnimationListener implements
            ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            MonthView.this.invalidate();
        }
    }
}
