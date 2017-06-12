package io.flyingmongoose.brave;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class FragmentGroupsOld extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener
{
    private Context context;
    private ListView lstvSubbedGroups;
    private CustomListAdapterGroups lstAdapter;
//    private ProgressBar progbGroupsSubbed;
    public static TextView txtvGroupsRemainingValue;
    private SwipeRefreshLayout srLayGroups;
    private ImageButton ibtnManageGroups;

    private final String TAG = "FragmentGroupsOld";

    public FragmentGroupsOld()
    {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_groups_old, container, false);

        lstvSubbedGroups = (ListView) view.findViewById(R.id.lstvGroups);
        txtvGroupsRemainingValue = (TextView) view.findViewById(R.id.txtvGroupsRemainingValue);
//        progbGroupsSubbed = (ProgressBar) view.findViewById(R.id.progbGroupsSubbed);
        ibtnManageGroups = (ImageButton) view.findViewById(R.id.ibtnManageGroups);
        srLayGroups = (SwipeRefreshLayout) view.findViewById(R.id.srLayGroups);
        initSwipeRefresh();

        return view;
    }

    private void initSwipeRefresh()
    {
        srLayGroups.setOnRefreshListener(this);
        srLayGroups.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayGroups.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayGroups.setProgressViewOffset(true, 0, 130);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();

        initFragGroups();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);

        if(!hidden)
            initFragGroups();
    }

    public void initFragGroups()
    {
        ibtnManageGroups.setVisibility(View.INVISIBLE);
        lstvSubbedGroups.setVisibility(View.INVISIBLE);

        //check if user belongs to any groups and load them into list view
        srLayGroups.setRefreshing(true);
        findSubscribedGroups();
    }

    private void updateRemainingGroups()
    {
        //Check for groups remaining value
        HomeActivity.currentUser.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if(e == null)
                {
                    int groupsSlots = HomeActivity.currentUser.getInt("numberOfGroups");

                    txtvGroupsRemainingValue.setText( "" + (groupsSlots - lstAdapter.getCount()));
                    //Animate add button in
                    animateAddGroupButton(true);
                    srLayGroups.setRefreshing(false);
                }
            }
        });
    }

    private void updateList(List<ParseObject> subbedGroups)
    {
        lstAdapter = new CustomListAdapterGroups(context, R.layout.list_item_group_old, subbedGroups);
        lstvSubbedGroups.setAdapter(lstAdapter);
        lstvSubbedGroups.setOnItemClickListener(this);
        lstvSubbedGroups.setOnScrollListener(this);

        registerForContextMenu(lstvSubbedGroups);
        lstvSubbedGroups.setVisibility(View.VISIBLE);
        //Check for groups remaining value
        updateRemainingGroups();
    }

    private int svPrevFirstVisibleItem;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if(svPrevFirstVisibleItem < firstVisibleItem)
        {
            //Scrolling down
            if(ibtnManageGroups.getVisibility() == View.VISIBLE)
                animateAddGroupButton(false);
        }
        if(svPrevFirstVisibleItem > firstVisibleItem)
        {
            //Scrolling up
            if(ibtnManageGroups.getVisibility() == View.GONE)
                animateAddGroupButton(true);
        }

        svPrevFirstVisibleItem=firstVisibleItem;
    }

    @Override
    public void onRefresh()
    {
        //Animate add button out
        if(ibtnManageGroups.getVisibility() == View.VISIBLE)
            animateAddGroupButton(false);

        findSubscribedGroups();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v.getId() == R.id.lstvGroups)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            /*LayoutInflater inflater = LayoutInflater.from(context);
            View header = inflater.inflate(R.layout.context_menu_header, null);
            TextView headerTitle = (TextView) header.findViewById(R.id.txtvContextMenuHeaderTitle);

            headerTitle.setText(capturedRoutes.get(info.position).getRouteName());
            menu.setHeaderView(header);*/
            menu.setHeaderTitle(lstAdapter.items.get(info.position).getString("name"));
            menu.add(Menu.NONE, 0, 0, "Leave");
//            menu.add(Menu.NONE, 1, 1, "Share");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        switch(menuItemIndex)
        {
            case 0:
                //Leave
                unSubUserFromGroup(lstAdapter.items.get(info.position));
                break;

            case 1:
                //Share
                Toast.makeText(context, "Coming soon", Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }

    private void findSubscribedGroups()
    {
        //Update user
        HomeActivity.currentUser.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if(e == null)
                {
                    List<String> groupNames = parseObject.getList("groups");
                    //Query for group objects, ps only ones the user is subbed to
                    ParseQuery<ParseObject> querySubbedGroups = new ParseQuery<ParseObject>("Groups");
                    querySubbedGroups.whereContainedIn("name", groupNames);
                    querySubbedGroups.findInBackground(new FindCallback<ParseObject>()
                    {
                        @Override
                        public void done(List<ParseObject> list, ParseException e)
                        {
                            if(e == null)
                            {
                                updateList(list);
                            }
                            else
                            {
                                if(e.getCode() == 100)
                                    Toast.makeText(getActivity(), R.string.error_100_no_internet, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getActivity(), "Unsuccessful retrieval of subscribed groups: " + e.getMessage() + " Code: " + e.getCode(), Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
                else
                {
                    srLayGroups.setRefreshing(false);
                    if(e.getCode() == 100)
                        Toast.makeText(getActivity(), R.string.error_100_no_internet, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), "Unsuccessful retrieval of subscribed groups: " + e.getMessage() + " Code: " + e.getCode(), Toast.LENGTH_LONG).show();

                }
            }
        });

        /*ParseQuery<ParseObject> findSubbedGroups = ParseQuery.getQuery("Groups");
        findSubbedGroups.whereEqualTo("subscriberObjects", HomeActivity.currentUser.getObjectId());

        final Toast msg = Toast.makeText(context, "", Toast.LENGTH_LONG);

//        progbGroupsSubbed.setVisibility(View.VISIBLE);
        findSubbedGroups.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e)
            {
                if(e == null)
                {
                    updateList(parseObjects);
//                    progbGroupsSubbed.setVisibility(View.GONE);
                    lstvSubbedGroups.setVisibility(View.VISIBLE);

                    srLayGroups.setRefreshing(false);
                }
                else
                {
//                    progbGroupsSubbed.setVisibility(View.GONE);
                    srLayGroups.setRefreshing(false);
                    if(e.getCode() == 100)
                        msg.setText(R.string.error_100_no_internet);
                    else
                        msg.setText("Unsuccessful retrieval of subscribed groups: " + e.getMessage() + " Code: " + e.getCode());

                    msg.show();
                }
            }
        });*/
    }

    private void animateAddGroupButton(boolean enter)
    {
        if(enter)
        {
            //Do enter animation
            Animation slideUp = (Animation) AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_bottom);

            ibtnManageGroups.startAnimation(slideUp);
            ibtnManageGroups.setVisibility(View.VISIBLE);
        }
        else
        {
            //Do Exit animation
            Animation slideDown = (Animation) AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom);

            ibtnManageGroups.startAnimation(slideDown);
            ibtnManageGroups.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        //Unsub user from group
        view.showContextMenu();

    }

    public void unSubUserFromGroup(ParseObject group)
    {
        //Unsub user from group
        List<String> userId = new ArrayList<String>();
        userId.add(HomeActivity.currentUser.getObjectId());
        group.increment("subscribers", -1);
        group.removeAll("subscriberObjects", userId);

        final Toast msg = Toast.makeText(context, "", Toast.LENGTH_LONG);

        lstvSubbedGroups.setVisibility(View.INVISIBLE);
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
                    lstvSubbedGroups.setVisibility(View.VISIBLE);
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
        HomeActivity.currentUser.removeAll("groups", groupNameAsId);

        HomeActivity.currentUser.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    findSubscribedGroups();
                    msg.setText("You have been successfully un subscribed");
                }
                else
                {
//                    progbGroupsSubbed.setVisibility(View.GONE);
                    lstvSubbedGroups.setVisibility(View.VISIBLE);

                    if(e.getCode() == 100)
                        msg.setText(R.string.error_100_no_internet);
                    else
                        msg.setText("Unsuccessful un subscribe group from user: " + e.getMessage() + " Code: " + e.getCode());
                }
            }
        });

        //sub to channel
        //Caps start of each word, uncaps every other
        String groupName = group.getString("name");
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

        Log.d(TAG, "Channel name to unsub from: " + channelName);
        ParsePush.unsubscribeInBackground(channelName);

        msg.show();
    }
}
