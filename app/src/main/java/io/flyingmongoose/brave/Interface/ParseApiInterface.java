package io.flyingmongoose.brave.Interface;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by wprenison on 2017/10/17.
 */

public interface ParseApiInterface
{

    void onLoadingStatusChanged(boolean loading);

    void onParseObjectListResponse(List<ParseObject> listObjects);
}
