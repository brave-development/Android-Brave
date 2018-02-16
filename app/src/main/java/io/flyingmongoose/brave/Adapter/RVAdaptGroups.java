package io.flyingmongoose.brave.adapter;

/**
 * Created by wprenison on 2017/05/17.
 */

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.parse.ParseObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yayandroid.parallaxrecyclerview.ParallaxViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.fragment.FragGroups;
import io.flyingmongoose.brave.fragment.FragGroupsPublic;
import io.flyingmongoose.brave.R;

/**
 * Created by yahyabayramoglu on 14/04/15.
 */
public class RVAdaptGroups extends RecyclerView.Adapter<RVAdaptGroups.ViewHolder> implements Filterable
{

    private Context context;
    private FragGroups fragGroups;
    private FragGroupsPublic fragGroupsPublic;
    private LayoutInflater inflater;

    /*
    private int[] imageIds = new int[]{R.mipmap.test_image_1,
            R.mipmap.test_image_2, R.mipmap.test_image_3,
            R.mipmap.test_image_4, R.mipmap.test_image_5};
    */

    private String[] imageUrls = new String[]{
            "http://yayandroid.com/data/github_library/parallax_listview/test_image_1.jpg",
            "http://yayandroid.com/data/github_library/parallax_listview/test_image_2.jpg",
            "http://yayandroid.com/data/github_library/parallax_listview/test_image_3.png",
            "http://yayandroid.com/data/github_library/parallax_listview/test_image_4.jpg",
            "http://yayandroid.com/data/github_library/parallax_listview/test_image_5.png",
    };

    private List<ParseObject> lstGroups = new ArrayList<ParseObject>();
    private List<ParseObject> storedItems;

    public RVAdaptGroups(Context context, FragGroups fragGroups, FragGroupsPublic fragGroupsPublic, List<ParseObject> lstGroups, final android.support.v4.app.FragmentManager fragMang)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.lstGroups = lstGroups;
        this.storedItems = lstGroups;
        this.fragGroups = fragGroups;
        this.fragGroupsPublic = fragGroupsPublic;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position)
    {
        return new ViewHolder(inflater.inflate(R.layout.list_item_group, viewGroup, false));
    }

    public ParseObject getGroupData(int groupIndex)
    {
        return lstGroups.get(groupIndex);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position)
    {
        final ParseObject currGroup = lstGroups.get(position);

//        currGroup.getParseFile("imageFile").getDataInBackground(new GetDataCallback()
//        {
//            @Override
//            public void done(byte[] data, ParseException e)
//            {
//                if(e == null)
//                {
//                    viewHolder.getBackgroundImage().setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
//                } else
//                {
//                    viewHolder.getBackgroundImage().setImageResource(R.drawable.bg_group_row);
//                    e.printStackTrace();
//                }
//            }
//        }, new ProgressCallback()
//        {
//            @Override
//            public void done(Integer percentDone)
//            {
//                //TODO: show progress
//            }
//        });

        if(currGroup.has("imageFile"))
        {
            Picasso.with(context).load(currGroup.getParseFile("imageFile").getUrl()).resize(600, 800).onlyScaleDown().centerCrop().into(viewHolder.getBackgroundImage(), new Callback()
            {
                @Override
                public void onSuccess()
                {
                    viewHolder.bgDownloadResult(true, null);
                }

                @Override
                public void onError()
                {
                    viewHolder.bgDownloadResult(false, "Failed to load Image");
                }
            });
        }

        viewHolder.setData(currGroup);

        if(fragGroups == null)
        {
            viewHolder.setCanJoin(true, new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    fragGroupsPublic.subscribeUserToGroup(currGroup);
                }
            });
        } else
        {
            viewHolder.setCanJoin(false, null);

            viewHolder.setHolderClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    fragGroups.famGroups.close(true);
                }
            });
        }

        viewHolder.initOptions(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                BottomSheet.Builder sheetBuilder = new BottomSheet.Builder(context)
                    .dark()
                    .setStyle(R.style.MyBottomSheetStyle)
                    .setTitle(currGroup.getString("name"))
                    .setListener(new BottomSheetListener()
                    {
                        @Override
                        public void onSheetShown(@NonNull BottomSheet bottomSheet)
                        {

                        }

                        @Override
                        public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem)
                        {
                            bottomSheet.dismiss();
                            int mnuiId = menuItem.getItemId();

                            switch(mnuiId)
                            {
                                case R.id.mnuiShare:
                                    Intent intentShare = new Intent();
                                    intentShare.setAction(Intent.ACTION_SEND);

                                    if(currGroup.getBoolean("public"))
                                        intentShare.putExtra(Intent.EXTRA_TEXT, "Hey come join my group *" + currGroup.getString("name") + "*\n\n" +
                                                "Get Brave for\nAndroid: " + context.getString(R.string.appUrlANDROID) + "\niOS: " + context.getString(R.string.appUrlIOS));
                                    else
                                        intentShare.putExtra(Intent.EXTRA_TEXT, "Hey come join my private group using this code: *" + currGroup.getObjectId() + "*\n\n" +
                                                "Get Brave for\nAndroid: " + context.getString(R.string.appUrlANDROID) + "\niOS: " + context.getString(R.string.appUrlIOS));

                                    intentShare.setType("text/plain");
                                    context.startActivity(intentShare);
                                    break;

                                case R.id.mnuiJoin:
                                    fragGroupsPublic.subscribeUserToGroup(currGroup);
                                    break;

                                case R.id.mnuiEdit:
                                    fragGroups.showEditGroup(position);
                                    break;

                                case R.id.mnuiReport:
                                    Snackbar.make(view, currGroup.getString("name")  + " Reported", Snackbar.LENGTH_LONG).show();
                                    break;

                                case R.id.mnuiLeave:
                                    lstGroups.remove(currGroup);
                                    notifyDataSetChanged();
                                    fragGroups.unSubUserFromGroup(currGroup);
                                    break;
                            }
                        }

                        @Override
                        public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i)
                        {

                        }
                    });

                //Modify menu visible items as needed

                if(fragGroups == null)
                {
                    sheetBuilder.setSheet(R.menu.menu_group_item_searched);
                }
                else
                {
                    fragGroups.famGroups.close(true);

                    if(ActivHome.currentUser.getObjectId().equals(currGroup.getParseObject("admin").getObjectId()))
                        sheetBuilder.setSheet(R.menu.menu_group_item_owner);
                    else
                        sheetBuilder.setSheet(R.menu.menu_group_item_member);
                }

                BottomSheet sheetOptions = sheetBuilder.create();
                sheetOptions.show();
            }
        });

        // # CAUTION:
        // Important to call this method
        viewHolder.getBackgroundImage().reuse();
    }

    @Override
    public int getItemCount()
    {
        return lstGroups.size();
    }

    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults filterResults = new FilterResults();
                List<ParseObject> filteredItems = new ArrayList<ParseObject>();


                //Clean search term
                constraint = constraint.toString().trim().toLowerCase().replaceAll("\\s+", "");

                Log.i("New Filter", "constraint: %" + constraint + "%");

                if(constraint == null || constraint.length() == 0)
                {
                    //Return entire list
                    filterResults.count = storedItems.size();
                    filterResults.values = storedItems;
                }
                else
                {
                    //Perform search
                    for (int i = 0; i < storedItems.size(); i++)
                    {
                        if (storedItems.get(i).getString("flatValue").startsWith(constraint.toString()))
                            filteredItems.add(storedItems.get(i));
                    }

                    filterResults.count = filteredItems.size();
                    filterResults.values = filteredItems;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                lstGroups = (List<ParseObject>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
    }

    /**
     * # CAUTION:
     * ViewHolder must extend from ParallaxViewHolder
     */
    public static class ViewHolder extends ParallaxViewHolder
    {
        private final int animationRes = R.anim.anim_float_register;
        private final Animation animShake;
        private final ImageView imgvDownloadIndicator;
        private final View holderView;
        private final TextView txtvGroupName, txtvGroupRegion, txtvGroupSize;
        private final FloatingActionButton fabOptions;
        private final FloatingActionButton fabIsPrivate;
        private final Button btnJoin;

        public ViewHolder(View holderView)
        {
            super(holderView);

            this.holderView = holderView;
            fabOptions = (FloatingActionButton) holderView.findViewById(R.id.fabGroupOptions);
            txtvGroupName = (TextView) holderView.findViewById(R.id.txtvGroupName);
            txtvGroupRegion = (TextView) holderView.findViewById(R.id.txtvGroupRegion);
            txtvGroupSize = (TextView) holderView.findViewById(R.id.txtvGroupSize);
            fabIsPrivate = (FloatingActionButton) holderView.findViewById(R.id.fabGroupsIsPrivate);
            btnJoin = (Button) holderView.findViewById(R.id.btnGroupJoin);
            imgvDownloadIndicator = (ImageView) holderView.findViewById(R.id.imgvDownloadIndicator);
            fabIsPrivate.setEnabled(false);

            animShake = AnimationUtils.loadAnimation(holderView.getContext(), animationRes);
        }

        @Override
        public int getParallaxImageId()
        {
            return R.id.backgroundImage;
        }

        public void setData(ParseObject groupData)
        {
            //Set view data
            setGroupName(groupData.getString("name"));
            setGroupRegion(groupData.getString("country"));
            setGroupSize(groupData.getInt("subscribers"));
            setIsPrivate(!groupData.getBoolean("public"));

            imgvDownloadIndicator.startAnimation(animShake);
        }

        public void setHolderClickListener(View.OnClickListener clickListener)
        {
            holderView.setOnClickListener(clickListener);
        }

        public void setGroupName(String groupName)
        {
            txtvGroupName.setText(groupName);
        }

        public void setGroupRegion(String groupRegion)
        {
            txtvGroupRegion.setText(groupRegion);
        }

        public void setGroupSize(int groupSize)
        {
            txtvGroupSize.setText(groupSize + " Members"); //TODO: use string resource
        }

        private void initOptions(View.OnClickListener clickListener)
        {
            fabOptions.setOnClickListener(clickListener);
        }

        private void setIsPrivate(boolean isPrivate)
        {
            if(isPrivate)
                fabIsPrivate.setVisibility(View.VISIBLE);
            else
                fabIsPrivate.setVisibility(View.INVISIBLE);
        }

        private void setCanJoin(boolean canJoin, View.OnClickListener joinGroupListner)
        {
            if(canJoin)
            {
                btnJoin.setVisibility(View.VISIBLE);
                btnJoin.setOnClickListener(joinGroupListner);
            }
            else
            {
                btnJoin.setVisibility(View.INVISIBLE);
                btnJoin.setOnClickListener(joinGroupListner);
            }
        }

        private void bgDownloadResult(boolean success, String errorMsg)
        {
            imgvDownloadIndicator.clearAnimation();
            if(!success)
            {
                imgvDownloadIndicator.setImageResource(R.drawable.ic_download_failed);
                Snackbar.make(holderView, errorMsg, Snackbar.LENGTH_LONG).show();
            }
        }
    }


}
