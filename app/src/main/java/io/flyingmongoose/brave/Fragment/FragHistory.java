package io.flyingmongoose.brave.fragment;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.flyingmongoose.brave.adapter.VPAdaptHistory;
import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.util.UtilAnalytics;
import io.flyingmongoose.brave.view.ViewSlidingTabLayout;


public class FragHistory extends Fragment
{
    private final String TAG = "FragHistory";
    private final String SCREEN_NAME = "History";

    private FragmentActivity activContext;
    private ViewPager vpHistoryContent;
    private ViewSlidingTabLayout stLayHistory;
    public VPAdaptHistory pageAdapter;
    private CharSequence titles[] = {"Yours" , "Others"};
    private int noOfTabs = 2;

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
        stLayHistory = (ViewSlidingTabLayout) view.findViewById(R.id.stLayHistory);

        initFragSlidingTab();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        UtilAnalytics.logEventScreenView(getActivity(), SCREEN_NAME, TAG);
    }

    private void initFragSlidingTab()
    {
        pageAdapter = new VPAdaptHistory(activContext.getSupportFragmentManager(), titles, noOfTabs);
        vpHistoryContent.setAdapter(pageAdapter);

        stLayHistory.setDistributeEvenly(true);
        stLayHistory.setCustomTabColorizer(new ViewSlidingTabLayout.TabColorizer()
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
