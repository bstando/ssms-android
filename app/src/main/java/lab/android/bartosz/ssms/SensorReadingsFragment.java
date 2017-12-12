package lab.android.bartosz.ssms;


import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SensorReadingsFragment extends Fragment {


    public SensorReadingsFragment() {
        // Required empty public constructor
    }

    public interface SensorReadingsFragmentListener {
        public void loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor_readings, container, false);
    }

}
