package com.yq.simpleindicator;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SimpleIndicator indicator;
    private ViewPager viewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assignViews();

        List<String> titles = new ArrayList<>();
        ArrayList<TextView> views = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            titles.add("标题" + (i + 1));
            TextView tv = new TextView(this);
            tv.setText("pager" + (i + 1));
            tv.setTextSize(18);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            views.add(tv);
        }


        viewpager.setAdapter(new ViewPagerAdapter(views));

        indicator.setTabItemTitles(titles);
        indicator.setViewPager(viewpager, 0);
    }


    private void assignViews() {
        indicator = (SimpleIndicator) findViewById(R.id.id_indicator);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
    }

}
