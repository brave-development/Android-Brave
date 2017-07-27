package io.flyingmongoose.brave;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.clans.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by wprenison on 2017/05/29.
 */

public class FragmentGroupNew extends Fragment
{
    private HomeActivity activity;
    private Context context;
    private final FragmentGroupNew thisFrag = this;

    //Views
    private FloatingActionButton fabGroupBg;
    private FloatingActionButton fabPrivate;
    private ImageView imgvBg;
    private SwipeRefreshLayout srLayNewGroup;
    private EditText etxtGroupName;
    private EditText etxtGroupDescrition;
    private EditText etxtGroupShowBgError;
    private Button btnCancel;
    private Button btnFinish;

    //Vars
    private boolean groupPublic = true;
    private Uri groupimageUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_groups_new, container, false);

        //Get handle on views
        fabGroupBg = (FloatingActionButton) constructedView.findViewById(R.id.fabGroupBg);
        fabPrivate = (FloatingActionButton) constructedView.findViewById(R.id.fabGroupPrivate);
        imgvBg = (ImageView) constructedView.findViewById(R.id.imgvGroupBg);
        srLayNewGroup = (SwipeRefreshLayout) constructedView.findViewById(R.id.srLayNewGroup);
        etxtGroupName = (EditText) constructedView.findViewById(R.id.etxtGroupName);
        etxtGroupDescrition = (EditText) constructedView.findViewById(R.id.etxtGroupDescription);
        etxtGroupShowBgError = (EditText) constructedView.findViewById(R.id.etxtGroupShowBgError);
        btnCancel = (Button) constructedView.findViewById(R.id.btnNewGroupCancel);
        btnFinish = (Button) constructedView.findViewById(R.id.btnNewGroupFinish);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        activity = (HomeActivity) getActivity();
        context = getContext();

        initSwipeRefresh();

        fabGroupBg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Open image selector
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(100,120)
                        .setMinCropResultSize(1000, 1200)
                        .start(context, thisFrag);
            }
        });

        fabPrivate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(groupPublic)
                {
                    groupPublic = false;

                    //Swap image
                    fabPrivate.setImageResource(R.drawable.ic_lock_closed);

                    Snackbar.make(fabPrivate, "Group set to private", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    groupPublic = true;

                    //Swap image
                    fabPrivate.setImageResource(R.drawable.ic_lock_open);

                    Snackbar.make(fabPrivate, "Group set to public", BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }
        });

        etxtGroupName.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                etxtGroupDescrition.requestFocus();
                return true;
            }
        });

        etxtGroupDescrition.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etxtGroupDescrition.getWindowToken(), 0);
                return true;
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                HomeActivity.fragManager.popBackStack();
            }
        });

        //Check which way to init frag for edit or for new group
        if(getArguments().getBoolean("newGroup"))
            initNewGroup();
        else
            initEditGroup(getArguments().getInt("groupIndex"));
    }

    private void initSwipeRefresh()
    {
        srLayNewGroup.setEnabled(false);
        srLayNewGroup.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                srLayNewGroup.setRefreshing(true);
                srLayNewGroup.setRefreshing(false);
            }
        });
        srLayNewGroup.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayNewGroup.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayNewGroup.setProgressViewOffset(true, 0, 8);
    }

    private void initNewGroup()
    {
        btnFinish.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(validate())
                {
                    String groupName = etxtGroupName.getText().toString().trim();
                    final String groupDescription = etxtGroupDescrition.getText().toString().trim();

                    //Format group name
                    final String groupNameFlat = FormatHelper.formatGroupFlatName(groupName);
                    final String finalFormattedGroupName = FormatHelper.formatGroupName(groupName);
                    final String channelName = FormatHelper.formatChannelName(groupName);

                    srLayNewGroup.setRefreshing(true);

                    //Check already if exists then create if not
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
                                srLayNewGroup.setRefreshing(false);
                                etxtGroupName.setError("Group Name is already taken");   //tell user
                            } else
                            {
                                //Unsuccessful
                                //No obj found (Success)
                                //Check internet connection code 100
                                if (e.getCode() == 101)
                                    createGroup(finalFormattedGroupName, groupNameFlat, groupDescription, channelName, groupPublic, groupimageUri); //create group
                                else if (e.getCode() == 100)
                                {
                                    srLayNewGroup.setRefreshing(false);
                                    msg = "Check your internet connection and try again";
                                }
                                else
                                {
                                    srLayNewGroup.setRefreshing(false);
                                    msg = "Unsuccessful while checking if group name exists: " + e.getMessage() + " Code: " + e.getCode();
                                }
                            }

                            if (!msg.isEmpty())
                            {
                                Snackbar.make(btnFinish, msg, Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        btnFinish.performClick();
                                    }
                                }).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void initEditGroup(int groupIndex)
    {
        //Disable group name change not possible will confuse users
        etxtGroupName.setEnabled(false);

        //init other views with group data
        final FragmentGroups fragGroups = (FragmentGroups) HomeActivity.fragManager.findFragmentByTag("fragGroups");
        final ParseObject group = fragGroups.lstAdapter.getGroupData(groupIndex);

        Picasso.with(context).load(group.getParseFile("imageFile").getUrl()).resize(600, 800).onlyScaleDown().centerCrop().into(imgvBg);
        final Drawable drawInitBg = imgvBg.getDrawable();
        etxtGroupName.setText(group.getString("name"));
        etxtGroupName.setEnabled(false);
        etxtGroupDescrition.setText(group.getString("description"));

        if(!group.getBoolean("public"))
            fabPrivate.callOnClick();

        btnFinish.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(validate())
                {
                    final String groupDescription = etxtGroupDescrition.getText().toString().trim();

                    if(!group.getString("description").equals(groupDescription))
                        group.put("description", groupDescription);

                    if(!group.getBoolean("public") == groupPublic)
                        group.put("public", groupPublic);

                    srLayNewGroup.setRefreshing(true);

                    //Check if image upload is needed
                    if(!drawInitBg.equals(imgvBg.getDrawable()))
                    {
                        //Image Upload is needed
                        try
                        {
                            final ParseFile parseImageFile = new ParseFile(new File(new URI(groupimageUri.toString())));
                            parseImageFile.saveInBackground(new SaveCallback()
                            {
                                @Override
                                public void done(ParseException e)
                                {
                                    String msg = "";
                                    if(e == null)
                                    {
                                        group.remove("imageFile");
                                        group.put("imageFile", parseImageFile);
                                        group.saveInBackground(new SaveCallback()
                                        {
                                            @Override
                                            public void done(ParseException e)
                                            {
                                                String msg = "";
                                                if(e == null)
                                                {
                                                    srLayNewGroup.setRefreshing(false);
                                                    Snackbar.make(HomeActivity.txtvProfileName, "Group Updated", Snackbar.LENGTH_LONG).show();
                                                    HomeActivity.fragManager.popBackStack();
                                                } else
                                                {
                                                    //Unsuccessful
                                                    //Check internet connection code 100
                                                    if(e.getCode() == 100)
                                                    {
                                                        srLayNewGroup.setRefreshing(false);
                                                        msg = "Check your internet connection and try again";
                                                    } else
                                                    {
                                                        srLayNewGroup.setRefreshing(false);
                                                        msg = "Unsuccessful while updating group: " + e.getMessage() + " Code: " + e.getCode();
                                                    }
                                                }

                                                if(!msg.isEmpty())
                                                    Snackbar.make(btnFinish, msg, Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View view)
                                                        {
                                                            btnFinish.performClick();
                                                        }
                                                    }).show();
                                            }
                                        });
                                    } else
                                    {
                                        //Unsuccessful
                                        //Check internet connection code 100
                                        if(e.getCode() == 100)
                                        {
                                            srLayNewGroup.setRefreshing(false);
                                            msg = "Check your internet connection and try again";
                                        } else
                                        {
                                            srLayNewGroup.setRefreshing(false);
                                            msg = "Unsuccessful while updating group: " + e.getMessage() + " Code: " + e.getCode();
                                        }
                                    }

                                    if(!msg.isEmpty())
                                        Snackbar.make(btnFinish, msg, Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                btnFinish.performClick();
                                            }
                                        }).show();
                                }
                            });
                        }
                        catch(URISyntaxException se)
                        {
                            se.printStackTrace();
                        }
                    }
                    else
                    {
                        group.saveInBackground(new SaveCallback()
                        {
                            @Override
                            public void done(ParseException e)
                            {
                                String msg = "";
                                if(e == null)
                                {
                                    srLayNewGroup.setRefreshing(false);
                                    Snackbar.make(HomeActivity.txtvProfileName, "Group Updated", Snackbar.LENGTH_LONG).show();
                                    HomeActivity.fragManager.popBackStack();
                                } else
                                {
                                    //Unsuccessful
                                    //Check internet connection code 100
                                    if(e.getCode() == 100)
                                    {
                                        srLayNewGroup.setRefreshing(false);
                                        msg = "Check your internet connection and try again";
                                    } else
                                    {
                                        srLayNewGroup.setRefreshing(false);
                                        msg = "Unsuccessful while updating group: " + e.getMessage() + " Code: " + e.getCode();
                                    }
                                }

                                if(!msg.isEmpty())
                                {
                                    Snackbar.make(btnFinish, msg, Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            btnFinish.performClick();
                                        }
                                    }).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public boolean validate()
    {
        boolean valid = true;

        //Validate
        if(groupimageUri == null && imgvBg.getDrawable() == null)
        {
            valid = false;
            etxtGroupShowBgError.setError("Choose an image");
        }

        if(etxtGroupDescrition.getText().length() < 10)
        {
            valid = false;
            etxtGroupDescrition.setError("Must be 10 or more chracters");
        }

        if(etxtGroupName.getText().length() < 3)
        {
            valid = false;
            etxtGroupName.setError("Must be 3 or more characters");
        }

        return valid;
    }

    public void createGroup(final String groupName, String groupNameFlat, String groupDescription, final String channelName, final boolean groupPublic, Uri groupimageUri)
    {
        final ParseObject newGroup = new ParseObject("Groups");
        newGroup.put("name", groupName.trim());
        newGroup.put("flatValue", groupNameFlat.trim());     //Checks group name is unique
        newGroup.put("description", groupDescription);
        newGroup.put("public", groupPublic);
        newGroup.put("admin", HomeActivity.currentUser);
        newGroup.put("country", HomeActivity.currentUser.get("country"));
        newGroup.put("subscribers", 1);
        newGroup.add("subscriberObjects", HomeActivity.currentUser.getObjectId());


        //Handle group image upload
        try
        {
            final ParseFile groupImage = new ParseFile(new File(new URI(groupimageUri.toString())));
            groupImage.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e == null)
                    {
                        Snackbar.make(btnFinish, "Group image upload successful", Snackbar.LENGTH_LONG).show();

                        newGroup.put("imageFile", groupImage);

                        //Set ACL
                        ParseACL groupACL = new ParseACL();
                        groupACL.setPublicWriteAccess(true);
                        groupACL.setPublicReadAccess(true);

                        newGroup.setACL(groupACL);

                        final Snackbar msg = Snackbar.make(btnFinish, "", Snackbar.LENGTH_LONG);
                        newGroup.saveInBackground(new SaveCallback()
                        {
                            @Override
                            public void done(ParseException e)
                            {
                                if(e == null)
                                {
                                    //Successful
                                    srLayNewGroup.setRefreshing(false);

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
                                            } else
                                            {
                                                Log.i("Create New Group", "Channel sub failed: " + e.getMessage() + " Code: " + e.getCode());
                                            }
                                        }
                                    });


                                    msg.setText("Group Created");

                                    showShareGroupDialog(newGroup);

                                    HomeActivity.fragManager.popBackStack();
                                } else
                                {
                                    //Unsuccessful
                                    srLayNewGroup.setRefreshing(false);
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
                    else
                    {
                        srLayNewGroup.setRefreshing(false);

                        //Check that user has not canceled group creation whilst a request was being made otherwise a snackbar has no view to attach to
                        if(thisFrag.isVisible())
                        {
                            Snackbar snackMsg;

                            if(e.getCode() == 100)
                                snackMsg = Snackbar.make(btnFinish, "Check internet connection and try again", Snackbar.LENGTH_INDEFINITE);
                            else
                                snackMsg = Snackbar.make(btnFinish, "Unsuccessful group image upload: " + e.getMessage() + " Code:" + e.getCode(), Snackbar.LENGTH_INDEFINITE);

                            snackMsg.setAction("Retry", new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    btnFinish.performClick();
                                }
                            });
                            snackMsg.show();
                        }
                    }
                }
            });
        }
        catch(URISyntaxException se)
        {
            se.printStackTrace();
            Snackbar.make(btnFinish, "Something went wrong with your image, please try another", Snackbar.LENGTH_LONG).show();
        }
    }

    public void showShareGroupDialog(ParseObject group)
    {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage("Your group was set to private and is hidden from any public searches. Others can join using your unique group code: " + groupCode).setCancelable(false)
//                .setTitle("How can others join?")
//                .setNegativeButton("Got It", new DialogInterface.OnClickListener()
//                {
//                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
//                    {
//                        dialog.cancel();
//                    }
//                });
//        final AlertDialog alert = builder.create();
//        alert.show();

        FragmentDialogShareGroup diagShare = new FragmentDialogShareGroup();

        //Add arguments
        Bundle args = new Bundle();
        args.putBoolean("privateGroup", !group.getBoolean("public"));
        args.putString("groupCode", group.getObjectId());
        args.putString("groupName", group.getString("name"));

        diagShare.setArguments(args);

        diagShare.show(getFragmentManager(), "shareGroup");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                groupimageUri = result.getUri();
                imgvBg.setImageURI(groupimageUri);
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Snackbar.make(imgvBg, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
