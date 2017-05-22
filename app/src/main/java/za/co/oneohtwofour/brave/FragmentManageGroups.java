package za.co.oneohtwofour.brave;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class FragmentManageGroups extends Fragment implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener
{
    private FragmentActivity activContext;
    private ViewPager vpGroupsContent;
    private SlidingTabLayout stLayManageGroups;
    private CustomViewPagerAdapterGroups pagerAdapter;
    private int selectedPage = 0;
    CharSequence titles[] = {"Public", "Private", "New"};
    int numberOfTabs = 3;

    public static SearchView svGroups;

    private List<ParseObject> publicSearchResults;
    private String cached3CharSearchString = null;

    public FragmentManageGroups() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_manage_groups, container, false);

        vpGroupsContent = (ViewPager) view.findViewById(R.id.vpGroupsContent);
        stLayManageGroups = (SlidingTabLayout) view.findViewById(R.id.stLayManageGroups);
        svGroups = (SearchView) view.findViewById(R.id.svGroups);

        initGroupSlidingTab();

        return view;
    }

    private void initGroupSlidingTab()
    {
        svGroups.setOnQueryTextListener(this);

        //Setup tabs
        pagerAdapter = new CustomViewPagerAdapterGroups(activContext.getSupportFragmentManager(), titles, numberOfTabs);
        vpGroupsContent.setAdapter(pagerAdapter);

        stLayManageGroups.setDistributeEvenly(true);

        stLayManageGroups.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position)
            {
                return getResources().getColor(R.color.White);
            }
        });

        stLayManageGroups.setViewPager(vpGroupsContent);

        stLayManageGroups.setOnPageChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        ((HomeActivity)getActivity()).lockDrawer(true);
    }

    /*public static void onRefresh()
    {
        String currentQuery = svGroups.getQuery().toString();
        svGroups.setQuery(currentQuery, true);
    }*/

    public void searchPublicGroup(String searchString)
    {
        //Clean up searchString
        searchString = searchString.trim().replaceAll("\\s+", "").trim().toLowerCase().toString();

        Log.i("Searching public group", "cached: %" + cached3CharSearchString + "% searchString: %" + searchString + "%");
        if(cached3CharSearchString == null || !cached3CharSearchString.equalsIgnoreCase(searchString))  //only searches for new results set online when doesnt match previous search or is first search
        {
            FragmentGroupsPublic.srLaySearchPublicGroups.setRefreshing(true);
            Log.i("Searching public group", "searching online db");
            cached3CharSearchString = searchString; //set new cached search string
            final Toast msg = Toast.makeText(activContext, "", Toast.LENGTH_LONG);

            ParseQuery<ParseObject> querySearch = ParseQuery.getQuery("Groups");
            querySearch.whereStartsWith("flatValue", searchString).addAscendingOrder("flatValue").whereEqualTo("public", true);

            FragmentGroupsPublic.loadingAnimate();
            querySearch.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e)
                {
                    if (e == null)
                    {
                        publicSearchResults = parseObjects;

                        if(publicSearchResults != null)
                            FragmentGroupsPublic.populateResultList(activContext, publicSearchResults);

                        FragmentGroupsPublic.srLaySearchPublicGroups.setRefreshing(false);
                    }
                    else
                    {
                        FragmentGroupsPublic.srLaySearchPublicGroups.setRefreshing(false);
                        if (e.getCode() == 100)
                            msg.setText(R.string.error_100_no_internet);   //check internet conn
                        else
                            msg.setText("Unsuccessful while searching public groups: " + e.getMessage() + " code: " + e.getCode()); //display msg

                        msg.show();
                    }
                }
            });
        }
        else
            FragmentGroupsPublic.filterResultList("");      //resets data to have no filter
    }

    @Override
    public void onAttach(Activity activity)
    {
        activContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(int position)
    {
        switch (position)
        {
            case 0:
                svGroups.setQueryHint("Search group name");
                svGroups.setQuery("", false);
                svGroups.setImeOptions(EditorInfo.IME_ACTION_DONE);
                selectedPage = 0;
                break;

            case 1:
                svGroups.setQueryHint("Private group name");
                svGroups.setQuery("", false);
                svGroups.setImeOptions(EditorInfo.IME_ACTION_DONE);
                selectedPage = 1;
                break;

            case 2:
                svGroups.setQueryHint("New group name");
                svGroups.setQuery("", false);
                svGroups.setImeOptions(EditorInfo.IME_ACTION_DONE);
                selectedPage = 2;
                break;

            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        svGroups.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        if(selectedPage == 0)
        {
            //Check for searching
            if (newText.length() == 3)
            {
                searchPublicGroup(newText);
            }
            else if (newText.length() > 3)
            {
                Log.i("Filtering groups", "filter word: %" + newText + "%");
                FragmentGroupsPublic.filterResultList(newText);
            }
            else
            {
                //Reset filter
                FragmentGroupsPublic.filterResultList("");
            }
        }

        return false;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        ((HomeActivity)getActivity()).lockDrawer(false);
    }
}
