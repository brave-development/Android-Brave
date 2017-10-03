package io.flyingmongoose.brave;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yayandroid.parallaxrecyclerview.ParallaxRecyclerView;

/**
 * Created by wprenison on 2017/09/15.
 */

public class FragmentProfile extends Fragment
{

    //Views
    private ImageView imgvProfileCover;
    private TextView txtvHelpedValue;
    private TextView txtvRequested;
    private TextView txtvMarked;
    private ParallaxRecyclerView prcvHistory;

    //Vars
    private HomeActivity homeActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_profile, container, false);

        //get handel on views
        imgvProfileCover = (ImageView) constructedView.findViewById(R.id.imgvProfileCover);
        txtvHelpedValue = (TextView) constructedView.findViewById(R.id.txtvProfileHelpedValue);
        txtvRequested = (TextView) constructedView.findViewById(R.id.txtvProfileRequestedValue);
        txtvMarked = (TextView) constructedView.findViewById(R.id.txtvProfileMarkedValue);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        homeActivity = (HomeActivity) getActivity();

        //Query for amount of "helped" so responses, then populate value


        //Query for amount of requests so alerts, then populate value

        //Query for amount of marked locations so drop pins, then populate value

        //Query user history, this includes all the above action, sorted by time stamp
    }
}
