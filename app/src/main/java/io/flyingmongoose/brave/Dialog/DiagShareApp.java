package io.flyingmongoose.brave.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.R;

/**
 * Created by wprenison on 2017/07/06.
 */

public class DiagShareApp extends DialogFragment
{

    private Button btnShareFb;
    private Button btnShareWA;
    private Button btnShareTw;
    private Button btnShareOther;
    private Button btnShareSkip;
    private TextView txtvTitle;
    private TextView txtvMsg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.dialog_share_group, container, false);

        btnShareFb = (Button) constructedView.findViewById(R.id.diagShareGroupBtnFacebook);
        btnShareWA = (Button) constructedView.findViewById(R.id.diagShareGroupBtnWhatsapp);
        btnShareTw = (Button) constructedView.findViewById(R.id.diagShareGroupBtnTwitter);
        btnShareOther = (Button) constructedView.findViewById(R.id.diagShareGroupBtnOther);
        btnShareSkip = (Button) constructedView.findViewById(R.id.diagShareGroupBtnNoThanks);
        txtvTitle = (TextView) constructedView.findViewById(R.id.diagShareGrouptxtvTitle);
        txtvMsg = (TextView) constructedView.findViewById(R.id.diagShareGrouptxtvMsg);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Init dialog

        //Set title
        txtvTitle.setText(getString(R.string.diagShareAppTitle));

        //Set msg
        String msg = getString(R.string.diagShareAppMsg);
        txtvMsg.setText(msg);

        //String build share msg
        final String shareMsg = ActivHome.currentUser.get("name") + " " + getString(R.string.diagShareAppShareMsg) + "\n" + getString(R.string.appSiteUrl);

        copyToClipboard(shareMsg);

        //Set click listeners
        btnShareFb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ShareDialog shareDialog = new ShareDialog(getActivity());

                //Create link content
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(getString(R.string.appUrlANDROID)))
                        .setQuote(shareMsg).build();

                shareDialog.show(linkContent);
                dismiss();
            }
        });

        btnShareWA.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
                try
                {
                    getActivity().startActivity(whatsappIntent);
                    dismiss();
                }
                catch (android.content.ActivityNotFoundException ex)
                {
                    Snackbar.make(btnShareSkip, getString(R.string.diagShareGroupWANotInstalled), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        btnShareTw.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Uri imageUri = Uri.parse("android.resource://io.flyingmongoose.brave/drawable/ic_share");

                TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                        .text(shareMsg)
                        .image(imageUri);
                builder.show();
                dismiss();
            }
        });

        btnShareOther.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent otherIntent = new Intent(Intent.ACTION_SEND);
                otherIntent.setType("text/plain");
                otherIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
                getActivity().startActivity(otherIntent);
                dismiss();
            }
        });

        btnShareSkip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });
    }

    private void copyToClipboard(String msg)
    {
        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("Brave Share Group", msg);

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);

        Snackbar.make(btnShareSkip, "Group Copied to clipboard", Snackbar.LENGTH_SHORT).show();
    }
}
