package za.co.oneohtwofour.brave;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;

/**
 * Created by IC on 5/24/2015.
 */
public class CustomViewPagerAdapterGroups extends FragmentStatePagerAdapter
{

    CharSequence titles[];
    int numberOfTabs;

    public CustomViewPagerAdapterGroups(FragmentManager fragMang, CharSequence titles[], int numberOfTabs)
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
                FragmentGroupsPublicOld fragGroupPublic = new FragmentGroupsPublicOld();
                return fragGroupPublic;

            case 1:
                FragmentGroupsPrivate fragGroupPrivate = new FragmentGroupsPrivate();
                return fragGroupPrivate;

            case 2:
                FragmentGroupsNewOld fragGroupNew = new FragmentGroupsNewOld();
                return fragGroupNew;

            default:
                FragmentGroupsPublicOld fragGroupPublicDefault = new FragmentGroupsPublicOld();
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
