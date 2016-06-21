package grimesmea.gmail.com.pricklefit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class HedgehogCollectionActivity extends AppCompatActivity implements HedgehogCollectionFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = HedgehogCollectionActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        setContentView(R.layout.activity_hedgehog_collection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (findViewById(R.id.hedgehog_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                HedgehogDetailFragment fragment = new HedgehogDetailFragment();
                if (contentUri != null) {
                    Bundle args = new Bundle();
                    args.putParcelable(HedgehogDetailFragment.DETAIL_URI, contentUri);
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.hedgehog_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(HedgehogDetailFragment.DETAIL_URI, contentUri);

            HedgehogDetailFragment fragment = new HedgehogDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.hedgehog_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, HedgehogDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
