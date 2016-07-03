package grimesmea.gmail.com.pricklefit;


import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;


/**
 * Displays details for the specified hedgehog.
 */
public class HedgehogDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "HEDGEHOG_URI";
    private static final String LOG_TAG = HedgehogDetailFragment.class.getSimpleName();
    private static final int HEDGEHOG_LOADER = 200;

    private Uri mUri;

    private Hedgehog hedgehog;
    private ImageView hedgehogImageView;
    private TextView hedgehogDescriptionView;
    private ImageView heartImage1;
    private ImageView heartImage2;
    private ImageView heartImage3;
    private ImageView heartImage4;
    private ImageView heartImage5;
    private ImageView[] heartImageViews;

    private FloatingActionButton selectionFab;

    public HedgehogDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }
        getLoaderManager().initLoader(HEDGEHOG_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hedgehog_detail, container, false);

        selectionFab = (FloatingActionButton) rootView.findViewById(R.id.select_hedgehog_fab);
        selectionFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSelectedHedgehogInContentProvider(hedgehog);
                selectionFab.setVisibility(View.GONE);
            }
        });

        hedgehogImageView = (ImageView) rootView.findViewById(R.id.hedgehog_image);
        hedgehogDescriptionView = (TextView) rootView.findViewById(R.id.hedgehog_description);
        heartImage1 = (ImageView) rootView.findViewById(R.id.heart_1);
        heartImage2 = (ImageView) rootView.findViewById(R.id.heart_2);
        heartImage3 = (ImageView) rootView.findViewById(R.id.heart_3);
        heartImage4 = (ImageView) rootView.findViewById(R.id.heart_4);
        heartImage5 = (ImageView) rootView.findViewById(R.id.heart_5);
        heartImageViews = new ImageView[]{
                heartImage1, heartImage2, heartImage3, heartImage4, heartImage5
        };

        return rootView;
    }

    public void setSelectedHedgehogInContentProvider(Hedgehog hedgehog) {
        ContentValues selectedValue = new ContentValues();
        ContentValues unselectedValue = new ContentValues();

        selectedValue.put(HedgehogsEntry.COLUMN_SELECTED_STATUS, (byte) 1);
        unselectedValue.put(HedgehogsEntry.COLUMN_SELECTED_STATUS, (byte) 0);

        getActivity().getContentResolver().update(
                HedgehogsEntry.CONTENT_URI,
                unselectedValue,
                HedgehogsEntry.COLUMN_SELECTED_STATUS + "= ?",
                new String[]{Integer.toString(1)}
        );

        getActivity().getContentResolver().update(
                HedgehogsEntry.buildHedgehogUri(hedgehog.getId()),
                selectedValue,
                null,
                null
        );
    }

    private void updateHedgehogViews() {
        int hedgehogImageResource;
        String hedgehogDescription;
        Drawable hedgehogDrawable;

        if (hedgehog.getIsUnlocked()) {
            hedgehogImageResource = getContext().getResources().getIdentifier(hedgehog.getImageName(), "drawable", getContext().getPackageName());
            hedgehogDescription = hedgehog.getDescription();
            for (int i = 0; i < 5; i++) {
                if (i < hedgehog.getHappinessLevel()) {
                    heartImageViews[i].setImageResource(R.drawable.heart_filled);
                } else {
                    heartImageViews[i].setImageResource(R.drawable.heart_outline);
                }
            }

            if (!hedgehog.getIsSelected()) {
                selectionFab.setVisibility(View.VISIBLE);
            }
        } else {
            hedgehogImageResource = getContext().getResources().getIdentifier(hedgehog.getSilhouetteImageName(), "drawable", getContext().getPackageName());
            hedgehogDescription = getString(R.string.locked_hedgehog_description_placeholder);
            for (int i = 0; i < 5; i++) {
                heartImageViews[i].setImageResource(R.drawable.heart_filled_grey);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hedgehogDrawable = getContext().getResources().getDrawable(hedgehogImageResource, getContext().getTheme());
        } else {
            hedgehogDrawable = getContext().getResources().getDrawable(hedgehogImageResource);
        }

        hedgehogDescriptionView.setText(hedgehogDescription);
        hedgehogImageView.setImageDrawable(hedgehogDrawable);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            hedgehog = new Hedgehog(data);
            updateHedgehogViews();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        hedgehog = null;
    }
}
