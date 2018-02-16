package io.flyingmongoose.brave.event;

/**
 * Created by Acinite on 2018/01/31.
 */

public class EvtRespondResult
{
    public boolean isResponding;
    public String errorMsg = "";



    public EvtRespondResult(boolean isResponding)
    {
        this.isResponding = isResponding;
    }

    public EvtRespondResult(boolean isResponding, String errorMsg)
    {
        this.isResponding = isResponding;
        this.errorMsg = errorMsg;
    }
}
