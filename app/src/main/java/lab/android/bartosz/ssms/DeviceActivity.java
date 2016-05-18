package lab.android.bartosz.ssms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    InetAddress address;
    int port;
    ListView listView;


    protected SensorService sensorService;
    protected boolean bounded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extrasBundle = getIntent().getExtras();
        if(extrasBundle!=null) {
            try {
                address = InetAddress.getByAddress(extrasBundle.getByteArray("address"));
            } catch (UnknownHostException e)
            {
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
            port = extrasBundle.getInt("port");
        }
        listView = (ListView) findViewById(R.id.methodsListView);
        List<String> list= new ArrayList<String>();

        list.add(getString(R.string.show_device_info));
        list.add(getString(R.string.show_current_readings));
        list.add(getString(R.string.start_periodic_task));
        list.add(getString(R.string.stop_periodic_task));
        list.add(getString(R.string.show_reading_from_database));
        list.add(getString(R.string.show_charts));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        onClickDeviceInfoBtn(view);
                        break;
                    case 1:
                        onClickCurrentReadingsBtn(view);
                        break;
                    case 2:
                        startPeriodicTask(view);
                        break;
                    case 3:
                        stopPeriodicTask(view);
                        break;
                    case 4:
                        break;
                    case 5:
                        showChart();
                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_device, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            //sensorService.setHelpers(nsdHelper, sensorDataDbHelper);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //sensorService.stopSearching();
            bounded = false;
        }
    };

    public void onClickDeviceInfoBtn(View v)
    {

    }

    public void showChart()
    {
        Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
        startActivity(intent);
    }

    public void onClickCurrentReadingsBtn(View v)
    {
        if(bounded)
        sensorService.getDataFromSensor(address,port);
    }

    public void startPeriodicTask(View v)
    {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = prefs.getString("sync_frequency","100000");
        Log.d("SYNC",interval);

        if(bounded)
            sensorService.startTimerTask(address,port,Integer.valueOf(interval));
    }

    public void stopPeriodicTask(View v)
    {
        if(bounded)
            sensorService.stopTimerTask(address);
    }

}
