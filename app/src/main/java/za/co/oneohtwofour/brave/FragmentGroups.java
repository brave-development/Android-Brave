package za.co.oneohtwofour.brave;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.yayandroid.parallaxrecyclerview.ParallaxRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wprenison on 2017/05/17.
 */

public class FragmentGroups extends Fragment
{
    private final String LOG_TAG = "FragGroups";
    private final int groupLimit = 50;

    private Context context;
    private ParallaxRecyclerView pararvGroups;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_groups, container, false);

        //Get handle on views
        pararvGroups = (ParallaxRecyclerView) constructedView.findViewById(R.id.recyclerView);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();

        pararvGroups.setLayoutManager(new LinearLayoutManager(context));
        pararvGroups.setHasFixedSize(true);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Groups");
        query.setLimit(groupLimit);
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                if(e == null)
                {
                    pararvGroups.setAdapter(new AdapterGroups(context, objects));
                }
                else
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
