package za.co.oneohtwofour.brave;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentHistory extends Fragment
{

    private FragmentActivity activContext;
    private ViewPager vpHistoryContent;
    private SlidingTabLayout stLayHistory;
    public CustomViewPagerAdapterHistory pageAdapter;
    private CharSequence titles[] = {"Yours" , "Others"};
    private int noOfTabs = 2;
    private final String TAG = "FragHistory";


    @Override
    public void onAttach(Activity activity)
    {
        activContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        vpHistoryContent = (ViewPager) view.findViewById(R.id.vpHistoryContent);
        stLayHistory = (SlidingTabLayout) view.findViewById(R.id.stLayHistory);

        initFragSlidingTab();

        return view;
    }

    private void initFragSlidingTab()
    {
        pageAdapter = new CustomViewPagerAdapterHistory(activContext.getSupportFragmentManager(), titles, noOfTabs);
        vpHistoryContent.setAdapter(pageAdapter);

        stLayHistory.setDistributeEvenly(true);
        stLayHistory.setCustomTabColorizer(new SlidingTabLayout.TabColorizer()
        {
            @Override
            public int getIndicatorColor(int position)
            {
                return getResources().getColor(R.color.White);
            }
        });
        stLayHistory.setViewPager(vpHistoryContent);
    }
}
