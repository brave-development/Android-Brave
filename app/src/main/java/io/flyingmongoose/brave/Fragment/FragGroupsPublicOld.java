package io.flyingmongoose.brave.Fragment;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.util.List;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.Adapter.LAdaptGroups;
import io.flyingmongoose.brave.R;

/**
 * Created by IC on 5/24/2015.
 */
public class FragGroupsPublicOld extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener
{
    private static ListView lstvSearchPublicGroupsResult;
//    private static ProgressBar progbGroupsPublic;
    private static TextView txtvSearchPublicDesc;
    private static LAdaptGroups lstAdapter;
    public static SwipeRefreshLayout srLaySearchPublicGroups;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tab_groups_public, container, false);

//        progbGroupsPublic = (ProgressBar) view.findViewById(R.id.progbGroupsPublic);
        srLaySearchPublicGroups = (SwipeRefreshLayout) view.findViewById(R.id.srLaySearchPublicGroups);
        initSwipeRefresh();
        txtvSearchPublicDesc  = (TextView) view.findViewById(R.id.txtvSearchPublicDesc);
        lstvSearchPublicGroupsResult = (ListView) view.findViewById(R.id.lstvSearchPublicGroupsResults);
        lstvSearchPublicGroupsResult.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onRefresh()
    {
//        FragManageGroups.onRefresh();
    }

    private void initSwipeRefresh()
    {
        srLaySearchPublicGroups.setEnabled(false);
        srLaySearchPublicGroups.setOnRefreshListener(this);
        srLaySearchPublicGroups.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLaySearchPublicGroups.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        setRefreshCircleYOffset();
    }

    private void setRefreshCircleYOffset()
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        srLaySearchPublicGroups.setProgressViewOffset(true, 0, height / 30);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    public static void loadingAnimate()
    {
        txtvSearchPublicDesc.setVisibility(View.GONE);
        lstvSearchPublicGroupsResult.setVisibility(View.GONE);
//        progbGroupsPublic.setVisibility(View.VISIBLE);
    }

    public static void populateResultList(Context context, List<ParseObject> results)
    {
        lstAdapter = new LAdaptGroups(context, R.layout.list_item_group_old, results);
        lstvSearchPublicGroupsResult.setAdapter(lstAdapter);
//        progbGroupsPublic.setVisibility(View.GONE);
        lstvSearchPublicGroupsResult.setVisibility(View.VISIBLE);
    }

    public static void filterResultList(CharSequence filterTerm)
    {
        if(lstAdapter != null)
            lstAdapter.getFilter().filter(filterTerm);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        //Get selected group
        ParseObject selectedGroup = lstAdapter.items.get(position);

        //Check if user is already subbed
        boolean isSubscribed = false;

        List<String> subbedGroups = ActivHome.currentUser.getList("groups");

        if(subbedGroups != null)
        {
            for (int i = 0; i < subbedGroups.size(); i++)
                if (subbedGroups.get(i).equals(selectedGroup.getString("name")))
                    isSubscribed = true;
        }

        if(!isSubscribed)
        {
            //Subscribe user to group
            subscribeUserToGroup(selectedGroup);
        }
        else
            Toast.makeText(context, "User already subscribed to this group", Toast.LENGTH_LONG).show();
    }

    private void subscribeUserToGroup(ParseObject group)
    {
        final String groupName = group.getString("name");

        //sub to group
        group.addUnique("subscriberObjects", ActivHome.currentUser.getObjectId());
        group.increment("subscribers");
        final Toast msg = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        lstvSearchPublicGroupsResult.setVisibility(View.GONE);
//        progbGroupsPublic.setVisibility(View.VISIBLE);
        srLaySearchPublicGroups.setRefreshing(true);
        group.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {

                }
                else
                {
//                    progbGroupsPublic.setVisibility(View.GONE);
                    srLaySearchPublicGroups.setRefreshing(false);
                    lstvSearchPublicGroupsResult.setVisibility(View.VISIBLE);

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
                if(e == null)
                {
//                    progbGroupsPublic.setVisibility(View.GONE);
                    srLaySearchPublicGroups.setRefreshing(false);
                    lstvSearchPublicGroupsResult.setVisibility(View.VISIBLE);

                    msg.setText("Successfully subscribed to " + groupName);
                    ActivHome.fragManager.popBackStack();
                }
                else
                {
                    srLaySearchPublicGroups.setRefreshing(false);
                    lstvSearchPublicGroupsResult.setVisibility(View.VISIBLE);
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
