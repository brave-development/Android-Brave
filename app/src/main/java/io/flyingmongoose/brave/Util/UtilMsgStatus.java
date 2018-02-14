package io.flyingmongoose.brave.Util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.github.bassaer.chatmessageview.util.IMessageStatusIconFormatter;

import org.jetbrains.annotations.NotNull;

import io.flyingmongoose.brave.R;

/**
 * Created by Acinite on 2018/02/06.
 */

public class UtilMsgStatus implements IMessageStatusIconFormatter
{
    public static final int STATUS_SENT = 1;
    public static final int STATUS_FAILED = -1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_READ = 3;

    private Drawable drawFailed;
    private Drawable drawSent;
    private Drawable drawDelivered;
    private Drawable drawRead;

    public UtilMsgStatus(Context context)
    {
        drawSent = context.getResources().getDrawable(R.drawable.ic_msg_send);
        drawFailed = context.getResources().getDrawable(R.drawable.ic_msg_failed);
        drawDelivered = context.getResources().getDrawable(R.drawable.ic_msg_delivered);
    }

    @NotNull
    @Override
    public Drawable getStatusIcon(int status, boolean b)
    {
        switch(status)
        {
            case STATUS_SENT:
                return drawSent;

            case STATUS_FAILED:
                return drawFailed;

            case STATUS_DELIVERED:
                return drawDelivered;

            case STATUS_READ:
                return drawRead;

            default:
                return drawFailed;
        }
    }
}
