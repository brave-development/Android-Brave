package io.flyingmongoose.brave.Activity;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.flyingmongoose.brave.Adapter.VPAdaptGroups;
import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.View.ViewSlidingTabLayout;


public class ActivManageGroups extends AppCompatActivity
{

    private Intent intent;
    private String searchQuery;

    private ViewSlidingTabLayout tabs;
    private ViewPager vpGroupContent;
    private VPAdaptGroups pagerAdapter;
    CharSequence titles[] = {"Public", "Private", "New"};
    int numberOfTabs = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups);

        intent = getIntent();

        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            //do search
        }

        //Setup tabs
        pagerAdapter = new VPAdaptGroups(getSupportFragmentManager(), titles, numberOfTabs);

        vpGroupContent = (ViewPager) findViewById(R.id.vpGroupsContent);
        vpGroupContent.setAdapter(pagerAdapter);

        tabs = (ViewSlidingTabLayout) findViewById(R.id.stLayManageGroups);
        tabs.setDistributeEvenly(true);

        tabs.setCustomTabColorizer(new ViewSlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position)
            {
                return getResources().getColor(R.color.White);
            }
        });

        tabs.setViewPager(vpGroupContent);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            //do search
        }
    }

    public void searchGroups()
    {
        //Use parse to actually search for group here
    }
}