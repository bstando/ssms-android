package lab.android.bartosz.ssms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    protected SensorService sensorService;
    protected boolean bounded = false;
    ListView listView;

    private List<DeviceInfo> devicesInfo = new ArrayList<>();
    ArrayAdapter<DeviceInfo> adapter;

    SensorDataDbHelper sensorDataDbHelper;
    NsdHelper nsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sensorDataDbHelper = new SensorDataDbHelper(getApplicationContext());
        nsdHelper = new NsdHelper(getApplicationContext());
        nsdHelper.initializeNsd();

        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<DeviceInfo>(this,android.R.layout.simple_list_item_1,devicesInfo);
        listView.setAdapter(adapter);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this,SensorService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            bounded = true;
            sensorService.setHelpers(nsdHelper, sensorDataDbHelper);

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
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_start_search:
                startSearching();
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

    public void startSearching() {
        if(bounded) {
            sensorService.startSearching();
            Toast.makeText(getApplicationContext(), "Searching Started", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Service is NOT bounded", Toast.LENGTH_LONG).show();
        }
    }


    public void stopSearching() {
        if (bounded) {
            sensorService.stopSearching();
            Toast.makeText(getApplicationContext(), "Searching Stopped", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Service is NOT bounded", Toast.LENGTH_LONG).show();
        }
    }

    public void refreshList()
    {
        if (bounded) {
        Map map = sensorService.getClients();
        devicesInfo.clear();
        DownloadDeviceInfo deviceInfo = new DownloadDeviceInfo();
        deviceInfo.execute(map);
        } else {
            Toast.makeText(getApplicationContext(), "Service is NOT bounded", Toast.LENGTH_LONG).show();
        }

    }

    private class DownloadDeviceInfo extends AsyncTask<Map<InetAddress, Integer>, Void, Void> {
        protected DownloadDeviceInfo() {

        }

        @Override
        protected Void doInBackground(Map<InetAddress, Integer>... pairs) {
            Map<InetAddress,Integer> map = pairs[0];
            Set<InetAddress> set = map.keySet();
            for(InetAddress inetAddress : set) {
                InetAddress address = inetAddress;
                Integer port = map.get(inetAddress);
                DeviceInfo deviceInfo = sensorService.getDeviceInfo(address,port);
                if(!devicesInfo.contains(deviceInfo)) {
                    devicesInfo.add(deviceInfo);
                    //adapter.add(deviceInfo);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void data) {

            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "OK: " + devicesInfo.size(), Toast.LENGTH_LONG).show();
        }

    }

}
