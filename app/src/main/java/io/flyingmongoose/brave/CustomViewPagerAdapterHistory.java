package io.flyingmongoose.brave;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by IC on 6/16/2015.
 */
public class CustomViewPagerAdapterHistory extends FragmentStatePagerAdapter
{
    FragmentHistoryYours fragHistYours = null;
    FragmentHistoryOthers fragHistOthers = null;
    FragmentHistoryYours fragDefaultHistYours = null;
    CharSequence titles[];
    int numberOfTabs;

    public CustomViewPagerAdapterHistory(FragmentManager fragMang, CharSequence titles[], int numberOfTabs)
    {
        super(fragMang);

        this.titles = titles;
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch(position)
        {
            case 0:
                if(fragHistYours == null)
                    fragHistYours = new FragmentHistoryYours();
                return fragHistYours;

            case 1:
                if(fragHistOthers == null)
                    fragHistOthers = new FragmentHistoryOthers();
                return fragHistOthers;

            default:
                if(fragDefaultHistYours == null)
                    fragDefaultHistYours = new FragmentHistoryYours();
                return fragDefaultHistYours;
        }
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return titles[position];
    }

    @Override
    public int getCount()
    {
        return numberOfTabs;
    }
}
