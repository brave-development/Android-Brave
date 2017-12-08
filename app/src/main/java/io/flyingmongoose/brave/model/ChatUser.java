package io.flyingmongoose.brave.model;

import android.graphics.Bitmap;

import com.github.bassaer.chatmessageview.model.IChatUser;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Acinite on 2017/12/07.
 */

public class ChatUser implements IChatUser {
    ParseUser user;

    public ChatUser(ParseUser user){this.user = user;}

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
    public Bitmap getIcon() {
        return null;
    }

    @Override
    public void setIcon(Bitmap bitmap)
    {

    }
}
