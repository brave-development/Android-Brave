package io.flyingmongoose.brave.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.github.bassaer.chatmessageview.model.IChatUser;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.flyingmongoose.brave.util.UtilAutoProfileBitmap;

/**
 * Created by Acinite on 2017/12/07.
 */

public class ChatUser implements IChatUser {

    int [] profileCirclesColors;

    int thisProfileCircleColor;

    Bitmap autoProfilePic = null;

    Context context;
    ParseUser user;

    public ChatUser(Context context, ParseUser user, int color)
    {
        this.context = context;
        this.user = user;
        this.thisProfileCircleColor = color;
    }

    public ChatUser(Context context, String objectId, String name, int color)
    {
        this.context = context;
        user = new ParseUser();
        user.setObjectId(objectId);
        user.put("name", name);
        this.thisProfileCircleColor = color;
    }

    @NotNull
    @Override
    public String getId() {
        return user.getObjectId();
    }

    @Nullable
    @Override
    public String getName() {
        return user.getString("name");
    }

    @Nullable
    @Override
    public Bitmap getIcon()
    {
        //Get string to be used
        String[] splitName = getName().split(" ");
        String firstLet = splitName[0].substring(0, 1);
        String secondLet = splitName[1].substring(0, 1);

        return UtilAutoProfileBitmap.textAsBitmap(firstLet + secondLet, 40f, Color.WHITE, thisProfileCircleColor);
    }

    @Override
    public void setIcon(Bitmap bitmap)
    {

    }

    public int getUserColor(){return thisProfileCircleColor;}
}
