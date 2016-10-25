package com.niu.weekmonthcalenders.calenders;

/**
 * 主题的默认实现类
 * <p>
 * The default implement of theme
 *
 * @author AigeStudio 2015-06-17
 */
public class DPBaseTheme extends DPTheme {
    @Override
    public int colorBG() {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorBGCircle() {//这个是圆的背景颜色
        return 0xFFE22425;
    }

    @Override
    public int colorTitleBG() {
        return 0xFFF37B7A;
    }

    @Override
    public int colorTitle() {
        return 0xEEFFFFFF;
    }

    @Override
    public int colorToday() {
        return 0xFFFFBC21;
    }

    @Override
    public int colorG() {
        return 0xFF999999;
    }

    @Override
    public int colorF() {
        return 0xEEC08AA4;
    }

    @Override
    public int colorWeekend() {
        return 0xFFE22425;
    }

    @Override
    public int colorHoliday() {
        return 0x80FED6D6;
    }

    @Override
    public int colorL() {
        return 0xFF00ff00;
    }

    @Override
    public int colorDeferred() {
        return 0xFFFF0000;
    }

    @Override
    public int colorTodayText() {
        return 0xEEFF733A;
    }


    @Override
    public int colorChooseText() {
        return 0xFFFFFFFF;
    }
}
