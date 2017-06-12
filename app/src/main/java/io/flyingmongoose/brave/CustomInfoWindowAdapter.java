package io.flyingmongoose.brave;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseObject;

import java.util.Map;

/**
 * Created by !Aries! on 2015-06-26.
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
{
    private String TAG = "CustomeInfoWindowAdapter";
    private Context context;
    private Map<String, ParseObject> panicObjs;

    public CustomInfoWindowAdapter(Context context, Map<String, ParseObject> panicObjs)
    {
        this.context = context;
        this.panicObjs = panicObjs;
        Log.i(TAG, "CustomInfoWindowAdpater Created");
    }

    @Override
    public View getInfoWindow(Marker marker)
    {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker)
    {
        View constuctedView = LayoutInflater.from(context).inflate(R.layout.map_info_window, null);

        TextView txtvName = (TextView) constuctedView.findViewById(R.id.txtvMapInfoWindowName);
        TextView txtvCellNumber = (TextView) constuctedView.findViewById(R.id.txtvMapInfoWindowCellNumber);
        TextView txtvDesc = (TextView) constuctedView.findViewById(R.id.txtvMapInfoWindowDesc);

        txtvName.setText(panicObjs.get(marker.getId()).getParseUser("user").getString("name"));
        txtvCellNumber.setText(panicObjs.get(marker.getId()).getParseUser("user").getString("cellNumber"));

        //Hide details txtv if there are no details
        String details = panicObjs.get(marker.getId()).getString("details");

        if(details != null)
            if(!details.isEmpty())
                txtvDesc.setText(details);


        Log.i(TAG, "Info window constructed");
        return constuctedView;
    }
}
