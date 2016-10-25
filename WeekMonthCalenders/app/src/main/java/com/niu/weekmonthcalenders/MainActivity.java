package com.niu.weekmonthcalenders;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.niu.weekmonthcalenders.beans.DPInfo;
import com.niu.weekmonthcalenders.view.MonthView;
import com.niu.weekmonthcalenders.view.WeekView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity implements MonthView.OnDateChangeListener,
        MonthView.OnDatePickedListener, View.OnClickListener, ScrollLayout.MoveTopCallBack, ScrollLayout.MoveUpCallBack {

    private MonthView monthView;
    private WeekView weekView;
    private Calendar now;
    private TextView mTvTitle;
    private ScrollLayout mScrollLayout;
    private ListView listView;
    private List<String> datas = new ArrayList<>();
    private MyAdapter myAdapter;
    public static View mWeekView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        now = Calendar.getInstance();
        mTvTitle = (TextView) findViewById(R.id.month_title);
        mTvTitle.setText("北京演出日历");
        monthView = (MonthView) findViewById(R.id.month_calendar);
        weekView = (WeekView) findViewById(R.id.week_calendar);
        monthView.setDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        monthView.setTodayDisplay(true);
        monthView.setOnDateChangeListener(this);
        monthView.setOnDatePickedListener(this);
        mScrollLayout = (ScrollLayout) findViewById(R.id.myScrollLayout);
        weekView.setDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        weekView.setTodayDisplay(true);
        weekView.setOnDatePickedListener(this);
        mWeekView1 = findViewById(R.id.rl_week1);
        listView = (ListView) findViewById(R.id.listview);
        for (int i = 0; i < 30; i++) {
            datas.add(now.get(Calendar.YEAR) + now.get(Calendar.MONTH) + 1 + "------" + i);
        }
        if (myAdapter == null) {
            myAdapter = new MyAdapter();
            listView.setAdapter(myAdapter);
        }
        myAdapter.notifyDataSetChanged();
        mScrollLayout.setMoveTopCallBack(this);
        mScrollLayout.setMoveUpCallBack(this);
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onDateChange(int year, int month) {

    }


    @Override
    public void moveTopCallback(int count, int index) {

    }

    @Override
    public void moveUpCallback(int count, int index) {

    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDatePicked(DPInfo dpInfo) {
        try {
            datas.clear();
            for (int i = 0; i < 30; i++) {
                datas.add(dpInfo.strG + "------" + i);
            }
            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.content_list_item_abs, null);
                holder = new Holder();
                holder.textView = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            holder.textView.setText(datas.get(position));
            return convertView;
        }

        class Holder {
            TextView textView;
        }
    }

}
