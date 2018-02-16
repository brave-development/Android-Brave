package io.flyingmongoose.brave.interfaces;

import android.location.Location;

/**
 * Created by wprenison on 2017/05/20.
 */

public interface UserLocationListener
{
    void onLocationUpdate(Location newLocation);
}
