package io.flyingmongoose.brave.Dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.Fragment.FragGroups;
import io.flyingmongoose.brave.R;

/**
 * Created by wprenison on 2017/06/03.
 */

public class DiagGroupsPrivate extends DialogFragment
{
    private EditText etxtGroupCode;
    private Button btnJoin;
    private Button btnCancel;
    private SwipeRefreshLayout srLayGroupsPvt;
    private FragGroups fragGroups;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_dialog_groups_private, container, false);

        etxtGroupCode = (EditText) constructedView.findViewById(R.id.etxtGroupJoinPvtCode);
        btnJoin = (Button) constructedView.findViewById(R.id.btnGroupJoinPvt);
        btnCancel = (Button) constructedView.findViewById(R.id.btnGroupPvtCancel);
        srLayGroupsPvt = (SwipeRefreshLayout) constructedView.findViewById(R.id.srLayGroupsJoinPvt);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        fragGroups = (FragGroups) getFragmentManager().findFragmentByTag("fragGroups");

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Join pvt group
                String groupCode = etxtGroupCode.getText().toString().trim();

                //Try join group if it exists
                if(groupCode != null && !groupCode.isEmpty())
                {
                    srLayGroupsPvt.setRefreshing(true);

                    ParseQuery<ParseObject> findPrivateGroup = ParseQuery.getQuery("Groups");
                    findPrivateGroup.whereEqualTo("objectId", groupCode);

                    final Snackbar msg = Snackbar.make(etxtGroupCode, "", Snackbar.LENGTH_LONG);
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
                                    srLayGroupsPvt.setRefreshing(false);
                                }
                                else
                                    msg.setText("Unsuccessful private group search: " + e.getMessage() + " Code: " + e.getCode());

                                msg.show();
                            }
                        }
                    });
                }
                else
                    Snackbar.make(etxtGroupCode, "A group code is required", Snackbar.LENGTH_LONG).show();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        initSwipeRefresh();
    }

    private void initSwipeRefresh()
    {
        srLayGroupsPvt.setEnabled(false);
        srLayGroupsPvt.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayGroupsPvt.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
    }

    private void subscribeUserToGroup(ParseObject group)
    {
        final String groupName = group.getString("name");

        //Check if user does not already belong to group
        JSONArray groupSubbedUsers= group.getJSONArray("subscriberObjects");

        boolean userAlreadySubbed = false;
        for(int i = 0; i < groupSubbedUsers.length(); i++)
        {
            try
            {
                if(ActivHome.currentUser.getObjectId().equals(groupSubbedUsers.getString(i)))
                    userAlreadySubbed = true;
            }
            catch(JSONException je)
            {
                je.printStackTrace();
            }
        }

        if(!userAlreadySubbed)
        {
            //sub to group
            group.addUnique("subscriberObjects", ActivHome.currentUser.getObjectId());
            group.increment("subscribers");
            final Snackbar msg = Snackbar.make(fragGroups.getView(), "", Snackbar.LENGTH_SHORT);

            //        progbGroupsPrivate.setVisibility(View.VISIBLE);
            group.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e == null)
                    {

                    } else
                    {
                        //                    progbGroupsPrivate.setVisibility(View.INVISIBLE);
                        srLayGroupsPvt.setRefreshing(false);
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
                    srLayGroupsPvt.setRefreshing(false);
                    if(e == null)
                    {
                        msg.setText("Successfully subscribed to " + groupName);
                        dismiss();
                        fragGroups.findSubscribedGroups();
                    } else
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
            for(int i = 0; i < words.length; i++)
            {
                char[] wordLetters = words[i].toCharArray();
                wordLetters[0] = Character.toUpperCase(wordLetters[0]);

                //Uncaps the rest
                if(wordLetters.length > 1)
                {
                    for(int j = 1; j < wordLetters.length; j++)
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
        else
        {
            Snackbar.make(etxtGroupCode, "Your already a member of " + groupName, Snackbar.LENGTH_LONG).show();
            srLayGroupsPvt.setRefreshing(false);
        }
    }
}
