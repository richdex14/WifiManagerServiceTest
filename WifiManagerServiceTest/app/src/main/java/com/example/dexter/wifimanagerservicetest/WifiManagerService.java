package com.example.dexter.wifimanagerservicetest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

/**
 * Created by dexter on 10/29/17.
 */

public class WifiManagerService extends Service {
    private WifiManager mManager;
    private List<ScanResult> scanResults;

    protected static WifiScanCallback wifiScanCallback;

    private final IWifiManagerService.Stub mBinder = new IWifiManagerService.Stub() {
        private WifiStateChangedCallback mCallback;

        @Override
        public boolean getWifiAdapterState() throws RemoteException {
            return mManager.isWifiEnabled();
        }

        @Override
        public void setWifiStateChangedListener(WifiStateChangedCallback callback)
        {
            mCallback = callback;

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    if (mCallback != null)
                    {
                        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                        try
                        {
                            mCallback.onWifiStateChanged(wifiState == 3);
                        }
                        catch (RemoteException re)
                        {
                            Log.e("ERR", "Error connecting to remote service");
                        }
                    }
                }
            }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }

        @Override
        public String getCurrentNetworkSSID()
        {
            WifiInfo wifiInfo = mManager.getConnectionInfo();
            String ssidString = wifiInfo.getSSID();
            if (ssidString == null)
            {
                return null;
            }

            else
            {
                return ssidString;
            }
        }

        @Override
        public void startWifiScan()
        {
            mManager.startScan();
        }

        @Override
        public boolean connectToWPA2Network(String ssid, String password)
        {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + ssid + "\"";
            conf.preSharedKey = "\"" + password + "\"";
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            int networkID = mManager.addNetwork(conf);
            if (networkID != -1)
            {
                mManager.enableNetwork(networkID, true);

                return true;
            }

            else
            {
                return false;
            }
        }

        @Override
        public boolean connectToOpenNetwork(String ssid)
        {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + ssid + "\"";
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            int networkID = mManager.addNetwork(conf);
            if (networkID != -1)
            {
                mManager.enableNetwork(networkID, true);

                return true;
            }

            else
            {
                return false;
            }
        }

        @Override
        public boolean disconnectFromCurrentNetwork()
        {
            return mManager.disconnect();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // listen for the scan to be completed and then call the callback
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                scanResults = mManager.getScanResults();
                wifiScanCallback.ScanResultsCallBack(scanResults);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        return mBinder;
    }

    static abstract class WifiScanCallback
    {
        abstract void ScanResultsCallBack(List<ScanResult> results);
    }
}
