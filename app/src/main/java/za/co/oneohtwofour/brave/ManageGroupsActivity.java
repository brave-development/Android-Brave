package za.co.oneohtwofour.brave;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class ManageGroupsActivity extends ActionBarActivity
{

    private Intent intent;
    private String searchQuery;

    private SlidingTabLayout tabs;
    private ViewPager vpGroupContent;
    private CustomViewPagerAdapterGroups pagerAdapter;
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
        pagerAdapter = new CustomViewPagerAdapterGroups(getSupportFragmentManager(), titles, numberOfTabs);

        vpGroupContent = (ViewPager) findViewById(R.id.vpGroupsContent);
        vpGroupContent.setAdapter(pagerAdapter);

        tabs = (SlidingTabLayout) findViewById(R.id.stLayManageGroups);
        tabs.setDistributeEvenly(true);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
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