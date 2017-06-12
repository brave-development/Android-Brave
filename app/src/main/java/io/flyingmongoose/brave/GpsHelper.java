package io.flyingmongoose.brave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Created by !Aries! on 2015-07-17.
 */
public class GpsHelper
{

    private Context context;

    public GpsHelper(Context context)
    {
        this.context = context;
    }

    public void showDialog()
    {
        buildAlertMessageNoGps();
    }

    public void showDialog(String msg)
    {
        buildAlertMessageNoGps(msg);
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public boolean isGpsOn()
    {
        LocationManager locMang = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //Check if gps is enabled
        if(!locMang.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return false;
        else
            return true;
    }

    private void buildAlertMessageNoGps()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS is disabled, please enabled it.").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                context.startActivity((new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
            {
                dialog.dismiss();
            }

        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoGps(String msg)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                context.startActivity((new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
            {
                dialog.dismiss();
            }

        });

        final AlertDialog alert = builder.create();
        alert.show();
    }
}
