package com.niu.weekmonthcalenders.calenders;

/**
 * 主题抽象类
 * 你可以继承该类定制自己的颜色主题
 * <p>
 * Abstract class of theme
 * You can extends this class to implement your own theme colors
 *
 * @author AigeStudio 2015-06-30
 */
public abstract class DPTheme {
    /**
     * 月视图背景色
     * <p>
     * Color of MonthView's background
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorBG();

    /**
     * 背景圆颜色
     * <p>
     * Color of MonthView's selected circle
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorBGCircle();


    /**
     * 选中的文本颜色
     * <p>
     * Color of MonthView's selected text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorChooseText();

    /**
     * 标题栏背景色
     * <p>
     * Color of TitleBar's background
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorTitleBG();

    /**
     * 标题栏文本颜色
     * <p>
     * Color of TitleBar text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorTitle();

    /**
     * 今天的背景色
     * <p>
     * Color of Today's background
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorToday();

    /**
     * 公历文本颜色
     * <p>
     * Color of Gregorian text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorG();

    /**
     * 节日文本颜色
     * <p>
     * Color of Festival text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorF();

    /**
     * 周末文本颜色
     * <p>
     * Color of Weekend text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorWeekend();


    /**
     * 今天文本颜色
     * <p>
     * Color of Today text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorTodayText();

    /**
     * 假期文本颜色
     * <p>
     * Color of Holiday text
     *
     * @return 16进制颜色值 hex color
     */
    public abstract int colorHoliday();

    public abstract int colorL();

    public abstract int colorDeferred();
}
