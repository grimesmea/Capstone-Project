package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * {@link HedgehogCollectionAdapter} exposes a list of hedgehogs from a
 * {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class HedgehogCollectionAdapter extends RecyclerView.Adapter<HedgehogCollectionAdapter.HedgehogCollectionAdapterViewHolder> {

    private final String LOG_TAG = HedgehogCollectionAdapter.class.getSimpleName();

    final private Context mContext;
    final private View mEmptyView;
    final private HedgehogCollectionAdapterOnClickHandler mClickHandler;
    final private ItemChoiceManager mItemChoiceManager;

    private Cursor mCursor;

    public HedgehogCollectionAdapter(Context context,
                                     HedgehogCollectionAdapterOnClickHandler onClickHandler,
                                     View emptyView,
                                     boolean isSingleChoiceMode) {
        mContext = context;
        mClickHandler = onClickHandler;
        mEmptyView = emptyView;
        mItemChoiceManager = new ItemChoiceManager(this, isSingleChoiceMode);
    }

    @Override
    public HedgehogCollectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.list_item_hedgehog;

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            HedgehogCollectionAdapterViewHolder hedgehogCollectionAdapterViewHolder =
                    new HedgehogCollectionAdapterViewHolder(view);

            return hedgehogCollectionAdapterViewHolder;
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(HedgehogCollectionAdapterViewHolder viewHolder, int position) {
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);

        mCursor.moveToPosition(position);
        updateHedgehogViews(mCursor, viewHolder);

        mItemChoiceManager.onBindViewHolder(viewHolder, position);
    }

    private void updateHedgehogViews(Cursor cursor, final HedgehogCollectionAdapterViewHolder viewHolder) {
        Hedgehog hedgehog = new Hedgehog(cursor);
        int hedgehogImageResource;
        Drawable hedgehogDrawable;

        if (hedgehog.getIsSelected()) {
            viewHolder.selectedHedgehogTickImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectedHedgehogTickImageView.setVisibility(View.GONE);
        }

        if (hedgehog.getIsUnlocked()) {
            hedgehogImageResource = mContext.getResources().getIdentifier(hedgehog.getImageName(), "drawable", mContext.getPackageName());
            for (int i = 0; i < 5; i++) {
                if (i < hedgehog.getHappinessLevel()) {
                    viewHolder.heartImageViews[i].setImageResource(R.drawable.heart_filled);
                } else {
                    viewHolder.heartImageViews[i].setImageResource(R.drawable.heart_outline);
                }
            }
            viewHolder.heartsContainer.setContentDescription(
                    mContext.getString(R.string.hedgehog_hearts_content_description)
                            + mContext.getString(R.string.unlocked_hedgehog_heart_count_content_description)
                            + hedgehog.getHappinessLevel() + " out of " + 5);
            if (hedgehog.getIsSelected()) {
                viewHolder.hedgehogImageView.setContentDescription(mContext.getString(R.string.selected_hedgehog_image_content_description));
            } else {
                viewHolder.hedgehogImageView.setContentDescription(mContext.getString(R.string.unlocked_hedgehog_image_content_description));
            }


        } else {
            hedgehogImageResource = mContext.getResources()
                    .getIdentifier(hedgehog.getSilhouetteImageName(), "drawable", mContext.getPackageName());
            for (int i = 0; i < 5; i++) {
                viewHolder.heartImageViews[i].setImageResource(R.drawable.heart_filled_grey);
            }
            viewHolder.heartsContainer.setContentDescription(
                    mContext.getString(R.string.hedgehog_hearts_content_description)
                            + mContext.getString(R.string.locked_hedgehog_heart_count_content_description));

            viewHolder.hedgehogImageView.setContentDescription(mContext.getString(R.string.locked_hedgehog_image_content_description));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hedgehogDrawable = mContext.getResources().getDrawable(hedgehogImageResource, mContext.getTheme());
        } else {
            hedgehogDrawable = mContext.getResources().getDrawable(hedgehogImageResource);
        }

        viewHolder.hedgehogImageView.setImageDrawable(hedgehogDrawable);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof HedgehogCollectionAdapterViewHolder) {
            HedgehogCollectionAdapterViewHolder hedgehogCollectionAdapterViewHolder = (HedgehogCollectionAdapterViewHolder) viewHolder;
            hedgehogCollectionAdapterViewHolder.onClick(hedgehogCollectionAdapterViewHolder.itemView);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mItemChoiceManager.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mItemChoiceManager.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return mItemChoiceManager.getSelectedItemPosition();
    }

    public interface HedgehogCollectionAdapterOnClickHandler {
        void onClick(int hedgehogId, HedgehogCollectionAdapterViewHolder viewHolder);
    }

    /**
     * Cache of the children views for a hedgehog list item.
     */
    public class HedgehogCollectionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LinearLayout hedgehogContainer;
        public ImageView hedgehogImageView;
        public ImageView selectedHedgehogTickImageView;
        public ImageView heartImage1;
        public ImageView heartImage2;
        public ImageView heartImage3;
        public ImageView heartImage4;
        public ImageView heartImage5;
        public RelativeLayout heartsContainer;
        public ImageView[] heartImageViews;

        public HedgehogCollectionAdapterViewHolder(View view) {
            super(view);
            hedgehogContainer = (LinearLayout) view.findViewById(R.id.hedgehog_container);
            selectedHedgehogTickImageView = (ImageView) view.findViewById(R.id.selected_hedgehog_tick);
            hedgehogImageView = (ImageView) view.findViewById(R.id.hedgehog_image);
            heartImage1 = (ImageView) view.findViewById(R.id.heart_1);
            heartImage2 = (ImageView) view.findViewById(R.id.heart_2);
            heartImage3 = (ImageView) view.findViewById(R.id.heart_3);
            heartImage4 = (ImageView) view.findViewById(R.id.heart_4);
            heartImage5 = (ImageView) view.findViewById(R.id.heart_5);
            heartsContainer = (RelativeLayout) view.findViewById(R.id.hearts_container);

            heartImageViews = new ImageView[]{
                    heartImage1, heartImage2, heartImage3, heartImage4, heartImage5
            };

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getInt(TodayStepsFragment.COL_HEDGEHOG_ID), this);
            mItemChoiceManager.onClick(this);
        }
    }
}

