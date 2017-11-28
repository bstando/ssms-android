package lab.android.bartosz.ssms;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_CLIENTS_CHANGED = "lab.android.bartosz.ssms.CLIENT_CHANGED";
    protected SensorService sensorService;
    protected boolean bounded = false;
    ListView listView;
    private NSDReciever nsdReciever;

    private List<MDNSDevice> devicesInfo = new ArrayList<>();
    ArrayAdapter<MDNSDevice> adapter;
    boolean toast;
    boolean searching=false;

    //SensorDataDbHelper sensorDataDbHelper;
    //NsdHelper nsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //sensorDataDbHelper = new SensorDataDbHelper(getApplicationContext());
        //nsdHelper = new NsdHelper(getApplicationContext());
        //nsdHelper.initializeNsd();

        listView = (ListView) findViewById(R.id.sensorListView);
        adapter = new SensorListAdapter(this, devicesInfo);
        listView.setAdapter(adapter);

        listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object object = listView.getItemAtPosition(position);
                MDNSDevice deviceInfo = (MDNSDevice) object;
                if (deviceInfo.getIsSensor()) {
                    StartSensorActivity startSensorActivity = new StartSensorActivity();
                    startSensorActivity.execute(new Pair<InetAddress, Integer>(deviceInfo.getAddress(),deviceInfo.getPort()));

                } else {
                    Intent intent = new Intent(getApplicationContext(), CollectorActivity.class);
                    intent.putExtra("address", deviceInfo.getAddress().getAddress());
                    intent.putExtra("port", deviceInfo.getPort());
                    startActivity(intent);
                }
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        toast = prefs.getBoolean("show_toast",true);

        initReceiver();
    }

    private void initReceiver() {
        nsdReciever = new NSDReciever();
        IntentFilter filter = new IntentFilter(ACTION_CLIENTS_CHANGED);
        registerReceiver(nsdReciever, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }


        super.onStop();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(nsdReciever);
        Intent intent = new Intent(this, SensorService.class);
        stopService(intent);
        super.onDestroy();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            bounded = true;
            //sensorService.setHelpers(nsdHelper, sensorDataDbHelper);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //sensorService.stopSearching();
            bounded = false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_exit:
                this.finish();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_start_search:
                showNoWifiWarning();
                return true;
            case R.id.action_stop_search:
                stopSearching();
                return true;
            case R.id.action_refresh:
                refreshList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void showNoWifiWarning()
    {
        if(!sensorService.isConnectedViaWiFi()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.string_noWifi))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else
        {
            startSearching();
        }
    }

    public void startSearching() {
        if (bounded) {
            sensorService.startSearching();
            searching=true;
            if(toast)
            Toast.makeText(getApplicationContext(), getString(R.string.string_searchStarted), Toast.LENGTH_LONG).show();

        } else {
            if(toast)
            Toast.makeText(getApplicationContext(), getString(R.string.string_notBounded), Toast.LENGTH_LONG).show();
        }
    }


    public void stopSearching() {
        if (bounded) {
            if(searching) {
                sensorService.stopSearching();
                if (toast)
                    Toast.makeText(getApplicationContext(), getString(R.string.string_searchStopped), Toast.LENGTH_LONG).show();
            }

        } else {
            if(toast)
            Toast.makeText(getApplicationContext(), getString(R.string.string_notBounded), Toast.LENGTH_LONG).show();
        }
    }

    public void refreshList() {
        if (bounded) {
            if(searching) {
                //Map map = sensorService.getClients();
                devicesInfo.clear();
                adapter.clear();
                adapter.notifyDataSetChanged();
                //DownloadDeviceInfo deviceInfo = new DownloadDeviceInfo();
                //deviceInfo.execute(map);

                devicesInfo.addAll(sensorService.getDevices());
                if (toast)
                    Toast.makeText(getApplicationContext(), getString(R.string.string_added) + devicesInfo.size(), Toast.LENGTH_SHORT).show();

                //adapter.addAll(devicesInfo);
                adapter.notifyDataSetChanged();
            }
        } else {
            if(toast)
            Toast.makeText(getApplicationContext(), getString(R.string.string_notBounded), Toast.LENGTH_LONG).show();
        }

    }

    private class StartSensorActivity extends AsyncTask<Pair<InetAddress,Integer>,Void,DeviceInfo>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(MainActivity.this,getString(R.string.main_conn),getString(R.string.main_connWait));
        }

        @Override
        protected DeviceInfo doInBackground(Pair<InetAddress, Integer>... params) {
            try {
                return sensorService.getDeviceInfo(params[0].first, params[0].second);
            } catch (IOException ex)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(DeviceInfo data)
        {
            progressDialog.dismiss();
            if(data!=null) {
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                intent.putExtra("address", data.getAddress().getAddress());
                intent.putExtra("port", data.getPort());
                intent.putExtra("deviceID", data.getId());
                startActivity(intent);
            }
            else
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.alert_title))
                        .setMessage(getString(R.string.string_downloadError))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    private class DownloadDeviceInfo extends AsyncTask<Map<InetAddress, Integer>, Void, Void> {
        protected DownloadDeviceInfo() {

        }

        @Override
        protected Void doInBackground(Map<InetAddress, Integer>... pairs) {
            Map<InetAddress, Integer> map = pairs[0];
            Set<InetAddress> set = map.keySet();
            for (InetAddress inetAddress : set) {
                InetAddress address = inetAddress;
                Integer port = map.get(inetAddress);
                try {
                    DeviceInfo deviceInfo = sensorService.getDeviceInfo(address, port);

                    if (!devicesInfo.contains(deviceInfo)) {
                        //devicesInfo.add(deviceInfo);
                        //adapter.add(deviceInfo);
                    }
                }
                catch (IOException ex)
                {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void data) {

            adapter.notifyDataSetChanged();
            if(toast)
            Toast.makeText(getApplicationContext(), getString(R.string.string_found) + devicesInfo.size(), Toast.LENGTH_LONG).show();
        }

    }

    public class NSDReciever extends BroadcastReceiver {
        public NSDReciever() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CLIENTS_CHANGED)) {
                refreshList();
            }
        }
    }
}

