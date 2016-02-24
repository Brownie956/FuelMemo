/*Author: Chris Brown
* Date: 23/02/2016
* Description: Utils class for generic functions*/
package com.cfbrownweb.fuelmemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class Utils {

    public static boolean isConnected(Context context){
        //Get network information
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void netErrorToast(Context context){
        Toast.makeText(context, R.string.net_retry_fail, Toast.LENGTH_LONG).show();
    }

    public static void defaultErrorToast(Context context){
        Toast.makeText(context, R.string.default_error, Toast.LENGTH_LONG).show();
    }

    public static void serverErrorToast(Context context){
        Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG).show();
    }

    public static void setRefreshIconState(boolean refreshing, Menu menu){
        if(menu != null){
            MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
            if(refreshItem != null){
                if(refreshing){
                    refreshItem.setActionView(R.layout.refreshing_icon);
                }
                else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }
}
