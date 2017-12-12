package lab.android.bartosz.ssms;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class DeviceInfoDialog extends AppCompatDialogFragment {
    DeviceInfo deviceInfo;

    public DeviceInfoDialog() {

    }

    public static DeviceInfoDialog createNewInstance(DeviceInfo di) {
        DeviceInfoDialog deviceInfoDialog = new DeviceInfoDialog();
        Bundle args = new Bundle();
        args.putSerializable("data", di);
        deviceInfoDialog.setArguments(args);
        return deviceInfoDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deviceinfo_dialog_layout, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deviceInfo = (DeviceInfo) getArguments().getSerializable("data");
        TextView deviceInfoIDTV = (TextView) getView().findViewById(R.id.deviceInfoIDTV);
        TextView deviceInfoNameTV = (TextView) getView().findViewById(R.id.deviceInfoNameTV);
        TextView deviceInfoLocTV = (TextView) getView().findViewById(R.id.deviceInfoLocTV);

        String id = String.valueOf(deviceInfoIDTV.getText()) + " " + deviceInfo.getId();
        String name = String.valueOf(deviceInfoNameTV.getText()) + " " + deviceInfo.getName();
        String loc = String.valueOf(deviceInfoLocTV.getText()) + " " + deviceInfo.getLocalization();


        deviceInfoIDTV.setText(id);
        deviceInfoNameTV.setText(name);
        deviceInfoLocTV.setText(loc);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppDialogTheme);
    }
}
