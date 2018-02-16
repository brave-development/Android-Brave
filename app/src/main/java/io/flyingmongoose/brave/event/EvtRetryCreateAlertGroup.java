package io.flyingmongoose.brave.event;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Acinite on 2018/02/15.
 */

public class EvtRetryCreateAlertGroup
{

    public List<ParseObject> lstGroups;
    public int retryCount = 0;

    public EvtRetryCreateAlertGroup(List<ParseObject> lstGroups)
    {
        this.lstGroups = lstGroups;
    }
}
