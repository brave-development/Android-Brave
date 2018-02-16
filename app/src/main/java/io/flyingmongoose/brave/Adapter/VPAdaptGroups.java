package io.flyingmongoose.brave.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;

import io.flyingmongoose.brave.fragment.FragGroupsNewOld;
import io.flyingmongoose.brave.fragment.FragGroupsPrivate;
import io.flyingmongoose.brave.fragment.FragGroupsPublicOld;

/**
 * Created by IC on 5/24/2015.
 */
public class VPAdaptGroups extends FragmentStatePagerAdapter
{

    CharSequence titles[];
    int numberOfTabs;

    public VPAdaptGroups(FragmentManager fragMang, CharSequence titles[], int numberOfTabs)
    {
        super(fragMang);

        this.titles = titles;
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                FragGroupsPublicOld fragGroupPublic = new FragGroupsPublicOld();
                return fragGroupPublic;

            case 1:
                FragGroupsPrivate fragGroupPrivate = new FragGroupsPrivate();
                return fragGroupPrivate;

            case 2:
                FragGroupsNewOld fragGroupNew = new FragGroupsNewOld();
                return fragGroupNew;

            default:
                FragGroupsPublicOld fragGroupPublicDefault = new FragGroupsPublicOld();
                return fragGroupPublicDefault;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount()
    {
        return numberOfTabs;
    }
}
