package io.flyingmongoose.brave.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import io.flyingmongoose.brave.R;

/**
 * Created by IC on 6/1/2015.
 */
public class LAdaptGroups extends ArrayAdapter implements Filterable
{

    Context context;
    int layRes;
    public List<ParseObject> items;
    List<ParseObject> storedItems;

    //Statistics vars
    int totPotentialPrivateResponders;
    int totPotentialPublicResponders;

    public LAdaptGroups(Context context, int layRes, List<ParseObject> items)
    {
        super(context, layRes, items);

        this.context = context;
        this.layRes = layRes;
        this.items = items;
        this.storedItems = items;
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

        View vStatus = constructedView.findViewById(R.id.vStatus);

        //Set number of poeple in the group
        TextView txtvNoOfUsersValue = (TextView) constructedView.findViewById(R.id.txtvNoOfUsersValue);
        int noOfUsers = items.get(position).getInt("subscribers");
        txtvNoOfUsersValue.setText(noOfUsers + "");

        //Set Color status
        boolean _public = items.get(position).getBoolean("public");
        if(_public)
        {
            vStatus.setBackgroundColor(context.getResources().getColor(R.color.SeaGreen));
            totPotentialPublicResponders += noOfUsers;
        }
        else
        {
            vStatus.setBackgroundColor(context.getResources().getColor(R.color.Blue));
            totPotentialPrivateResponders += noOfUsers;
        }

        TextView txtvListItemGroupName = (TextView) constructedView.findViewById(R.id.txtvListItemGroupName);
        txtvListItemGroupName.setText(items.get(position).getString("name"));

        TextView txtvListItemCountry = (TextView) constructedView.findViewById(R.id.txtvListItemCountry);
        txtvListItemCountry.setText(items.get(position).getString("country"));

        return constructedView;
    }

    public int getPrivateUsers() {return totPotentialPrivateResponders;}
    public int getPublicUsers() {return totPotentialPublicResponders;}
    public int getTotalUsers() {return  totPotentialPrivateResponders + totPotentialPublicResponders;}

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
                items = (List<ParseObject>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
    }
}
