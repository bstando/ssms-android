package lab.android.bartosz.ssms;

import android.bluetooth.BluetoothClass;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SensorReadingsActivity extends AppCompatActivity {

    ListView listView;
    protected SensorService sensorService;
    protected boolean bounded = false;
    int sensorID = -1;
    boolean isSensorReadingDetailFragmentInLayout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_readings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.sensorReadingsMenu);
        setSupportActionBar(toolbar);

        Bundle extrasBundle = getIntent().getExtras();
        if(extrasBundle!=null) {

            sensorID = extrasBundle.getInt("sensorID");
        }
        SensorReadingDetailFragment sensorReadingDetailFragment = (SensorReadingDetailFragment) getFragmentManager().findFragmentById(R.id.sensorReadingDetailFragment);
        if(sensorReadingDetailFragment!=null && sensorReadingDetailFragment.isInLayout())
        {
            isSensorReadingDetailFragmentInLayout = true;
        }

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
    void generate()
    {
        listView = (ListView) findViewById(R.id.readingsListView);
        List<SensorData> datas;
        if(sensorID!=-1)
        {
            datas =  sensorService.getBySensorID(sensorID);
        } else
        {
            datas = sensorService.getFromDatabase();
        }
        ArrayAdapter<SensorData> arrayAdapter = new ArrayAdapter<SensorData>(this,android.R.layout.simple_list_item_1,datas);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object object = listView.getItemAtPosition(position);
                SensorData sensorData = (SensorData) object;
                if(!isSensorReadingDetailFragmentInLayout) {
                    Intent intent = new Intent(getApplicationContext(), SensorReadingDetailActivity.class);
                    intent.putExtra("id", sensorData.getId());
                    startActivity(intent);
                } else
                {
                    makeInterface(sensorData);
                }
            }
        });

    }

    void update()
    {
        List<SensorData> datas;
        if(sensorID!=-1)
        {
            datas =  sensorService.getBySensorID(sensorID);
        } else
        {
            datas = sensorService.getFromDatabase();
        }
        ArrayAdapter<SensorData> arrayAdapter = new ArrayAdapter<SensorData>(this,android.R.layout.simple_list_item_1,datas);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

    }

    void makeInterface(final SensorData sensorData)
    {
        final TextView sensorDataId = (TextView) findViewById(R.id.readingIDTextView);
        final TextView sensorDataDate = (TextView) findViewById(R.id.readingDateTextView);
        final EditText sensorDataDeviceId = (EditText) findViewById(R.id.readingDeviceIDEditText);
        final EditText sensorDataTemperature =  (EditText) findViewById(R.id.readingTemperatureEditText);
        final EditText sensorDataHumidity =  (EditText) findViewById(R.id.readingHumidityEditText);
        Button updateButton = (Button) findViewById(R.id.updateDataBtn);
        Button removeButton = (Button) findViewById(R.id.removeDataBtn);
        sensorDataId.setText(String.valueOf(sensorData.getId()));
        sensorDataDate.setText(sensorData.getDate().toString());
        sensorDataDeviceId.setText(String.valueOf(sensorData.getSensorId()));
        sensorDataTemperature.setText(String.valueOf(sensorData.getTemperature()));
        sensorDataHumidity.setText(String.valueOf(sensorData.getHumidity()));
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorData sensor = new SensorData();
                sensor.setId(sensorData.getId());
                sensor.setDate(sensorData.getDate());
                sensor.setSensorId(Long.parseLong(sensorDataDeviceId.getText().toString()));
                sensor.setTemperature(Float.parseFloat(sensorDataTemperature.getText().toString()));
                sensor.setHumidity(Float.parseFloat(sensorDataHumidity.getText().toString()));
                sensorService.updateSensorData(sensor);
                update();
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorService.removeFromDatabase(sensorData.getId());
                update();
            }
        });

    }



    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            bounded = true;
            generate();
            //sensorService.setHelpers(nsdHelper, sensorDataDbHelper);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //sensorService.stopSearching();
            bounded = false;
        }
    };


}
