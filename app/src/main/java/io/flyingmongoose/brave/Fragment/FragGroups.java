package io.flyingmongoose.brave.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.wooplr.spotlight.SpotlightView;
import com.yayandroid.parallaxrecyclerview.ParallaxRecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.adapter.RVAdaptGroups;
import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.util.UtilAnalytics;
import io.flyingmongoose.brave.util.UtilFormating;
import io.flyingmongoose.brave.dialog.DiagGroupsPrivate;

/**
 * Created by wprenison on 2017/05/17.
 */

public class FragGroups extends Fragment
{
    private final String TAG = "FragGroups";
    private final String SCREEN_NAME = "Groups";
    private final int groupLimit = 50;

    private ActivHome activity;
    private SwipeRefreshLayout srLayGroups;
    public ParallaxRecyclerView pararvGroups;
    public FloatingActionMenu famGroups;
    private FloatingActionButton fabCreateNew;
    private FloatingActionButton fabJoinPrivate;
    private FloatingActionButton fabJoinCom;

    public RVAdaptGroups lstAdapter;

    private SpotlightView spotvFamGroups;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_groups, container, false);

        //Get handle on views
        pararvGroups = (ParallaxRecyclerView) constructedView.findViewById(R.id.recyclerView);

        famGroups = (FloatingActionMenu) constructedView.findViewById(R.id.famMenuGroups);
        fabCreateNew = (FloatingActionButton) constructedView.findViewById(R.id.fabCreateGroup);
        fabJoinPrivate = (FloatingActionButton) constructedView.findViewById(R.id.fabJoinPrivateGroup);
        fabJoinCom = (FloatingActionButton) constructedView.findViewById(R.id.fabJoinComGroup);
        srLayGroups = (SwipeRefreshLayout) constructedView.findViewById(R.id.srLayGroups);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        activity = (ActivHome) getActivity();
        UtilAnalytics.logEventScreenView(activity, SCREEN_NAME, TAG);

        pararvGroups.setLayoutManager(new LinearLayoutManager(activity));
        pararvGroups.setHasFixedSize(true);

        initSwipeRefresh();

        findSubscribedGroups();

        fabCreateNew.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                famGroups.close(true);

                //Open new groups fragment
                FragmentTransaction fragTrans = ActivHome.fragManager.beginTransaction();

                //set args
                FragGroupNew fragNew = new FragGroupNew();
                Bundle bundleArgs = new Bundle();
                bundleArgs.putBoolean("newGroup", true);
                fragNew.setArguments(bundleArgs);

                fragTrans.replace(R.id.HomeContentLayout, fragNew , "fragGroupNew");
                fragTrans.addToBackStack("fragGroupNew");
                fragTrans.commit();
            }
        });

        fabJoinPrivate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DiagGroupsPrivate fragJoinPvt = new DiagGroupsPrivate();
                fragJoinPvt.show(getFragmentManager(), "fragDiagJoinPvt");
                famGroups.close(true);
            }
        });

        fabJoinCom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                famGroups.close(true);

                //Open join com fragment
                FragmentTransaction fragTrans = ActivHome.fragManager.beginTransaction();
                fragTrans.replace(R.id.HomeContentLayout, new FragGroupsPublic(), "fragGroupsPublic");
                fragTrans.addToBackStack("fragGroupsPublic");
                fragTrans.commit();
            }
        });

    }

    public void showEditGroup(int groupIndex)
    {
        //Open new groups fragment
        FragmentTransaction fragTrans = ActivHome.fragManager.beginTransaction();

        //set args
        FragGroupNew fragEdit = new FragGroupNew();
        Bundle bundleArgs = new Bundle();
        bundleArgs.putBoolean("newGroup", false);
        bundleArgs.putInt("groupIndex", groupIndex);
        fragEdit.setArguments(bundleArgs);

        fragTrans.replace(R.id.HomeContentLayout, fragEdit , "fragGroupEdit");
        fragTrans.addToBackStack("fragGroupEdit");
        fragTrans.commit();

    }

    private void initSwipeRefresh()
    {
        srLayGroups.setEnabled(false);
        srLayGroups.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                srLayGroups.setRefreshing(true);
                srLayGroups.setRefreshing(false);
            }
        });
        srLayGroups.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayGroups.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayGroups.setProgressViewOffset(true, 0, 8);
    }

    private void updateList(List<ParseObject> subbedGroups)
    {
        lstAdapter = new RVAdaptGroups(activity, this, null, subbedGroups, getFragmentManager());
        pararvGroups.setAdapter(lstAdapter);
    }


    public void findSubscribedGroups()
    {
        srLayGroups.setRefreshing(true);
        //Update user
        ActivHome.currentUser.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if(e == null)
                {
                    List<String> groupNames = new ArrayList<String>();

                    if(parseObject.getList("groups") != null)
                        groupNames = parseObject.getList("groups");

                    //Query for group objects, ps only ones the user is subbed to
                    ParseQuery<ParseObject> querySubbedGroups = new ParseQuery<ParseObject>("Groups");
                    querySubbedGroups.whereContainedIn("name", groupNames);
                    querySubbedGroups.findInBackground(new FindCallback<ParseObject>()
                    {
                        @Override
                        public void done(List<ParseObject> list, ParseException e)
                        {
                            srLayGroups.setRefreshing(false);
                            if(e == null)
                            {
                                updateList(list);
                                subInstallationToChannels(list);
                            } else
                            {
                                if(e.getCode() == 100)
                                    Snackbar.make(pararvGroups, R.string.error_100_no_internet, Snackbar.LENGTH_LONG).show();
                                else
                                    Snackbar.make(pararvGroups, "Unsuccessful retrieval of subscribed groups: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();

                            }
                        }
                    });
                } else
                {
                    srLayGroups.setRefreshing(false);
                    if(e.getCode() == 100)
                        Snackbar.make(pararvGroups, R.string.error_100_no_internet, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(pararvGroups, "Unsuccessful retrieval of subscribed groups: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }

    public void subInstallationToChannels(List<ParseObject> lstGroups)
    {
        //sub to channel
        List<String> lstChannelsToSubTo = new ArrayList<String>();

        for(int g = 0; g < lstGroups.size(); g++)
        {
            //Caps start of each word, uncaps every other
            String groupName = lstGroups.get(g).getString("name");
            lstChannelsToSubTo.add(UtilFormating.formatChannelName(groupName));
        }

        ParseInstallation.getCurrentInstallation().remove("channels");
        ParseInstallation.getCurrentInstallation().addAll("channels", lstChannelsToSubTo);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public void unSubUserFromGroup(final ParseObject group)
    {
        //Unsub user from group
        List<String> userId = new ArrayList<String>();
        userId.add(ActivHome.currentUser.getObjectId());
        group.increment("subscribers", -1);
        group.removeAll("subscriberObjects", userId);

        final Snackbar msg = Snackbar.make(pararvGroups, "", Snackbar.LENGTH_LONG);

        srLayGroups.setRefreshing(true);
//        progbGroupsSubbed.setVisibility(View.VISIBLE);
        group.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
                    srLayGroups.setRefreshing(false);
                } else
                {
//                    progbGroupsSubbed.setVisibility(View.GONE);
                    pararvGroups.setVisibility(View.VISIBLE);
                    srLayGroups.setRefreshing(false);

                    if(e.getCode() == 100)
                        msg.setText(R.string.error_100_no_internet);
                    else
                        msg.setText("Unsuccessful un subscribe user from group: " + e.getMessage() + " Coed: " + e.getCode());
                }
            }
        });

        List<String> groupNameAsId = new ArrayList<String>();
        groupNameAsId.add(group.getString("name"));
        ActivHome.currentUser.removeAll("groups", groupNameAsId);

        ActivHome.currentUser.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    msg.setText("You made like a tree and leafed " + group.getString("name"));
                }
                else
                {
//                    progbGroupsSubbed.setVisibility(View.GONE);
                    pararvGroups.setVisibility(View.VISIBLE);

                    if(e.getCode() == 100)
                        msg.setText(R.string.error_100_no_internet);
                    else
                        msg.setText("Unsuccessful un subscribe group from user: " + e.getMessage() + " Code: " + e.getCode());
                }
            }
        });

        //unsub to channel
        String groupName = group.getString("name");
        List<String> lstChannelsToUnSub = new ArrayList<String>();
        lstChannelsToUnSub.add(UtilFormating.formatChannelName(groupName));
        ParseInstallation.getCurrentInstallation().removeAll("channels", lstChannelsToUnSub);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        msg.show();
    }
}
