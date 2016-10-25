package com.niu.weekmonthcalenders.calenders;

import android.text.TextUtils;

import com.niu.weekmonthcalenders.ScrollLayout;
import com.niu.weekmonthcalenders.beans.DPInfo;

import java.util.HashMap;


/**
 * 日期管理器
 * The manager of date picker.
 *
 * @author AigeStudio 2015-06-12
 */
public final class DPCManager {
    private static final HashMap<Integer, HashMap<Integer, DPInfo[][]>> DATE_CACHE = new HashMap<>();


    private static DPCManager sManager;

    private DPCalendar c;

    private DPCManager() {
        initCalendar(new DPCalendar() {
        });
    }

    /**
     * 获取月历管理器
     * Get calendar manager
     *
     * @return 月历管理器
     */
    public static DPCManager getInstance() {
        if (null == sManager) {
            sManager = new DPCManager();
        }
        return sManager;
    }

    /**
     * 初始化日历对象
     * <p>
     * Initialization Calendar
     *
     * @param c ...
     */
    public void initCalendar(DPCalendar c) {
        this.c = c;
    }


    /**
     * 获取指定年月的日历对象数组
     *
     * @param year  公历年
     * @param month 公历月
     * @return 日历对象数组 该数组长度恒为6x7 如果某个下标对应无数据则填充为null
     */
    public DPInfo[][] obtainDPInfo(int year, int month) {
      /*  HashMap<Integer, DPInfo[][]> dataOfYear = DATE_CACHE.get(year);
        if (null != dataOfYear && dataOfYear.size() != 0) {
            DPInfo[][] dataOfMonth = dataOfYear.get(month);
            if (dataOfMonth != null) {
                return dataOfMonth;
            }
            dataOfMonth = buildDPInfo(year, month);
            dataOfYear.put(month, dataOfMonth);
            return dataOfMonth;
        }
        if (null == dataOfYear) dataOfYear = new HashMap<>();
        DPInfo[][] dataOfMonth = buildDPInfo(year, month);
        dataOfYear.put((month), dataOfMonth);
        DATE_CACHE.put(year, dataOfYear);*/
        DPInfo[][] dataOfMonth = buildDPInfo(year, month);
        String[] dates = ScrollLayout.selectDate.split("\\.");
        if (dates != null && dates.length == 3) {
            String selectYear = dates[0];
            String selectMonth = dates[1];
            String selectDay = dates[2];
            for (int i = 0; i < dataOfMonth.length; i++) {
                DPInfo[] info = dataOfMonth[i];
                for (int j = 0; j < info.length; j++) {
                    if (TextUtils.equals(selectYear, info[j].year + "")
                            && TextUtils.equals(selectMonth, info[j].month + "")
                            && TextUtils.equals(selectDay, info[j].strG)) {
                        info[j].isChoosed = true;
                    } else {
                        info[j].isChoosed = false;
                    }
                }
            }
        }
        return dataOfMonth;
    }

    /**
     * 获取指定年月的某一行日历对象数组
     *
     * @param year  公历年
     * @param month 公历月
     * @return 日历对象数组 该数组长度恒为6x7 如果某个下标对应无数据则填充为null
     */
    public DPInfo[] obtainWeekDPInfo(int year, int month, int line) {
        DPInfo[][] dataOfMonth = buildDPInfo(year, month);
        int len = c.getMonthDays(year, month);
        int mMonth;
        int mYear;
        if (month == 1) {
            mMonth = 12;
            mYear = year - 1;
        } else {
            mMonth = month - 1;
            mYear = year;
        }
        int lastLen = c.getMonthDays(mYear, mMonth);
        int day = c.getFirstDayWeek(year, month);
        if (len == 28) {
            //四行
            switch (day) {
                case 1:
                    break;
                case 2:
                    for (int j = 0; j < 1; j++) {
                        dataOfMonth[0][j].strG = 31 + j + "";
                    }
                    for (int i = 1; i < 7; i++) {
                        dataOfMonth[4][i].strG = i + "";
                    }
                    break;
                case 3:
                    for (int j = 0; j < 2; j++) {
                        dataOfMonth[0][j].strG = 31 - 1 + j + "";
                    }
                    for (int i = 2; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 1 + "";
                    }
                    break;
                case 4:
                    for (int j = 0; j < 3; j++) {
                        dataOfMonth[0][j].strG = 31 - 2 + j + "";
                    }
                    for (int i = 3; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 2 + "";
                    }
                    break;
                case 5:
                    for (int j = 0; j < 4; j++) {
                        dataOfMonth[0][j].strG = 31 - 3 + j + "";
                    }
                    for (int i = 4; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 3 + "";
                    }
                    break;
                case 6:
                    for (int j = 0; j < 5; j++) {
                        dataOfMonth[0][j].strG = 31 - 4 + j + "";
                    }
                    for (int i = 5; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 4 + "";
                    }
                    break;
                case 7:
                    for (int j = 0; j < 6; j++) {
                        dataOfMonth[0][j].strG = 31 - 5 + j + "";
                    }
                    dataOfMonth[4][6].strG = 1 + "";
                    break;
            }
        } else if (len == 29) {
            switch (day) {
                case 1:
                    for (int i = 1; i < 7; i++) {
                        dataOfMonth[4][i].strG = i + "";
                    }
                    break;
                case 2:
                    for (int j = 0; j < 1; j++) {
                        dataOfMonth[0][j].strG = 31 + j + "";
                    }
                    for (int i = 2; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 1 + "";
                    }
                    break;
                case 3:
                    for (int j = 0; j < 2; j++) {
                        dataOfMonth[0][j].strG = 30 + j + "";
                    }
                    for (int i = 3; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 2 + "";
                    }
                    break;
                case 4:
                    for (int j = 0; j < 3; j++) {
                        dataOfMonth[0][j].strG = 29 + j + "";
                    }
                    for (int i = 4; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 3 + "";
                    }
                    break;
                case 5:
                    for (int j = 0; j < 4; j++) {
                        dataOfMonth[0][j].strG = 28 + j + "";
                    }
                    for (int i = 5; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 4 + "";
                    }
                    break;
                case 6:
                    for (int j = 0; j < 5; j++) {
                        dataOfMonth[0][j].strG = 27 + j + "";
                    }
                    dataOfMonth[4][6].strG = 1 + "";
                    break;
                case 7:
                    for (int j = 0; j < 5; j++) {
                        dataOfMonth[0][j].strG = 26 + j + "";
                    }
                    break;

            }
        } else if (len == 30) {
            switch (day) {
                case 1:
                    for (int i = 2; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 1 + "";
                    }
                    break;
                case 2:
                    for (int j = 0; j < 1; j++) {
                        dataOfMonth[0][j].strG = lastLen + j + "";
                    }
                    for (int i = 3; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 2 + "";
                    }
                    break;
                case 3:
                    for (int j = 0; j < 2; j++) {
                        dataOfMonth[0][j].strG = lastLen - 1 + j + "";
                    }
                    for (int i = 4; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 3 + "";
                    }
                    break;
                case 4:
                    for (int j = 0; j < 3; j++) {
                        dataOfMonth[0][j].strG = lastLen - 2 + j + "";
                    }
                    for (int i = 5; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 4 + "";
                    }
                    break;
                case 5:
                    for (int j = 0; j < 4; j++) {
                        dataOfMonth[0][j].strG = lastLen - 3 + j + "";
                    }
                    for (int i = 6; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 5 + "";
                    }
                    break;
                case 6:
                    for (int j = 0; j < 5; j++) {
                        dataOfMonth[0][j].strG = lastLen - 4 + j + "";
                    }
                    break;
                case 7:
                    for (int j = 0; j < 6; j++) {
                        dataOfMonth[0][j].strG = lastLen - 5 + j + "";
                    }
                    for (int k = 1; k < 7; k++) {
                        dataOfMonth[5][k].strG = k + "";
                    }
                    break;
            }
        } else if (len == 31) {
            switch (day) {
                case 1:
                    for (int i = 3; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 2 + "";
                    }
                    break;
                case 2:
                    for (int j = 0; j < 1; j++) {
                        dataOfMonth[0][j].strG = lastLen + j + "";
                    }
                    for (int i = 4; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 3 + "";
                    }
                    break;
                case 3:
                    for (int j = 0; j < 2; j++) {
                        dataOfMonth[0][j].strG = lastLen - 1 + j + "";
                    }
                    for (int i = 5; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 4 + "";
                    }
                    break;
                case 4:
                    for (int j = 0; j < 3; j++) {
                        dataOfMonth[0][j].strG = lastLen - 2 + j + "";
                    }
                    for (int i = 6; i < 7; i++) {
                        dataOfMonth[4][i].strG = i - 5 + "";
                    }
                    break;
                case 5:
                    for (int j = 0; j < 4; j++) {
                        dataOfMonth[0][j].strG = lastLen - 3 + j + "";
                    }
                    break;
                case 6:
                    for (int j = 0; j < 5; j++) {
                        dataOfMonth[0][j].strG = lastLen - 4 + j + "";
                    }
                    for (int k = 1; k < 7; k++) {
                        dataOfMonth[5][k].strG = k + "";
                    }
                    break;
                case 7:
                    for (int j = 0; j < 6; j++) {
                        dataOfMonth[0][j].strG = lastLen - 5 + j + "";
                    }
                    for (int k = 2; k < 7; k++) {
                        dataOfMonth[5][k].strG = k - 1 + "";
                    }
                    break;
            }
        }
        String[] dates = ScrollLayout.selectDate.toString().split("\\.");
        if (dates != null && dates.length == 3) {
            String selectYear = dates[0];
            String selectMonth = dates[1];
            String selectDay = dates[2];
            for (int i = 0; i < dataOfMonth[line].length; i++) {
                if (TextUtils.equals(selectYear, dataOfMonth[line][i].year + "")
                        && TextUtils.equals(selectMonth, dataOfMonth[line][i].month + "")
                        && TextUtils.equals(selectDay, dataOfMonth[line][i].strG)) {
                    dataOfMonth[line][i].isChoosed = true;
                } else {
                    dataOfMonth[line][i].isChoosed = false;

                }
            }
        }
        return dataOfMonth[line];
    }


    private DPInfo[][] buildDPInfo(int year, int month) {
        DPInfo[][] info = new DPInfo[6][7];

        String[][] strG = c.buildMonthG(year, month);
        //可以在这里给日历设置哪天可以选择
        //Set<String> strWeekend = c.buildMonthWeekend(year, month);
        for (int i = 0; i < info.length; i++) {
            for (int j = 0; j < info[i].length; j++) {
                DPInfo tmp = new DPInfo();
                tmp.strG = strG[i][j];
                tmp.year = year;
                tmp.month = month;
                if (!TextUtils.isEmpty(tmp.strG)) tmp.isToday =
                        c.isToday(year, month, Integer.valueOf(tmp.strG));
                info[i][j] = tmp;
            }
        }
        return info;
    }
}
