package io.flyingmongoose.brave.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.Event.EvtNewChatMsg;
import io.flyingmongoose.brave.Interface.ParseApiInterface;
import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.Util.UtilMsgStatus;
import io.flyingmongoose.brave.Util.UtilParseAPI;
import io.flyingmongoose.brave.model.ChatUser;

/**
 * Created by Acinite on 2017/12/07.
 */

public class FragChat extends Fragment
{

    private final String TAG = "FragChat";

    private ParseObject chatAlert;
    private ChatUser cuMe = null;
    private HashMap<String, ChatUser> hMapChatUsers = new HashMap<>();

    private int profileCirclesColors [];
    private int currColorIndex = 0;

    private UtilMsgStatus utilMsgStatus;

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
    public void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        initChatColors();

        //Init this user's chat user obj
        cuMe = new ChatUser(getContext(),  ActivHome.currentUser, profileCirclesColors[currColorIndex]);
        incColorIndex();

        //Get alert this chat should be generated for
        chatAlert = ActivHome.lhMapActiveAlertChats.get(getArguments().getString("alertId"));

        initChatview();

        initSendMsg();

        //Pull msgs
        UtilParseAPI.getChatMsgs(chatAlert, new ParseApiInterface()
        {
            @Override
            public void onLoadingStatusChanged(boolean loading)
            {

            }

            @Override
            public void onParseObjectListResponse(List<ParseObject> listObjects)
            {
                initChat(listObjects);
            }
        });

//                //Receive message
//                final Message receivedMessage = new Message.Builder()
//                        .setUser(responder)
//                        .setRightMessage(false)
//                        .setMessageText(msg.getMessageText())
//                        .build();
//
//                // This is a demo bot
//                // Return within 3 seconds
//                int sendDelay = (new Random().nextInt(4) + 1) * 1000;
//                new Handler().postDelayed(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        chatView.receive(receivedMessage);
//                    }
//                }, sendDelay);
//            }
//        });
    }

    private void initChatColors()
    {
        profileCirclesColors = new int[] {getResources().getColor(R.color.SeaGreen), getResources().getColor(R.color.Red),
                getResources().getColor(R.color.Amethyst), getResources().getColor(R.color.Jade), getResources().getColor(R.color.Blue)};
    }

    private void incColorIndex()
    {
        if(currColorIndex == profileCirclesColors.length -1)
            currColorIndex = 0;
        else
            currColorIndex++;
    }

    private void initChatview()
    {
        utilMsgStatus = new UtilMsgStatus(getContext());
        chatView.setEnableSwipeRefresh(true);
        chatView.setUsernameTextColor(getResources().getColor(R.color.white));
        chatView.setLeftBubbleColor(getResources().getColor(R.color.androidGrey800));
        chatView.setLeftMessageTextColor(getResources().getColor(R.color.white));
        chatView.setRightBubbleColor(getResources().getColor(R.color.SeaGreen));
        chatView.setRightMessageTextColor(getResources().getColor(R.color.White));
        chatView.setDateSeparatorColor(getResources().getColor(R.color.white));
        chatView.setSendTimeTextColor(getResources().getColor(R.color.white));
        chatView.setMessageMarginTop(15);
        chatView.setMessageMarginBottom(15);
//        chatView.findViewById(R.id.option_button).setVisibility(View.INVISIBLE);

        chatView.setOnBubbleClickListener(new Message.OnBubbleClickListener()
        {
            @Override
            public void onClick(@NotNull final Message message)
            {
                //Present retry option if message status is failed
                if(message.getStatus() == UtilMsgStatus.STATUS_FAILED)
                {
                    //prep parse message
                    final ParseObject parseMsg = new ParseObject("Messages");
                    parseMsg.put("user", ActivHome.currentUser);
                    parseMsg.put("alert", chatAlert);
                    parseMsg.put("text", message.getMessageText());
                    parseMsg.put("displayName", cuMe.getName());

                    Snackbar.make(chatView, "Retry sending message?", Snackbar.LENGTH_LONG).setAction("Retry",
                            new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    sendParseMsgs(parseMsg, message);
                                }
                            }).show();
                }
            }
        });
    }

    private void initSendMsg()
    {
        chatView.setOnClickSendButtonListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!chatView.getInputText().matches("\\s+?") && !chatView.getInputText().isEmpty())
                {
                    final int newMsgIndex = chatView.getMessageView().getMessageList().size();

                    //Build Msg
                    final Message msg = new Message.Builder()
                            .setUser(cuMe)
                            .setRightMessage(true)
                            .setMessageText(chatView.getInputText())
                            .setCreatedAt(Calendar.getInstance())
                            .setUsernameVisibility(true)
                            .setStatusColorizerEnabled(false)
                            .setStatusIconFormatter(utilMsgStatus)
                            .setMessageStatusType(3)
                            .setStatus(UtilMsgStatus.STATUS_SENT)
                            .build();

                    //Send to chat view and pesist to db
                    chatView.send(msg);
                    final ParseObject parseMsg = new ParseObject("Messages");
                    parseMsg.put("user", ActivHome.currentUser);
                    parseMsg.put("alert", chatAlert);
                    parseMsg.put("text", chatView.getInputText());
                    parseMsg.put("displayName", cuMe.getName());
                    sendParseMsgs(parseMsg, msg);
                    chatView.setInputText("");
                }
            }
        });
    }

    private void sendParseMsgs(final ParseObject parseMsg, final Message msg)
    {
        chatView.getMessageView().updateMessageStatus(msg, UtilMsgStatus.STATUS_SENT);

        parseMsg.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
                    chatView.getMessageView().updateMessageStatus(msg, UtilMsgStatus.STATUS_DELIVERED);
                }
                else
                {
                    chatView.getMessageView().updateMessageStatus(msg, UtilMsgStatus.STATUS_FAILED);

                    //Explain Error and offer a retry
                    Snackbar.make(chatView, "Message failed: " + e.getMessage(), Snackbar.LENGTH_LONG).setAction("Retry",
                    new View.OnClickListener(){

                        @Override
                        public void onClick(View v)
                        {
                            sendParseMsgs(parseMsg, msg);
                        }
                    }).show();
                }
            }
        });
    }

    private void initChat(List<ParseObject> lstMsgs)
    {
        //add each msgs to the chat creating chat users as need along the way
        for(int i = 0; i < lstMsgs.size(); i++)
        {
            ParseObject currMsg = lstMsgs.get(i);
            ChatUser currChatUser = null;

            //Check if chat user already exists
            if (currMsg.getParseUser("user").getObjectId().equals(cuMe.getId()))
            {
                currChatUser = cuMe;
            }
            else if(hMapChatUsers.containsKey(currMsg.getParseUser("user").getObjectId()))
            {
                currChatUser = hMapChatUsers.get(currMsg.getParseUser("user").getObjectId());
            }
            else
            {
                //Create chat user obj for this user
                currChatUser = new ChatUser(getContext(), currMsg.getParseUser("user").getObjectId(), currMsg.getString("displayName"), profileCirclesColors[currColorIndex]);
                incColorIndex();
                hMapChatUsers.put(currChatUser.getId(), currChatUser);
            }

            //Add msgs to chat view
            //Build msg
            Message.Builder msgBuilder = new Message.Builder();
            msgBuilder.setMessageText(currMsg.getString("text"));
            msgBuilder.setUser(currChatUser);
            msgBuilder.setUsernameVisibility(true);

            //Init calendar for created at date
            Calendar calCreatedAt = Calendar.getInstance();
            calCreatedAt.setTime(currMsg.getCreatedAt());

            msgBuilder.setCreatedAt(calCreatedAt);

            if(currChatUser.getId().equals(cuMe.getId()))
            {
                msgBuilder.setRightMessage(true);
                msgBuilder.setStatusColorizerEnabled(false);
                msgBuilder.setStatus(UtilMsgStatus.STATUS_DELIVERED);
                msgBuilder.setMessageStatusType(3);
                msgBuilder.setStatusIconFormatter(utilMsgStatus);
                Log.d(TAG, "Message set to right");
            }
            else
                Log.d(TAG, "Didn't set msg right");
            chatView.getMessageView().setMessage(msgBuilder.build());
        }
    }

    @Subscribe
    public void onEvtNewChatMsg(EvtNewChatMsg evtChatMsg)
    {
        //Create msg
        ChatUser currChatUser = null;

        //Check if chat user already exists
        if (evtChatMsg.userId.equals(cuMe.getId()))
        {
            currChatUser = cuMe;
        }
        else if(hMapChatUsers.containsKey(evtChatMsg.userId))
        {
            currChatUser = hMapChatUsers.get(evtChatMsg.userId);
        }
        else
        {
            //Create chat user obj for this user
            currChatUser = new ChatUser(getContext(), evtChatMsg.userId, evtChatMsg.displayName, profileCirclesColors[currColorIndex]);
            incColorIndex();
            hMapChatUsers.put(currChatUser.getId(), currChatUser);
        }

        //Add msgs to chat view
        //Build msg
        final Message.Builder msgBuilder = new Message.Builder();
        msgBuilder.setMessageText(evtChatMsg.text);
        msgBuilder.setUser(currChatUser);
        msgBuilder.setUsernameVisibility(true);

        //Init calendar for created at date
        Calendar calCreatedAt = Calendar.getInstance();

        msgBuilder.setCreatedAt(calCreatedAt);

        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                chatView.getMessageView().setMessage(msgBuilder.build());
                Log.d("DebuhChat", "New received msg added");
            }
        });
    }
}
