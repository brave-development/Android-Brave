package io.flyingmongoose.brave.util;

/**
 * Created by wprenison on 2017/07/06.
 */

public class UtilFormating
{

    //Used to format text values
    public static String formatChannelName(String groupName)
    {
        String channelName = "";

        //Lower case everything
        channelName = groupName.toLowerCase();

        //trim spaces from front and rear
        channelName = channelName.trim();

        //caps each letter of each word
        String[] strArray = channelName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray)
        {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap);    //And remove white spacing
        }
        channelName = builder.toString();

        return channelName;
    }

    public static String formatGroupName(String groupName)
    {
        String newGroupName = "";

        //Lower case everything
        newGroupName = groupName.toLowerCase();

        //trim spaces from front and rear
        newGroupName = newGroupName.trim();

        //caps each letter of each word
        String[] strArray = newGroupName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray)
        {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");    //And remove white spacing
        }
        newGroupName = builder.toString();

        return newGroupName;
    }

    public static String formatGroupFlatName(String groupName)
    {
        String groupFlatName = "";

        //Lower case everything
        groupFlatName = groupName.toLowerCase();

        //trim spaces from front and rear
        groupFlatName = groupFlatName.trim();

        //caps each letter of each word
        String[] strArray = groupFlatName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray)
        {
            builder.append(s);    //And remove white spacing
        }
        groupFlatName = builder.toString();

        return groupFlatName;
    }

}
