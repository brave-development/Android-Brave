package io.flyingmongoose.brave.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.yayandroid.parallaxrecyclerview.ParallaxRecyclerView;

import java.util.List;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.Adapter.RVAdaptGroups;
import io.flyingmongoose.brave.R;

/**
 * Created by wprenison on 2017/06/02.
 */

public class FragGroupsPublic extends Fragment
{

    private SearchView svGroupsPublic;
    private ParallaxRecyclerView paravGroupsPublic;
    private SwipeRefreshLayout srLayGroupsPublic;
    private TextView txtvSearchPublicDesc;

    private Context context;
    private List<ParseObject> publicSearchResults;
    private String cached3CharSearchString = null;
    private RVAdaptGroups lstAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_groups_public, container, false);

        //Get handle on views
        svGroupsPublic = (SearchView) constructedView.findViewById(R.id.svGroupsPublic);
        paravGroupsPublic = (ParallaxRecyclerView) constructedView.findViewById(R.id.recvGroupsPublic);
        srLayGroupsPublic = (SwipeRefreshLayout) constructedView.findViewById(R.id.srLayGroupsPublic);
        txtvSearchPublicDesc = (TextView) constructedView.findViewById(R.id.txtvSearchPublicDesc);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();

        //init list
        paravGroupsPublic.setLayoutManager(new LinearLayoutManager(getActivity()));
        paravGroupsPublic.setHasFixedSize(true);

        svGroupsPublic.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                svGroupsPublic.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                //Check for searching
                if (newText.length() == 3)
                {
                    searchPublicGroup(newText);
                }
                else if (newText.length() > 3)
                {
                    Log.i("Filtering groups", "filter word: %" + newText + "%");
                    filterResultList(newText);
                }
                else
                {
                    //Reset filter
                    filterResultList("");
                    txtvSearchPublicDesc.setText("Type at least 3 \ncharacters to search");
                }
                return false;
            }
        });

        initSwipeRefresh();
    }

    private void initSwipeRefresh()
    {
        srLayGroupsPublic.setEnabled(false);
        srLayGroupsPublic.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayGroupsPublic.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
    }

    public void searchPublicGroup(String searchString)
    {
        //Clean up searchString
        searchString = searchString.trim().replaceAll("\\s+", "").trim().toLowerCase().toString();

        Log.i("Searching public group", "cached: %" + cached3CharSearchString + "% searchString: %" + searchString + "%");
        if(cached3CharSearchString == null || !cached3CharSearchString.equalsIgnoreCase(searchString))  //only searches for new results set online when doesnt match previous search or is first search
        {
            srLayGroupsPublic.setRefreshing(true);
            txtvSearchPublicDesc.setVisibility(View.INVISIBLE);
            Log.i("Searching public group", "searching online db");
            cached3CharSearchString = searchString; //set new cached search string
            final Snackbar msg = Snackbar.make(paravGroupsPublic, "", Snackbar.LENGTH_LONG);

            ParseQuery<ParseObject> querySearch = ParseQuery.getQuery("Groups");
            querySearch.whereStartsWith("flatValue", searchString).addAscendingOrder("flatValue").whereEqualTo("public", true).whereNotEqualTo("subscriberObjects", ActivHome.currentUser.getObjectId());

            querySearch.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e)
                {
                    if (e == null)
                    {
                        publicSearchResults = parseObjects;

                        Log.d("searchDebug", "No of items found: " + parseObjects.size());

                        if(publicSearchResults != null && publicSearchResults.size() > 0)
                        {
                            populateResultList(context, publicSearchResults);
                            txtvSearchPublicDesc.setText(publicSearchResults.size() + " Results Found");
                        }
                        else
                            txtvSearchPublicDesc.setText("No Results");

                        srLayGroupsPublic.setRefreshing(false);
                        txtvSearchPublicDesc.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        srLayGroupsPublic.setRefreshing(false);
                        txtvSearchPublicDesc.setVisibility(View.VISIBLE);
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
            filterResultList("");      //resets data to have no filter
    }

    public void populateResultList(Context context, List<ParseObject> results)
    {
        Log.d("searchDebug", "pupulating list with results");
        lstAdapter = new RVAdaptGroups(context, null, this, results, getFragmentManager());
        paravGroupsPublic.setAdapter(lstAdapter);
    }

    public void filterResultList(CharSequence filterTerm)
    {
        if(lstAdapter != null)
            lstAdapter.getFilter().filter(filterTerm);
    }

    public void subscribeUserToGroup(ParseObject group)
    {
        srLayGroupsPublic.setRefreshing(true);
        final String groupName = group.getString("name");

        //sub to group
        group.addUnique("subscriberObjects", ActivHome.currentUser.getObjectId());
        group.increment("subscribers", 1);
        final Snackbar msg = Snackbar.make(paravGroupsPublic, "", Snackbar.LENGTH_SHORT);

//        progbGroupsPrivate.setVisibility(View.VISIBLE);
        group.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
                }
                else
                {
                    srLayGroupsPublic.setRefreshing(false);
                    srLayGroupsPublic.setRefreshing(false);
                    msg.setText("Unsuccessful subscribing user to group :" + e.getMessage() + " Code: " + e.getCode());
                    msg.show();
                }
            }
        });

        //sub group to user
        ActivHome.currentUser.addUnique("groups", group.getString("name"));
        ActivHome.currentUser.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
//                progbGroupsPrivate.setVisibility(View.INVISIBLE);
                srLayGroupsPublic.setRefreshing(false);
                if (e == null)
                {
                    srLayGroupsPublic.setRefreshing(false);
                    msg.setText("Successfully subscribed to " + groupName);
                    ActivHome.fragManager.popBackStack();
                }
                else
                {
                    srLayGroupsPublic.setRefreshing(false);
                    msg.setText("Unsuccessful subscribing group to user :" + e.getMessage() + " Code: " + e.getCode());
                }

                msg.show();
            }
        });

        //sub to channel
        //Caps start of each word, uncaps every other
        String[] words = groupName.split(" ");
        String formattedGroupName = "";
        for (int i = 0; i < words.length; i++)
        {
            char[] wordLetters = words[i].toCharArray();
            wordLetters[0] = Character.toUpperCase(wordLetters[0]);

            //Uncaps the rest
            if (wordLetters.length > 1)
            {
                for (int j = 1; j < wordLetters.length; j++)
                {
                    wordLetters[j] = Character.toLowerCase(wordLetters[j]);
                }
            }

            formattedGroupName += new String(wordLetters) + " ";
        }
        final String finalFormattedGroupName = formattedGroupName;

        final String channelName = groupName.replaceAll("\\s+", "").trim().toString();

        ParsePush.subscribeInBackground(channelName);
    }
}
