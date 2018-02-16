package io.flyingmongoose.brave.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.adapter.LAdaptHistory;
import io.flyingmongoose.brave.activity.ActivHistoryMap;
import io.flyingmongoose.brave.R;

/**
 * Created by IC on 6/16/2015.
 */
public class FragHistoryYours extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener
{

    private ListView lstvHistoryYours;
    private LAdaptHistory lstAdapter;
//    private ProgressBar progbHistoryYours;
    private SwipeRefreshLayout srLayHistoryYours;
    private final int NO_OF_RECORDS = 20;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_history_yours, container, false);

        lstvHistoryYours = (ListView) view.findViewById(R.id.lstvHistoryYours);
        lstvHistoryYours.setOnItemClickListener(this);

//        progbHistoryYours = (ProgressBar) view.findViewById(R.id.progbHistoryYours);
        srLayHistoryYours = (SwipeRefreshLayout) view.findViewById(R.id.srLayHistoryYours);
        initSwipeRefresh();
        return view;
    }

    private void initSwipeRefresh()
    {
        srLayHistoryYours.setOnRefreshListener(this);
        srLayHistoryYours.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayHistoryYours.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayHistoryYours.setProgressViewOffset(true, 0, 130);
    }

    @Override
    public void onRefresh()
    {
        setYourHistory(NO_OF_RECORDS
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setYourHistory(NO_OF_RECORDS);
    }

    public void setYourHistory(int maxHistoryEntries)
    {
        ParseQuery<ParseObject> queryYourHistory = new ParseQuery<ParseObject>("Panics");
        queryYourHistory.whereEqualTo("user", ActivHome.currentUser);
        queryYourHistory.orderByDescending("createdAt");
        queryYourHistory.setLimit(maxHistoryEntries);


        lstvHistoryYours.setVisibility(View.GONE);
//        progbHistoryYours.setVisibility(View.VISIBLE);
        srLayHistoryYours.setRefreshing(true);
        queryYourHistory.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e)
            {

                if(e == null)
                {
                    Log.i("FragHistoryYours", "Results recieved: " + parseObjects.size());
                    lstAdapter = new LAdaptHistory(getActivity() , R.layout.list_item_history, parseObjects);
                    lstvHistoryYours.setAdapter(lstAdapter);

//                    progbHistoryYours.setVisibility(View.GONE);
                    srLayHistoryYours.setRefreshing(false);
                    lstvHistoryYours.setVisibility(View.VISIBLE);
                }
                else
                {
                    Log.e("FragHistoryYours", "Error while retrieving your history: " + e.getMessage() +  " Code: " + e.getCode());
                }
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        ParseObject historyPanic = lstAdapter.items.get(position);

        ParseGeoPoint location = historyPanic.getParseGeoPoint("location");
        Date panicDate = historyPanic.getCreatedAt();
        String name = ActivHome.currentUser.getString("name");
        String cellNumber = ActivHome.currentUser.getString("cellNumber");

        Intent displayHistoryPanicIntent = new Intent(getActivity(), ActivHistoryMap.class);

        if(location != null)
        {
            displayHistoryPanicIntent.putExtra("locationAvailable", true);
            displayHistoryPanicIntent.putExtra("locationLat", location.getLatitude());
            displayHistoryPanicIntent.putExtra("locationLon", location.getLongitude());
        }
        else
        {
            displayHistoryPanicIntent.putExtra("locationAvailable", false);
        }

        displayHistoryPanicIntent.putExtra("panicDate", new SimpleDateFormat("dd MMMM yyyy").format(panicDate));
        displayHistoryPanicIntent.putExtra("name", name);
        displayHistoryPanicIntent.putExtra("cellNumber", cellNumber);
        getActivity().startActivity(displayHistoryPanicIntent);
    }

}
