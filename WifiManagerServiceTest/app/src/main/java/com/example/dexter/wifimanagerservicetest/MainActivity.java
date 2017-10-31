package com.example.dexter.wifimanagerservicetest;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ListView.OnItemClickListener, PasswordDialogFragment.PasswordDialogListener {
    public final String ERR_TAG = "ERR";

    private TextView mTextMessage;
    private TextView mSSIDText;
    private Switch mWifiStatus;
    private Button mScanButton;
    private Button mDisconnectButton;
    private ListView mNetworksList;

    private boolean mWifiManagerBound = false;
    private ArrayList<String> mNetworksByRSSI;
    private ArrayList<ScanResult> mScanResults;
    private ArrayAdapter<String> mNetworksArrayAdapter;
    private String  mSelectedSSID;

    IWifiManagerService mIWifiManagerService;

    private final WifiStateChangedCallback.Stub mStateCallback = new WifiStateChangedCallback.Stub() {
        @Override
        public void onWifiStateChanged(boolean enabled) throws RemoteException {
            mWifiStatus.setChecked(enabled);
        }
    };

    private ServiceConnection mWifiManagerConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            mIWifiManagerService = IWifiManagerService.Stub.asInterface(service);
            mWifiManagerBound = true;

            // call the initial setup after we are connected
            wifiManagerServiceConnected();
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(ERR_TAG, "Service has unexpectedly disconnected");
            mIWifiManagerService = null;
            mWifiManagerBound = false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private void wifiManagerServiceConnected()
    {
        if (mWifiManagerBound) {
            boolean wifiState = false;

            try
            {
                wifiState = mIWifiManagerService.getWifiAdapterState();
            }
            catch (RemoteException re)
            {
                Log.e(ERR_TAG, "Error getting wifi network state from the remote service!");
            }

            mWifiStatus.setChecked(wifiState);

            String wifiSSID = null;

            try
            {
                wifiSSID = mIWifiManagerService.getCurrentNetworkSSID();
            }

            catch (RemoteException re)
            {
                Log.e(ERR_TAG, "Error getting wifi network SSID from the remote service!");
            }

            if (wifiSSID == null)
            {
                mSSIDText.setText(getString(R.string.not_connected));
            }

            else
            {
                mSSIDText.setText(wifiSSID);
            }

            try
            {
                mIWifiManagerService.setWifiStateChangedListener(mStateCallback);
            }
            catch (RemoteException re)
            {
                Log.e(ERR_TAG, "Error setting the state changed callback");
            }

        }
    }

    @Override
    public void onClick(View view)
    {
        if (mWifiManagerBound)
        {
            try
            {
                mIWifiManagerService.startWifiScan();
            }

            catch (RemoteException re)
            {
                Log.e(ERR_TAG, "Error starting wifi scan from the remote service!");
            }
        }
    }

    @Override
    public void onConnectPressed (DialogFragment dialog)
    {
        String password = ((PasswordDialogFragment)dialog).getPasswordText();

        try
        {
            if (mIWifiManagerService.connectToWPA2Network(mSelectedSSID, password))
            {
                //re-do the initial connectivity check, now that the network has changed
                wifiManagerServiceConnected();
            }
        }

        catch (RemoteException re)
        {
            Log.e(ERR_TAG, "Error starting wifi connection from the remote service!");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        mSelectedSSID = mNetworksByRSSI.get(position);
        ScanResult network = mScanResults.get(position);

        if (network.capabilities.contains("WPA2"))
        {
            PasswordDialogFragment dialog = new PasswordDialogFragment();
            dialog.mMainActivityRef = this;
            dialog.show(getFragmentManager(), "password");
        }

        else
        {
            try
            {
                mIWifiManagerService.connectToOpenNetwork(mSelectedSSID);
            }

            catch (RemoteException re)
            {
                Log.e(ERR_TAG, "Error starting wifi connection from the remote service!");
            }
        }
    }

    private void updateWifiNetworkList(List<ScanResult> scanResults)
    {
        Comparator<ScanResult> rssiComparator = new Comparator<ScanResult>() {
            @Override
            public int compare (ScanResult lhs, ScanResult rhs)
            {
                if (lhs.level < rhs.level)
                {
                    return 1;
                }
                else if (lhs.level == rhs.level)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
        };

        // sort by RSSI
        Collections.sort(scanResults, rssiComparator);
        mScanResults = new ArrayList<>(scanResults);

        // now fill up the array for the list
        mNetworksByRSSI.clear();
        for (ScanResult result : scanResults)
        {
            mNetworksByRSSI.add(result.SSID);
        }

        mNetworksArrayAdapter.notifyDataSetChanged();
    }

//    @Override
//    public void onWifiStateChanged(boolean enabled)
//    {
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // set up the UI elements
        mWifiStatus = findViewById(R.id.wifi_status);
        mSSIDText = findViewById(R.id.ssid_text_view);
        mScanButton = findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(this);
        mDisconnectButton = findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    if (mWifiManagerBound)
                    {
                        mIWifiManagerService.disconnectFromCurrentNetwork();
                    }
                }
                catch (RemoteException re)
                {
                    Log.e(ERR_TAG, "Failed to connect to the remote service to disconnect");
                }
            }
        });

        mNetworksList = findViewById(R.id.networks_list);

        mNetworksByRSSI = new ArrayList<>();
        mNetworksArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mNetworksByRSSI);
        mNetworksList.setAdapter(mNetworksArrayAdapter);
        mNetworksList.setOnItemClickListener(this);

        // set up the wifi scan callback
        WifiManagerService.wifiScanCallback = new WifiManagerService.WifiScanCallback(){
            @Override
            void ScanResultsCallBack(List<ScanResult> results)
            {
                updateWifiNetworkList(results);
            }
        };

        // create the connection to the service
        Intent intent = new Intent(this, WifiManagerService.class);
        boolean result = bindService(intent, mWifiManagerConnection, Context.BIND_AUTO_CREATE);

        if (!result)
        {
            Log.e(ERR_TAG, "unable to bind service.");
        }
    }
}
