package io.flyingmongoose.brave.event;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Acinite on 2018/02/15.
 */

public class EvtAlertResult
{
    public boolean success;
    public String errorMsg = "";
    public EvtRetryCreateAlertGroup evtRetryCreateAlertGroup;
    public List<ParseObject> lstGroups;

    public EvtAlertResult(boolean success)
    {
        this.success = success;
    }

    public EvtAlertResult(boolean success, String errorMsg, List<ParseObject> lstGroups)
    {
        this.success = success;
        this.errorMsg = errorMsg;
        this.lstGroups = lstGroups;
    }

    public EvtAlertResult(boolean success, String errorMsg, List<ParseObject> lstGroups, EvtRetryCreateAlertGroup evtRetryCreateAlertGroup)
    {
        this.success = success;
        this.errorMsg = errorMsg;
        this.lstGroups = lstGroups;
        this.evtRetryCreateAlertGroup = evtRetryCreateAlertGroup;
    }
}
