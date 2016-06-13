package grimesmea.gmail.com.pricklefit;


import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;


/**
 * Displays details for the specified hedgehog.
 */
public class HedgehogDetailFragment extends Fragment {

    private Hedgehog hedgehog;
    private ImageView hedgehogImageView;
    private ImageView heartImage1;
    private ImageView heartImage2;
    private ImageView heartImage3;
    private ImageView heartImage4;
    private ImageView heartImage5;
    private ImageView[] heartImageViews;

    public HedgehogDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            hedgehog = arguments.getParcelable("hedgehogParcelable");
        }

        if (hedgehog.getIsUnlocked() && !hedgehog.getIsSelected()) {
            setHasOptionsMenu(true);
        }

        getActivity().setTitle(hedgehog.getName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.hedgehog_detail_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_active:
                if (!item.isChecked() && hedgehog.getIsUnlocked()) {
                    item.setChecked(true);
                    setSelectedHedgehogInContentProvider(hedgehog);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSelectedHedgehogInContentProvider(Hedgehog hedgehog) {
        ContentValues selectedValue = new ContentValues();
        ContentValues unselectedValue = new ContentValues();

        selectedValue.put(HedgehogsEntry.COLUMN_SELECTED_STATUS, (byte) 1);
        unselectedValue.put(HedgehogsEntry.COLUMN_SELECTED_STATUS, (byte) 0);

        getActivity().getContentResolver().update(
                HedgehogsEntry.CONTENT_URI,
                selectedValue,
                HedgehogsEntry.COLUMN_SELECTED_STATUS + "= ?",
                new String[]{Integer.toString(1)}
        );

        getActivity().getContentResolver().update(
                HedgehogsEntry.CONTENT_URI,
                selectedValue,
                HedgehogsEntry.COLUMN_NAME + "= ?",
                new String[]{hedgehog.getName()}
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int hedgehogImageResource;
        Drawable hedgehogDrawable;

        View rootView = inflater.inflate(R.layout.fragment_hedgehog_detail, container, false);

        hedgehogImageView = (ImageView) rootView.findViewById(R.id.hedgehog_image);
        heartImage1 = (ImageView) rootView.findViewById(R.id.heart_1);
        heartImage2 = (ImageView) rootView.findViewById(R.id.heart_2);
        heartImage3 = (ImageView) rootView.findViewById(R.id.heart_3);
        heartImage4 = (ImageView) rootView.findViewById(R.id.heart_4);
        heartImage5 = (ImageView) rootView.findViewById(R.id.heart_5);
        heartImageViews = new ImageView[]{
                heartImage1, heartImage2, heartImage3, heartImage4, heartImage5
        };

        if (hedgehog.getIsUnlocked()) {
            hedgehogImageResource = getContext().getResources().getIdentifier(hedgehog.getImageName(), "drawable", getContext().getPackageName());
            for (int i = 0; i < 5; i++) {
                if (i < hedgehog.getHappinessLevel()) {
                    heartImageViews[i].setImageResource(R.drawable.heart_filled);
                } else {
                    heartImageViews[i].setImageResource(R.drawable.heart_outline);
                }
            }
        } else {
            hedgehogImageResource = getContext().getResources().getIdentifier(hedgehog.getSilhouetteImageName(), "drawable", getContext().getPackageName());
            for (int i = 0; i < 5; i++) {
                heartImageViews[i].setImageResource(R.drawable.heart_filled_grey);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hedgehogDrawable = getContext().getResources().getDrawable(hedgehogImageResource, getContext().getTheme());
        } else {
            hedgehogDrawable = getContext().getResources().getDrawable(hedgehogImageResource);
        }
        hedgehogImageView.setImageDrawable(hedgehogDrawable);

        return rootView;
    }
}
