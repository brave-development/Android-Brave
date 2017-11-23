package io.flyingmongoose.brave.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.R;

/**
 * Created by IC on 1/27/2015.
 */
public class FragBottomActionBar extends Fragment implements View.OnClickListener {

    Button btnNavPanic;
    Button btnNavMap;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view  = inflater.inflate(R.layout.fragment_bottom_action_bar_layout, container, false);

        btnNavPanic = (Button) view.findViewById(R.id.btnNavPanic);
        btnNavMap = (Button) view.findViewById(R.id.btnNavMap);

        btnNavPanic.setOnClickListener(this);
        btnNavMap.setOnClickListener(this);

        Log.i("fragBottomActionBar", "On Create called");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    /*@Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(hidden)
        {
            btnNavPanic.setTextColor(getResources().getColor(R.color.SelectedWhite));
            btnNavMap.setTextColor(getResources().getColor(R.color.HintGrey));
        }
    }*/

    public void resetButtons()  //Resets highlighted buttons to start position, used for when nav from map straight to another screen then to home ie. skipping going back to panic screen first
    {
        btnNavPanic.setTextColor(getResources().getColor(R.color.SelectedWhite));
        btnNavMap.setTextColor(getResources().getColor(R.color.HintGrey));
    }

    @Override
    public void onClick(View v) {

        //Perform fragment and button highlight swapping according to users choice
        FragmentManager fragManager = ActivHome.fragManager;

        if(v == btnNavPanic)
        {
            if(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_MAP) != null)    //Check that fragMap is currently active
            {
                FragmentTransaction fragTransaction = fragManager.beginTransaction();
                fragTransaction.hide(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_MAP));
                fragTransaction.show(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_PANIC));
                fragTransaction.commit();

                btnNavPanic.setTextColor(getResources().getColor(R.color.SelectedWhite));
                btnNavMap.setTextColor(getResources().getColor(R.color.HintGrey));
            }
        }
        else if(v == btnNavMap)
        {
            Log.i("fragBottomActionBar", "Map button clicked");
            if(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_PANIC) != null)  //Check that fragPanic is currently active
            {
                //Check that the app is not panicing, else bottom action bar dissapeared before switching to map
                FragPanic fragPanic = (FragPanic) fragManager.findFragmentByTag(ActivHome.TAG_FRAG_PANIC);
                if(!fragPanic.isPanicing())
                {

                    FragmentTransaction fragTransaction = fragManager.beginTransaction();

                    if(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_MAP) == null)    //Check if map fragment has been added before TODO: only needed if map has not been loaded yet
                    {
                        ActivHome.fragMap = new FragMap();
                        fragTransaction.add(ActivHome.fragMap, ActivHome.TAG_FRAG_MAP);
                    } else
                        fragTransaction.show(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_MAP));

                    fragTransaction.hide(fragManager.findFragmentByTag(ActivHome.TAG_FRAG_PANIC));
                    fragTransaction.commit();

                    btnNavMap.setTextColor(getResources().getColor(R.color.SelectedWhite));
                    btnNavPanic.setTextColor(getResources().getColor(R.color.HintGrey));
                }
            }
        }
    }
}
