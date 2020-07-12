package com.ledeke.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button onoffWifi, nearByDevices, sendBtn;
    TextView read_msg_box;
    EditText writeMsg;
    ListView listView;
    TextView connectionStatus;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    BroadcastReceiver mReceiver;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    static final int MESSAGE_READ = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializa();
        ListenerConfig();
    }


    Handler handler = new Handler(new Handler.Callback(){
    @Override
    public boolean handleMessage(Message msg) {
        connectionStatus.setText("Message Start");
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);
                read_msg_box.setText(tempMsg);
                break;
        }
        return true;
    }
});

private void ListenerConfig() {
        onoffWifi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    onoffWifi.setText("ON");

                } else {
                    wifiManager.setWifiEnabled(true);
                    onoffWifi.setText("OFF");
                }
            }
        });

        nearByDevices.setOnClickListener(new  View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Discovery Start Failed");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Not connected to ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

//        listView.setOnClickListener();

    sendBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String msg = writeMsg.getText().toString();
            sendReceive.write(msg.getBytes());
        }
    });
}
    private void initializa() {
        onoffWifi = (Button) findViewById(R.id.onOffWifi);
        read_msg_box = (TextView) findViewById(R.id.read_msg_box);
        writeMsg = (EditText) findViewById(R.id.writeMsg);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        nearByDevices = (Button) findViewById(R.id.discover);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        listView = (ListView) findViewById(R.id.peerListView);


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiStatusBroadcasteEmitter(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
//            connectionStatus.setText("Discoving ...");

            if (!peerList.getDeviceList().equals(peers)) {
//                Toast.makeText(getApplicationContext(), "devices found", Toast.LENGTH_SHORT).show();
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }
        if (peers.size() == 0) {
            Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_SHORT).show();
            return;
        }

    }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListner = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupInfoAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                connectionStatus.setText("Host");
//                serverClass = new ServerClass();
//                serverClass.start();
            } else if(wifiP2pInfo.groupFormed) {
                connectionStatus.setText("client");
//                clientClass = new ClientClass(groupInfoAddress);
//                clientClass.start();
            }
        }
    };

             @Override
             protected void onResume() {
                 super.onResume();
                 registerReceiver(mReceiver, mIntentFilter);
             }

             @Override
             protected void onPause() {
                 super.onPause();
                 unregisterReceiver(mReceiver);
             }


    public class ServerClass extends Thread {
                 Socket socket;
                 ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(88888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)  {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;
            while (socket!= null) {
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0) {
                        handler.obtainMessage(MESSAGE_READ, bytes,-1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class ClientClass extends Thread {
        Socket socket;
        String hostAddress;

        public ClientClass(InetAddress address){
            hostAddress = address.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {

                socket.connect(new InetSocketAddress(hostAddress, 88888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




         }



