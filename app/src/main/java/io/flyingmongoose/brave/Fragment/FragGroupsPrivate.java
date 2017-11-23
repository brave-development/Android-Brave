package io.flyingmongoose.brave.Fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.R;

/**
 * Created by IC on 5/24/2015.
 */
public class FragGroupsPrivate extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener
{
    private Context context;
    private ImageButton ibtnGroupPrivateJoin;
//    private ProgressBar progbGroupsPrivate;
    private SwipeRefreshLayout srLayGroupsPrivate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tab_groups_private, container, false);

        ibtnGroupPrivateJoin = (ImageButton) view.findViewById(R.id.ibtnGroupPrivateJoin);
        ibtnGroupPrivateJoin.setOnClickListener(this);

        srLayGroupsPrivate = (SwipeRefreshLayout) view.findViewById(R.id.srLayGroupsPrivate);
        initSwipeRefresh();

//        progbGroupsPrivate = (ProgressBar) view.findViewById(R.id.progbGroupsPrivate);

        return view;
    }

    private void initSwipeRefresh()
    {
        srLayGroupsPrivate.setEnabled(false);
        srLayGroupsPrivate.setOnRefreshListener(this);
        srLayGroupsPrivate.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayGroupsPrivate.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayGroupsPrivate.setProgressViewOffset(true, 0, 8);
    }

    @Override
    public void onRefresh()
    {
        srLayGroupsPrivate.setRefreshing(true);
        srLayGroupsPrivate.setRefreshing(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void onClick(View v)
    {
        if(v == ibtnGroupPrivateJoin)
        {
            String groupName = FragManageGroups.svGroups.getQuery().toString();

            if(groupName != null || !groupName.isEmpty())
            {
                srLayGroupsPrivate.setRefreshing(true);
                //flatten group name
                String flatGroupName = groupName.replaceAll("\\s+", "").trim().toLowerCase().toString();

                ParseQuery<ParseObject> findPrivateGroup = ParseQuery.getQuery("Groups");
                findPrivateGroup.whereEqualTo("flatValue", flatGroupName);

                final Toast msg = Toast.makeText(context, "", Toast.LENGTH_LONG);
                findPrivateGroup.getFirstInBackground(new GetCallback<ParseObject>()
                {
                    @Override
                    public void done(ParseObject parseObject, ParseException e)
                    {
                        if (e == null)
                        {
                            subscribeUserToGroup(parseObject);
                        } else
                        {
                            //No results code 101
                            if (e.getCode() == 101)
                            {
                                msg.setText("Group not found");
                                srLayGroupsPrivate.setRefreshing(false);
                            }
                            else
                                msg.setText("Unsuccessful private group search: " + e.getMessage() + " Code: " + e.getCode());

                            msg.show();
                        }
                    }
                });
            }
            else
                Toast.makeText(context, "A group name is required", Toast.LENGTH_LONG);
        }
    }

    private void subscribeUserToGroup(ParseObject group)
    {
        final String groupName = group.getString("name");

        //sub to group
        group.addUnique("subscriberObjects", ActivHome.currentUser.getObjectId());
        group.increment("subscribers", 1);
        final Toast msg = Toast.makeText(context, "", Toast.LENGTH_SHORT);

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
//                    progbGroupsPrivate.setVisibility(View.INVISIBLE);
                    srLayGroupsPrivate.setRefreshing(false);
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
                srLayGroupsPrivate.setRefreshing(false);
                if (e == null)
                {
                    msg.setText("Successfully subscribed to " + groupName);
                    ActivHome.fragManager.popBackStack();
                }
                else
                {
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
