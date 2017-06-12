package io.flyingmongoose.brave;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
//import com.parse.codec.binary.StringUtils;


/**
 * Created by IC on 5/24/2015.
 */
public class FragmentGroupsNewOld extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener
{
    private Activity activity;
    private Context context;
    private ImageButton ibtnHelpDiscoverableGroup;
    private TextView txtvDiscoverableStateDesc;
    private ImageButton ibtnCreateGroup;
    private Switch sDiscoverableGroup;
//    private ProgressBar progbGroupsNew;
    private SwipeRefreshLayout srLayGroupsNew;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tab_groups_new, container, false);

        ibtnHelpDiscoverableGroup = (ImageButton) view.findViewById(R.id.ibtnHelpDiscoverableGroup);
        ibtnHelpDiscoverableGroup.setOnClickListener(this);

        txtvDiscoverableStateDesc = (TextView) view.findViewById(R.id.txtvDiscoverableStateDesc);

        ibtnCreateGroup = (ImageButton) view.findViewById(R.id.ibtnCreateGroup);
        ibtnCreateGroup.setOnClickListener(this);

        sDiscoverableGroup = (Switch) view.findViewById(R.id.sDiscoverableGroup);
//        progbGroupsNew = (ProgressBar) view.findViewById(R.id.progbGroupsNew);
        srLayGroupsNew = (SwipeRefreshLayout) view.findViewById(R.id.srLayGroupsNew);
        initSwipeRefresh();


        return view;
    }

    private void initSwipeRefresh()
    {
        srLayGroupsNew.setEnabled(false);
        srLayGroupsNew.setOnRefreshListener(this);
        srLayGroupsNew.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayGroupsNew.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayGroupsNew.setProgressViewOffset(true, 0, 8);
    }

    @Override
    public void onRefresh()
    {
        srLayGroupsNew.setRefreshing(true);
        srLayGroupsNew.setRefreshing(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        context = getActivity();
    }

    public void createGroup(String groupName, String groupNameFlat, final String channelName,  boolean groupPublic)
    {
        final ParseObject newGroup = new ParseObject("Groups");
        newGroup.put("name", groupName.trim());
        newGroup.put("flatValue", groupNameFlat.trim());     //Checks group name is unique
        newGroup.put("public", !groupPublic);         //Invert boolean
        newGroup.put("admin", HomeActivity.currentUser);
        newGroup.put("country", HomeActivity.currentUser.get("country"));
        newGroup.put("subscribers", 1);
        newGroup.add("subscriberObjects", HomeActivity.currentUser.getObjectId());

        //Set ACL
        ParseACL groupACL = new ParseACL();
        groupACL.setPublicWriteAccess(true);
        groupACL.setPublicReadAccess(true);

        newGroup.setACL(groupACL);

        final Toast msg = Toast.makeText(context, "", Toast.LENGTH_LONG);

//        progbGroupsNew.setVisibility(View.VISIBLE);
        newGroup.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    //Successful
//                    progbGroupsNew.setVisibility(View.GONE);

                    //Insert group ref in user record
                    HomeActivity.currentUser.add("groups", newGroup.getString("name"));
                    HomeActivity.currentUser.saveInBackground();

                    Log.i("Create New Group", "Channel Name:" + channelName);

                    //Sub installation to push channel
                    ParsePush.subscribeInBackground(channelName, new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if(e == null)
                            {
                                Log.i("Creating New Group", "Channel sub successful");
                            }
                            else
                            {
                                Log.i("Create New Group", "Channel sub failed: " + e.getMessage() + " Code: " + e.getCode());
                            }
                        }
                    });


                    msg.setText("Group Created");
                    srLayGroupsNew.setRefreshing(false);
                    HomeActivity.fragManager.popBackStack();
                }
                else
                {
                    //Unsuccessful
                    //Check internet connection code 100
                    if(e.getCode() == 100)
                        msg.setText("Check internet connection and try again");
                    else
                        msg.setText("Unsuccessful group creation: " + e.getMessage() + " Code:" + e.getCode());
                }

                msg.show();
            }
        });

    }

    public void helpDiscoverableGroups()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Setting the group to private will hide it from public searches and require you to enter the name into the 'Private' tab to join.").setCancelable(false)
                .setTitle("What do you mean private?")
                .setNegativeButton("Got It", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v)
    {
        if(v == ibtnHelpDiscoverableGroup)
        {

            helpDiscoverableGroups();

            /*if(txtvDiscoverableStateDesc.getVisibility() == View.VISIBLE)
            {
                txtvDiscoverableStateDesc.setVisibility(View.INVISIBLE);
                ibtnHelpDiscoverableGroup.setImageResource(R.drawable.btn_info_inactive);
            }
            else
            {
                txtvDiscoverableStateDesc.setVisibility(View.VISIBLE);
                ibtnHelpDiscoverableGroup.setImageResource(R.drawable.btn_info_active);
            }*/
        }
        else if(v == ibtnCreateGroup)
        {
            if(true)
            {
                //Get values
                final String groupName = FragmentManageGroups.svGroups.getQuery().toString();

                if(!groupName.isEmpty())
                {
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
                    final String groupNameFlat = channelName.toLowerCase().toString();   //Process group name to flat value
                    final boolean groupPublic = sDiscoverableGroup.isChecked();

                    if (groupNameFlat.length() > 2)
                    {
                        //TODO: validate values
                        srLayGroupsNew.setRefreshing(true);
                        ParseQuery<ParseObject> queryGroupExists = ParseQuery.getQuery("Groups");
                        queryGroupExists.whereEqualTo("flatValue", groupNameFlat);
                        queryGroupExists.getFirstInBackground(new GetCallback<ParseObject>()
                        {
                            @Override
                            public void done(ParseObject object, ParseException e)
                            {
                                String msg = "";
                                if (e == null)
                                {
                                    //An obj was found
                                    msg = "Group Name is already taken";    //tell user
                                    srLayGroupsNew.setRefreshing(false);
                                } else
                                {
                                    //Unsuccessful
                                    //No obj found (Success)
                                    //Check internet connection code 100
                                    if (e.getCode() == 101)
                                        createGroup(finalFormattedGroupName, groupNameFlat, channelName, groupPublic); //create group
                                    else if (e.getCode() == 100)
                                    {
                                        msg = "Check your internet connection and try again";
                                        srLayGroupsNew.setRefreshing(false);
                                    }
                                    else
                                    {
                                        msg = "Unsuccessful while checking if group name exists: " + e.getMessage() + " Code: " + e.getCode();
                                        srLayGroupsNew.setRefreshing(false);
                                    }
                                }

                                if (!msg.isEmpty())
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else
                        Toast.makeText(context, "Groups require 3 or more characters in their name", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(context, "A group name is required", Toast.LENGTH_LONG).show();
            }
        }
    }

//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//    {
//        if(isChecked)
//            txtvDiscoverableState.setText("Private");
//        else
//            txtvDiscoverableState.setText("Public");
//    }
}
