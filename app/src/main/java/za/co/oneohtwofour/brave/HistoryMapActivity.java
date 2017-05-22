package za.co.oneohtwofour.brave;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class HistoryMapActivity extends ActionBarActivity implements View.OnClickListener
{

    private MapView mvHistoryMap;
    private String name;
    private String panicId;
    private String panicDate;
    private String cellNumber;
    private boolean locationAvailable;
    private Double locationLat;
    private Double locationLon;
    private Button btnReportUser;
    private final String TAG = "HistoryMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_map);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mvHistoryMap = (MapView) findViewById(R.id.mvHistoryMap);
        mvHistoryMap.onCreate(savedInstanceState);

        Intent receivedIntent = getIntent();
        name = receivedIntent.getStringExtra("name");
        panicId = receivedIntent.getStringExtra("panicId");
        panicDate = receivedIntent.getStringExtra("panicDate");
        cellNumber = receivedIntent.getStringExtra("cellNumber");
        locationAvailable = receivedIntent.getBooleanExtra("locationAvailable", false);

        if(locationAvailable)
        {
            locationLat = receivedIntent.getDoubleExtra("locationLat", 0);
            locationLon = receivedIntent.getDoubleExtra("locationLon", 0);
        }
        else
        {
            Toast.makeText(this, "Unfortunately a location was never made available for this panic", Toast.LENGTH_LONG).show();
        }

        //Setup Map
        mvHistoryMap.onResume();

        try
        {
            MapsInitializer.initialize(this.getApplicationContext());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        mvHistoryMap.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                if(locationAvailable)
                {
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(locationLat, locationLon)));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLon), 12f));
                }
            }
        });

        TextView txtvName = (TextView) findViewById(R.id.txtvHistoryMapName);
        txtvName.setText(name);
        txtvName.setSelected(true);

        TextView txtvDate = (TextView) findViewById(R.id.txtvHistoryMapDate);
        txtvDate.setText(panicDate);
        txtvDate.setSelected(true);

        TextView txtvCellNumber = (TextView) findViewById(R.id.txtvHistoryMapNumber);
        txtvCellNumber.setText(cellNumber);
        txtvCellNumber.setSelected(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //Add report button if panic user's name differs form logged in user
        if(!name.equalsIgnoreCase(HomeActivity.currentUser.getString("name")))
        {
            btnReportUser = new Button(this);
            btnReportUser.setText("Report");
            btnReportUser.setTextColor(getResources().getColor(R.color.White));
            btnReportUser.setBackground(getResources().getDrawable(R.drawable.selector_btn_report_user));
            btnReportUser.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.report_user), null, null, null);
            btnReportUser.setOnClickListener(this);

            menu.add(Menu.NONE, 0, Menu.NONE, "Report").setActionView(btnReportUser).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);  //Add report button to action bar
        }

        return true;
    }

    @Override
    public void onClick(View v)
    {
        if(v == btnReportUser)
        {
            //Report user
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@panic-sec.org", null));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Report User");
            intent.putExtra(Intent.EXTRA_TEXT, "User being reported: \nPanic ID: " + panicId + "\nPanic Date: " + panicDate + "\nUser's Name: " + name + "\nUser's Number: " + cellNumber + "\n\nYour reason for reporting this user:\n\n");

            startActivity(Intent.createChooser(intent, "Report User"));
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        mvHistoryMap.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mvHistoryMap.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mvHistoryMap.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mvHistoryMap.onLowMemory();
    }
}
