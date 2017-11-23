package io.flyingmongoose.brave.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.flyingmongoose.brave.Fragment.FragHistoryOthers;
import io.flyingmongoose.brave.Fragment.FragHistoryYours;

/**
 * Created by IC on 6/16/2015.
 */
public class VPAdaptHistory extends FragmentStatePagerAdapter
{
    FragHistoryYours fragHistYours = null;
    FragHistoryOthers fragHistOthers = null;
    FragHistoryYours fragDefaultHistYours = null;
    CharSequence titles[];
    int numberOfTabs;

    public VPAdaptHistory(FragmentManager fragMang, CharSequence titles[], int numberOfTabs)
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
                    fragHistYours = new FragHistoryYours();
                return fragHistYours;

            case 1:
                if(fragHistOthers == null)
                    fragHistOthers = new FragHistoryOthers();
                return fragHistOthers;

            default:
                if(fragDefaultHistYours == null)
                    fragDefaultHistYours = new FragHistoryYours();
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
