// WifiStateChangedCallback.aidl
package com.example.dexter.wifimanagerservicetest;

// Declare any non-default types here with import statements

interface WifiStateChangedCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void onWifiStateChanged(boolean enabled);
}
