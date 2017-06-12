package io.flyingmongoose.brave;

import android.location.Location;

/**
 * Created by wprenison on 2017/05/20.
 */

public interface UserLocationListener
{
    void onLocationUpdate(Location newLocation);
}
