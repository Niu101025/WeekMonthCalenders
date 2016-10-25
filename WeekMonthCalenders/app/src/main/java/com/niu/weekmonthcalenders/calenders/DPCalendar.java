package com.niu.weekmonthcalenders.calenders;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * 月历抽象父类
 * 继承该类可以实现自己的日历对象
 * <p>
 * Abstract class of Calendar
 *
 * @author AigeStudio 2015-06-15
 */
public abstract class DPCalendar {
    protected final Calendar c = Calendar.getInstance();


    /**
     * 判断某年是否为闰年
     *
     * @param year ...
     * @return true表示闰年
     */
    public boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }

    /**
     * 判断给定日期是否为今天
     *
     * @param year  某年
     * @param month 某月
     * @param day   某天
     * @return ...
     */
    public boolean isToday(int year, int month, int day) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.set(year, month - 1, day);
        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) &&
                (c1.get(Calendar.MONTH) == (c2.get(Calendar.MONTH))) &&
                (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 生成某年某月的公历天数数组
     * 数组为6x7的二维数组因为一个月的周数永远不会超过六周
     * 天数填充对应相应的二维数组下标
     * 如果某个数组下标中没有对应天数那么则填充一个空字符串
     *
     * @param year  某年
     * @param month 某月
     * @return 某年某月的公历天数数组
     */
    public String[][] buildMonthG(int year, int month) {
        c.clear();
        String[][] tmp = new String[6][7];
        c.set(year, month - 1, 1);

        int daysInMonth = 0;
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 ||
                month == 12) {
            daysInMonth = 31;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            daysInMonth = 30;
        } else if (month == 2) {
            if (isLeapYear(year)) {
                daysInMonth = 29;
            } else {
                daysInMonth = 28;
            }
        }
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        int day = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                tmp[i][j] = "";
                if (i == 0 && j >= dayOfWeek) {
                    tmp[i][j] = "" + day;
                    day++;
                } else if (i > 0 && day <= daysInMonth) {
                    tmp[i][j] = "" + day;
                    day++;
                }
            }
        }
        return tmp;
    }

    /**
     * 生成某年某月的周末日期集合
     *
     * @param year  某年
     * @param month 某月
     * @return 某年某月的周末日期集合
     */
    public Set<String> buildMonthWeekend(int year, int month) {
        Set<String> set = new HashSet<>();
        c.clear();
        c.set(year, month - 1, 1);
        do {
            int day = c.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                set.add(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
            }
            c.add(Calendar.DAY_OF_YEAR, 1);
        } while (c.get(Calendar.MONTH) == month - 1);
        return set;
    }

    /**
     * 通过年份和月份 得到当月的日子
     *
     * @param year
     * @param month
     * @return
     */
    public static int getMonthDays(int year, int month) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return -1;
        }
    }

    /**
     * 返回当前月份1号位于周几
     *
     * @param year  年份
     * @param month 月份，传入系统获取的，不需要正常的
     * @return 日：1		一：2		二：3		三：4		四：5		五：6		六：7
     */
    public static int getFirstDayWeek(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        //月份需要－1
        calendar.set(year, month - 1, 1);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getLastDayWeek(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        //月份需要－1
        calendar.set(year, month - 1, getMonthDays(year, month));
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
}
