package lab.android.bartosz.ssms;


import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SensorDialog extends AppCompatDialogFragment {
    SensorData data;


    public SensorDialog() {

    }

    public static SensorDialog createNewInstance(SensorData data) {
        SensorDialog dialog = new SensorDialog();
        Bundle args = new Bundle();
        args.putSerializable("data", data);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_dialog_layout, container);
        SensorDialog.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //return getActivity().getLayoutInflater().inflate(R.layout.sensor_dialog_layout, container);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        data = (SensorData) getArguments().getSerializable("data");
        TextView readingIDTV = (TextView) getView().findViewById(R.id.sensorFragmentHead);
        TextView readingSensorIDTV = (TextView) getView().findViewById(R.id.sensorFragmentSensorID);
        TextView readingDateTV = (TextView) getView().findViewById(R.id.sensorFragmentDate);
        TextView readingTemperatureTV = (TextView) getView().findViewById(R.id.sensorFragmentTemperature);
        TextView readingHumidityTV = (TextView) getView().findViewById(R.id.sensorFragmentHumidity);

        String id = String.valueOf(readingIDTV.getText()) + " " + data.getId();
        String sensorID = String.valueOf(readingSensorIDTV.getText()) + " " + data.getSensorId();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = String.valueOf(readingDateTV.getText()) + " " + dateFormat.format(data.getDate());
        String temperature = String.valueOf(readingTemperatureTV.getText()) + " " + data.getTemperature();
        String humidity = String.valueOf(readingHumidityTV.getText()) + " " + data.getHumidity();


        if (data.getId() > 0) {
            readingIDTV.setText(id);
        } else {
            readingIDTV.setText(getString(R.string.sensor_dialog_addr));
        }
        readingSensorIDTV.setText(sensorID);
        readingDateTV.setText(date);
        readingTemperatureTV.setText(temperature);
        readingHumidityTV.setText(humidity);


    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppDialogTheme);
    }
}
