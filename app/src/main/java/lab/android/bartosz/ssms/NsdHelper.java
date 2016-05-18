package lab.android.bartosz.ssms;


import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class NsdHelper {

    private NsdManager.ResolveListener resolveListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager nsdManager;
    Context context;

    private final String TAG = "NSD";
    private final String SERVICE_NAME = "sjd";
    private final String SERVICE_TYPE = "_json._udp.";

    Map<InetAddress,Integer> mDSNList;

    public NsdHelper(Context appContext)
    {
        mDSNList = new HashMap<>();
        context = appContext;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd()
    {
        initializeResolveListener();
        initializeDiscoveryListener();
    }


    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                Log.d(TAG, "Host:" + host.getHostAddress() + ", Port:" + port);
                mDSNList.put(host,port);
                Intent intent = new Intent(MainActivity.ACTION_CLIENTS_CHANGED);
                context.sendBroadcast(intent);
            }
        };
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                }  else if (service.getServiceName().contains(SERVICE_NAME)){
                    nsdManager.resolveService(service, resolveListener);
                    Log.d(TAG, "Found machine: " + SERVICE_NAME);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }


    public void discoverServices() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    public Map<InetAddress,Integer> getAvailableConnections() {
        return mDSNList;
    }


}
