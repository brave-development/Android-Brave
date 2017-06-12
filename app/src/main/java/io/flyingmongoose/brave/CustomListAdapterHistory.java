package io.flyingmongoose.brave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by IC on 6/16/2015.
 */
public class CustomListAdapterHistory extends ArrayAdapter
{
    Context context;
    int layRes;
    List<ParseObject> items;

    public CustomListAdapterHistory(Context context, int layRes, List<ParseObject> items)
    {
        super(context, layRes);

        this.context = context;
        this.layRes = layRes;
        this.items = items;
    }

    @Override
    public int getCount()
    {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View constructedView = LayoutInflater.from(context).inflate(layRes, parent, false);

        TextView txtvDate = (TextView) constructedView.findViewById(R.id.txtvListItemHistoryDate);
        TextView txtvStartTime = (TextView) constructedView.findViewById(R.id.txtvListItemHistoryStartTime);
        TextView txtvEndTime = (TextView) constructedView.findViewById(R.id.txtvListItemHistoryEndTime);
        TextView txtvDuration = (TextView) constructedView.findViewById(R.id.txtvListItemHistoryCalculatedDuration);

        ParseObject historyItem = items.get(position);
        Date createdAt = historyItem.getCreatedAt();
        Date updatedAt = historyItem.getUpdatedAt();

        txtvDate.setText(new SimpleDateFormat("dd MMMM yyyy").format(createdAt));
        txtvStartTime.setText(new SimpleDateFormat("HH:mm").format(createdAt));
        txtvEndTime.setText(new SimpleDateFormat("HH:mm").format(updatedAt));

        Date duration = new Date();
        duration.setTime(updatedAt.getTime() - createdAt.getTime());
        txtvDuration.setText(new SimpleDateFormat("dd:HH:mm").format(duration));

        return constructedView;
    }
}
