package io.flyingmongoose.brave.Dialog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.Interface.OnRespondStatusChange;
import io.flyingmongoose.brave.R;

/**
 * Created by Acinite on 2018/01/31.
 */

public class DiagDetailWindow extends DialogFragment
{
    private final int REQ_PERM_CALL = 100;

    private TextView txtvDate;
    private TextView txtvName;
    public TextView txtvNoOfResponders;
    private TextView txtvDetails;
    private TextView txtvAddress;
    public TextView txtvIsResponding;

    private FloatingActionButton fabRespond;
    private FloatingActionButton fabCall;
    private FloatingActionButton fabChat;

    private DiagDetailWindow thisDiag;
    private boolean isResponding;
    private int noOfResponders;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.map_detail_window, container, false);

        //Get handle
        txtvDate = constructedView.findViewById(R.id.txtvDetailWindowDate);
        txtvName = constructedView.findViewById(R.id.txtvDetailWindowName);
        txtvNoOfResponders = constructedView.findViewById(R.id.txtvDetailWindowNoOfResponders);
        txtvDetails = constructedView.findViewById(R.id.txtvDetailWindowDetails);
        txtvAddress = constructedView.findViewById(R.id.txtvDetailWindowAddress);
        txtvIsResponding = constructedView.findViewById(R.id.txtvDetailWindowRespond);

        fabRespond = constructedView.findViewById(R.id.fabDetailWindowRespond);
        fabCall = constructedView.findViewById(R.id.fabDetailWindowCall);
        fabChat = constructedView.findViewById(R.id.fabDetailWindowChat);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final ActivHome activHome = (ActivHome) getActivity();

        //Init from bundle args data
        final Bundle args = getArguments();

        txtvDate.setText(args.getString("date"));
        txtvName.setText(args.getString("name"));
        noOfResponders = args.getInt("noOfResponders", 0);
        txtvNoOfResponders.setText("Responders: " + noOfResponders);
        txtvDetails.setText(args.getString("detail", "No Details"));
        isResponding = args.getBoolean("isResponding", false);

        //Set text description for respond button
        if(isResponding)
            txtvIsResponding.setText("Stop Responding");
        else
            txtvIsResponding.setText("Respond");

        //Set click actions
        fabRespond.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Add or remove person to respond list
                ActivHome.fragMap.respondToAlert(!isResponding, new OnRespondStatusChange()
                {
                    @Override
                    public void onRespondStatusUpdate(boolean responding)
                    {
                        if(responding)
                        {
                            isResponding = true;
                            txtvIsResponding.setText("Stop Responding");
                            fabRespond.setColorNormal(R.color.red500);

                            noOfResponders++;
                            txtvNoOfResponders.setText("Responders: " + noOfResponders);
                        }
                        else
                        {
                            isResponding = false;
                            txtvIsResponding.setText("Respond");
                            fabRespond.setColorNormal(R.color.SeaGreen);

                            noOfResponders--;
                            txtvNoOfResponders.setText("Responders: " + noOfResponders);
                        }
                    }
                });
            }
        });

        fabCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Check for permission
                //Check for run time permission
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                {
                    //Call
                    String uri = "tel:" + args.getString("cellNumber").trim();
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
                else
                {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQ_PERM_CALL);
                }

            }
        });

        fabChat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Open chat window
                activHome.dismissInfoWindow();
                activHome.showChat(ActivHome.fragMap.currInfoWindowPanicObj);
                dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == REQ_PERM_CALL)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                fabCall.performClick();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
