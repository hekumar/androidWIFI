package com.ledeke.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

/**
 * Created by Hemant Kumar on 7/2/2020.
 */

public class WifiStatusBroadcasteEmitter extends BroadcastReceiver{
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WifiStatusBroadcasteEmitter(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
       String action = intent.getAction();
       if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
            Toast.makeText(context, "Wifi Switched On", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Wifi Switched Off", Toast.LENGTH_SHORT).show();
        }
       } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
           // Call  WifiP2pManager.requestPeers() to get a list of current peers
          // Toast.makeText(context, "peer sdsdf", Toast.LENGTH_SHORT).show();
           if (mManager != null) {
              // Toast.makeText(context, "Wifi dddd", Toast.LENGTH_SHORT).show();
               mManager.requestPeers(mChannel, mActivity.peerListListener);
           }

       } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
           if(mManager == null) {
               return;
           }
           NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
           if(networkInfo.isConnected()) {
            mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListner);
           } else {
               mActivity.connectionStatus.setText("Disconnected");
           }

       } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
          // Toast.makeText(context, "peer dev ch", Toast.LENGTH_SHORT).show();
       }
    }
}
