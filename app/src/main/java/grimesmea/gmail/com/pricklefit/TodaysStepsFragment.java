package grimesmea.gmail.com.pricklefit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Displays daily step count and goal.
 */
public class TodaysStepsFragment extends Fragment {


    public TodaysStepsFragment() {
        // Empty constructor required for fragment subclasses
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_todays_steps, container, false);
    }
}
