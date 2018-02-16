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
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.flyingmongoose.brave.adapter.LAdaptHistory;
import io.flyingmongoose.brave.activity.ActivHistoryMap;
import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.R;

/**
 * Created by IC on 6/16/2015.
 */
public class FragHistoryOthers extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener
{

    private ListView lstvHistoryOthers;
    private LAdaptHistory lstAdapter;
//    private ProgressBar progbHistoryOthers;
    private SwipeRefreshLayout srLayHistoryOthers;
    private final int NO_OF_RECORDS = 50;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view  = inflater.inflate(R.layout.fragment_history_others, container, false);

        lstvHistoryOthers = (ListView) view.findViewById(R.id.lstvHistoryOthers);
        lstvHistoryOthers.setOnItemClickListener(this);

//        progbHistoryOthers = (ProgressBar) view.findViewById(R.id.progbHistoryOthers);
        srLayHistoryOthers = (SwipeRefreshLayout) view.findViewById(R.id.srLayHistoryOthers);
        initSwipeRefresh();
        return view;
    }

    private void initSwipeRefresh()
    {
        srLayHistoryOthers.setOnRefreshListener(this);
        srLayHistoryOthers.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayHistoryOthers.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayHistoryOthers.setProgressViewOffset(true, 0, 130);
    }

    @Override
    public void onRefresh()
    {
        setOthersHistory(NO_OF_RECORDS);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        setOthersHistory(NO_OF_RECORDS);
    }

    public void setOthersHistory(int maxHistoryEntries)
    {
        ParseQuery<ParseObject> queryOthersHistory = new ParseQuery<ParseObject>("Panics");
        queryOthersHistory.whereNotEqualTo("user", ActivHome.currentUser);
        queryOthersHistory.orderByDescending("createdAt");
        queryOthersHistory.setLimit(maxHistoryEntries);

        lstvHistoryOthers.setVisibility(View.GONE);
//        progbHistoryOthers.setVisibility(View.VISIBLE);
        srLayHistoryOthers.setRefreshing(true);
        queryOthersHistory.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e)
            {

                if(e == null)
                {
                    Log.i("FragHistoryOthers", "Results received: " + parseObjects.size());
                    lstAdapter = new LAdaptHistory(getActivity(), R.layout.list_item_history, parseObjects);
                    lstvHistoryOthers.setAdapter(lstAdapter);

//                    progbHistoryOthers.setVisibility(View.GONE);
                    srLayHistoryOthers.setRefreshing(false);
                    lstvHistoryOthers.setVisibility(View.VISIBLE);
                }
                else
                {
                    Log.e("FragHistory", "Error while retrieving others hostory: " + e.getMessage() + " Code: " + e.getCode());
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        final ParseObject historyPanic = lstAdapter.items.get(position);
        ParseUser user = historyPanic.getParseUser("user");

        user.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject user, ParseException e)
            {
                if(e == null)
                {
                    ParseGeoPoint location = historyPanic.getParseGeoPoint("location");
                    Date panicDate = historyPanic.getCreatedAt();
                    String panicId = historyPanic.getObjectId();
                    String name = user.getString("name");
                    String cellNumber = user.getString("cellNumber");

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

                    displayHistoryPanicIntent.putExtra("panicId", panicId);
                    displayHistoryPanicIntent.putExtra("panicDate", new SimpleDateFormat("dd MMMM yyyy").format(panicDate));
                    displayHistoryPanicIntent.putExtra("name", name);
                    displayHistoryPanicIntent.putExtra("cellNumber", cellNumber);
                    getActivity().startActivity(displayHistoryPanicIntent);
                }
                else
                {
                    if(e.getCode() == 101)
                        Toast.makeText(getActivity(), "This panic unfortunately can't be view at this time, because the user's account has been removed from our system", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), "This panic unfortunately can't be view at this time, because: " + e.getMessage() + " Code: " + e.getCode(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
