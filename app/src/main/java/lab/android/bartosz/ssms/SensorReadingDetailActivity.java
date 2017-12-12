package lab.android.bartosz.ssms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class SensorReadingDetailActivity extends AppCompatActivity {

    protected SensorService sensorService;
    protected boolean bounded = false;

    private long id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }
        setContentView(R.layout.activity_sensor_reading_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.sensorReadingDetailMenu);
        setSupportActionBar(toolbar);
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {

            id = extrasBundle.getLong("id");
        }

    }

    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
    }

    @Override
    protected void onDestroy()
    {
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
        super.onDestroy();
    }

    void generate() {
        SensorData sensorData = sensorService.getByID(id);
        makeInterface(sensorData);
    }


    void makeInterface(final SensorData sensorData) {
        final TextView sensorDataId = (TextView) findViewById(R.id.readingIDTextView);
        final TextView sensorDataDate = (TextView) findViewById(R.id.readingDateTextView);
        final EditText sensorDataDeviceId = (EditText) findViewById(R.id.readingDeviceIDEditText);
        final EditText sensorDataTemperature = (EditText) findViewById(R.id.readingTemperatureEditText);
        final EditText sensorDataHumidity = (EditText) findViewById(R.id.readingHumidityEditText);
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
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorService.removeFromDatabase(sensorData.getId());
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bounded = false;
        }
    };

}
