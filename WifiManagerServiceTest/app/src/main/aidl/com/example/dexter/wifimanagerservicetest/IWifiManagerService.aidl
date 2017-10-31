// IWifiManagerServiceAidl.aidl
package com.example.dexter.wifimanagerservicetest;

// Declare any non-default types here with import statements
import android.net.wifi.WifiInfo;
import com.example.dexter.wifimanagerservicetest.WifiStateChangedCallback;

interface IWifiManagerService {
//    /**
//     * Demonstrates some basic types that you can use as parameters
//     * and return values in AIDL.
//     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    /**
    *  Get the Wifi Adapter State (assuming all android devices this is used on will only have 1
    *  Wifi adapter).
    */
    boolean getWifiAdapterState();

    /**
    *   Set callback for wifi state changes
    */
    void setWifiStateChangedListener(in WifiStateChangedCallback callback);

    /**
    *  Get the Wifi Connected Network SSID, or null if not connected (assuming all android devices this
    *  is used on will only have 1 Wifi adapter).
    */
    String getCurrentNetworkSSID();

    /**
    *  Start the wifi scan process. To receive the results, you must implement the callback
    *  within WifiManagerService by implementing the WifiScanCallback abstract class
    */
    void startWifiScan();

    /**
    *   Connect to a Secured network using WPA2 username and password
    */
    boolean connectToWPA2Network(in String ssid, in String password);

    /**
    *   Connect to an open wifi network.
    */
    boolean connectToOpenNetwork(in String ssid);

    /**
    *  Disconnects from current network
    */
    boolean disconnectFromCurrentNetwork();
}

