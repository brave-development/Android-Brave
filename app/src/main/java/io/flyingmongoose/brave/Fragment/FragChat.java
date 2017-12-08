package io.flyingmongoose.brave.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.util.ChatBot;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.github.bassaer.chatmessageview.model.IChatUser;

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
        final ChatUser responder = new ChatUser(ActivHome.currentUser);

        chatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //Build Msg
                Message msg = new Message.Builder()
                        .setUser(me)
                        .setRightMessage(true)
                        .setMessageText(chatView.getInputText())
                        .build();

                //Send to chat view
                chatView.send(msg);
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
