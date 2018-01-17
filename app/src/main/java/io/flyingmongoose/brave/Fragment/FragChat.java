package io.flyingmongoose.brave.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.model.ChatUser;

/**
 * Created by Acinite on 2017/12/07.
 */

public class FragChat extends Fragment
{

    @BindView(R.id.chat_view) ChatView chatView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_chat, container, false);

        ButterKnife.bind(this, constructedView);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        //Set local users
        final ChatUser me = new ChatUser(ActivHome.currentUser);

        ParseUser parseUserResponder = new ParseUser();
        parseUserResponder.setObjectId("DavidMundell");
        parseUserResponder.put("name", "David Mundell");

        final ChatUser responder = new ChatUser(parseUserResponder);

        chatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //Build Msg
                Message msg = new Message.Builder()
                        .setUser(me)
                        .setRightMessage(true)
                        .setMessageText(chatView.getInputText())
                        .setCreatedAt(Calendar.getInstance())
                        .setUsernameVisibility(true)
                        .build();

                //Send to chat view and pesist to db
                chatView.send(msg);
                ParseObject parseMsg = new ParseObject("Messages");
                parseMsg.put("user", ActivHome.currentUser);
                parseMsg.put("alert", FragPanic.panicObj); 
                parseMsg.put("text", chatView.getInputText());
                parseMsg.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if(e == null)
                        {
                            Toast.makeText(getContext(), "Msg sent and saved to db successfully", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Msg sent and saved to db failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                chatView.setInputText("");

                //Receive message
                final Message receivedMessage = new Message.Builder()
                        .setUser(responder)
                        .setRightMessage(false)
                        .setMessageText(msg.getMessageText())
                        .build();

                // This is a demo bot
                // Return within 3 seconds
                int sendDelay = (new Random().nextInt(4) + 1) * 1000;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatView.receive(receivedMessage);
                    }
                }, sendDelay);
            }
        });
    }
}
