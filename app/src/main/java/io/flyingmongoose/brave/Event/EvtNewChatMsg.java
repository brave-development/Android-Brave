package io.flyingmongoose.brave.event;

/**
 * Created by Acinite on 2018/02/06.
 */

public class EvtNewChatMsg
{

    public String userId;
    public String displayName;
    public String text;

    public EvtNewChatMsg(String userId, String displayName, String text)
    {
        this.userId = userId;
        this.displayName = displayName;
        this.text = text;
    }
}
