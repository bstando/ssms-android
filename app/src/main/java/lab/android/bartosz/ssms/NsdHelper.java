package lab.android.bartosz.ssms;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NsdHelper {

    private NsdManager.ResolveListener sensorResolveListener;
    private NsdManager.ResolveListener collectorResolveListener;
    private NsdManager.DiscoveryListener sensorDiscoveryListener;
    private NsdManager.DiscoveryListener collectorDiscoveryListener;
    private NsdManager nsdManager;
    Context context;
    boolean toast;

    private final String TAG = "NSD";
    private final String SERVICE_NAME = "sjd";
    private final String SERVICE_TYPE = "_json._udp.";
    private final String COLLECTOR_SERVICE_TYPE = "_ssmsd._udp.";

    Map<InetAddress, Integer> mDSNList;
    Map<InetAddress, Integer> cmDNSList;
    List<MDNSDevice> devices;
    List<MDNSDevice> sensorDevices;
    List<MDNSDevice> collectorDevices;

    public NsdHelper(Context appContext) {
        mDSNList = new HashMap<>();
        cmDNSList = new HashMap<>();
        devices = new ArrayList<>();
        sensorDevices = new ArrayList<>();
        collectorDevices = new ArrayList<>();
        context = appContext;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        toast = prefs.getBoolean("show_toast", false);
    }

    public void initializeNsd() {
        initializeDiscoveryListener();
        initializeResolveListener();
    }


    public void initializeResolveListener() {
        sensorResolveListener = new NsdManager.ResolveListener() {


            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed " + errorCode + ", " + serviceInfo);
                if (NsdManager.FAILURE_ALREADY_ACTIVE == errorCode) {
                    nsdManager.resolveService(serviceInfo, sensorResolveListener);
                }

            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                Log.e(TAG, "Host:" + host.getHostAddress() + ", Port:" + port + ", Service Type: " + serviceInfo.getServiceType());
                if (serviceInfo.getServiceType().equals("._json._udp")) {
                    mDSNList.put(host, port);
                    sensorDevices.add(new MDNSDevice(host, port, serviceInfo.getServiceName(), true));
                }
                Intent intent = new Intent(MainActivity.ACTION_CLIENTS_CHANGED);
                context.sendBroadcast(intent);
            }
        };
        collectorResolveListener = new NsdManager.ResolveListener() {


            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed " + errorCode + ", " + serviceInfo);
                if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) {
                    nsdManager.resolveService(serviceInfo, collectorResolveListener);
                }

            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                Log.e(TAG, "Host:" + host.getHostAddress() + ", Port:" + port + ", Service Type: " + serviceInfo.getServiceType());
                if (serviceInfo.getServiceType().equals("._ssmsd._udp")) {
                    cmDNSList.put(host, port);
                    collectorDevices.add(new MDNSDevice(host, port, serviceInfo.getServiceName(), false));
                }
                Intent intent = new Intent(MainActivity.ACTION_CLIENTS_CHANGED);
                context.sendBroadcast(intent);
            }
        };
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        sensorDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.e(TAG, "Service discovery success " + service);
                if ((service.getServiceType().equals(SERVICE_TYPE))) {
                    nsdManager.resolveService(service, sensorResolveListener);
                    Log.e(TAG, "Found machine: " + SERVICE_NAME);

                } else {
                    Log.e(TAG, "Found machine: " + SERVICE_NAME);
                    Log.e(TAG, "Unknown Service Type: " + service.getServiceType());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
                sensorDevices.clear();

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

        collectorDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.e(TAG, "Service discovery success " + service);
                if (service.getServiceType().equals(COLLECTOR_SERVICE_TYPE)) {

                    Log.e(TAG, "Found machine: " + SERVICE_NAME);
                    nsdManager.resolveService(service, collectorResolveListener);

                } else {
                    Log.e(TAG, "Found machine: " + SERVICE_NAME);
                    Log.e(TAG, "Unknown Service Type: " + service.getServiceType());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
                collectorDevices.clear();

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
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, sensorDiscoveryListener);
        nsdManager.discoverServices(COLLECTOR_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, collectorDiscoveryListener);
    }

    public void stopDiscovery() {
        nsdManager.stopServiceDiscovery(sensorDiscoveryListener);
        nsdManager.stopServiceDiscovery(collectorDiscoveryListener);
    }

    public Map<InetAddress, Integer> getAvailableConnections() {
        return mDSNList;
    }

    public Map<InetAddress, Integer> getAvailableCollectors() {
        return cmDNSList;
    }

    public List<MDNSDevice> getAvailableDevices() {
        devices.clear();
        devices.addAll(sensorDevices);
        devices.addAll(collectorDevices);
        return devices;
    }

    public void clearLists()
    {
        devices.clear();
        sensorDevices.clear();
        collectorDevices.clear();
    }
}
