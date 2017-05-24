package za.co.oneohtwofour.brave;

/**
 * Created by wprenison on 2017/05/17.
 */

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.squareup.picasso.Picasso;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yayandroid.parallaxrecyclerview.ParallaxViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yahyabayramoglu on 14/04/15.
 */
public class AdapterGroups extends RecyclerView.Adapter<AdapterGroups.ViewHolder>
{

    private Context context;
    private LayoutInflater inflater;
    private ContextMenuDialogFragment fragContextMnu;
    private View.OnClickListener ctxMnuClickListener;

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
    private List<MenuObject> lstMenuObjs = new ArrayList<MenuObject>();

    public AdapterGroups(Context context, List<ParseObject> lstGroups, final android.support.v4.app.FragmentManager fragMang)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.lstGroups = lstGroups;

        //Create context menu objects ect here not in view holder for performance
        MenuObject mnuoClose = new MenuObject();
        mnuoClose.setResource(R.drawable.ic_close);
        mnuoClose.setBgColor(R.color.SeaGreen);
        mnuoClose.setScaleType(ImageView.ScaleType.CENTER);

        MenuObject mnuoReport = new MenuObject();
        mnuoReport.setResource(R.drawable.ic_close);
        mnuoReport.setTitle("Report"); //TODO: use string resource
        mnuoReport.setBgColor(R.color.SeaGreen);
        mnuoReport.setScaleType(ImageView.ScaleType.CENTER);

        MenuObject mnuoLeave = new MenuObject();
        mnuoLeave.setResource(R.drawable.ic_close);
        mnuoLeave.setTitle("Leave"); //TODO: use string resource
        mnuoLeave.setBgColor(R.color.SeaGreen);
        mnuoLeave.setScaleType(ImageView.ScaleType.CENTER);

        lstMenuObjs.add(mnuoClose);
        lstMenuObjs.add(mnuoReport);
        lstMenuObjs.add(mnuoLeave);

        final MenuParams menuParams = new MenuParams();
        menuParams.setMenuObjects(lstMenuObjs);
        menuParams.setClosableOutside(true);

        final OnMenuItemClickListener mnuItemClickListener = new OnMenuItemClickListener()
        {
            @Override
            public void onMenuItemClick(View clickedView, int position)
            {
                Snackbar.make(clickedView, "Eh an item was clicked on the ctx menu yay!", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        };

        ctxMnuClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Calculate where to locate menu from top
                int [] locInWindow = new int[2];
                view.getLocationInWindow(locInWindow);
                menuParams.setActionBarSize(locInWindow[1]);

                // set other settings to meet your needs
                fragContextMnu = ContextMenuDialogFragment.newInstance(menuParams);
                fragContextMnu.setItemClickListener(mnuItemClickListener);
                fragContextMnu.show(fragMang, "ctxMnuGroup");
            }
        };

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position)
    {
        return new ViewHolder(inflater.inflate(R.layout.list_item_group, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position)
    {
        ParseObject currGroup = lstGroups.get(position);

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


        Picasso.with(context).load(currGroup.getParseFile("imageFile").getUrl()).resize(600, 800).onlyScaleDown().centerCrop().into(viewHolder.getBackgroundImage());
        viewHolder.setGroupName(currGroup.getString("name"));
        viewHolder.setGroupRegion(currGroup.getString("country"));
        viewHolder.setGroupSize(currGroup.getInt("subscribers"));
        viewHolder.setGroupObjectId(currGroup.getObjectId());
        viewHolder.initOptions(ctxMnuClickListener);

        // # CAUTION:
        // Important to call this method
        viewHolder.getBackgroundImage().reuse();
    }

    @Override
    public int getItemCount()
    {
        return lstGroups.size();
    }

    /**
     * # CAUTION:
     * ViewHolder must extend from ParallaxViewHolder
     */
    public static class ViewHolder extends ParallaxViewHolder
    {
        private String groupDbId;
        private final TextView txtvGroupName, txtvGroupRegion, txtvGroupSize;
        private final FloatingActionButton fabOptions;

        public ViewHolder(View holderView)
        {
            super(holderView);

            fabOptions = (FloatingActionButton) holderView.findViewById(R.id.fabGroupOptions);
            txtvGroupName = (TextView) holderView.findViewById(R.id.txtvGroupName);
            txtvGroupRegion = (TextView) holderView.findViewById(R.id.txtvGroupRegion);
            txtvGroupSize = (TextView) holderView.findViewById(R.id.txtvGroupSize);
        }

        @Override
        public int getParallaxImageId()
        {
            return R.id.backgroundImage;
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

        public void setGroupObjectId(String objectId) //DB object id
        {
            groupDbId = objectId;
        }

        private void initOptions(View.OnClickListener clickListener)
        {
            fabOptions.setOnClickListener(clickListener);
        }

    }


}
