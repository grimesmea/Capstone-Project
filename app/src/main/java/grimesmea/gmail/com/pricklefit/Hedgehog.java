package grimesmea.gmail.com.pricklefit;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;

public class Hedgehog implements Parcelable {

    public static final Parcelable.Creator<Hedgehog> CREATOR = new Parcelable.Creator<Hedgehog>() {
        @Override
        public Hedgehog createFromParcel(Parcel parcel) {
            return new Hedgehog(parcel);
        }

        @Override
        public Hedgehog[] newArray(int size) {
            return new Hedgehog[size];
        }
    };

    final static String NAME = "name";
    final static String IMAGE_NAME = "image_name";
    final static String SILHOUETTE_IMAGE_NAME = "silhouette_image_name";
    final static String DESCRIPTION = "description";
    final static String HAPPINESS_LEVEL = "happiness_level";
    final static String FITNESS_LEVEL = "fitness_level";
    final static String IS_UNLOCKED = "unlock_status";
    final static String IS_SELECTED = "selected_status";

    String name;
    String imageName;
    String silhouetteImageName;
    String description;
    int happinessLevel;
    int fitnessLevel;
    boolean isUnlocked;
    boolean isSelected;

    public Hedgehog(JSONObject hedgehogJson) throws JSONException {
        this(
                hedgehogJson.getString(NAME),
                hedgehogJson.getString(IMAGE_NAME),
                hedgehogJson.getString(SILHOUETTE_IMAGE_NAME),
                hedgehogJson.getString(DESCRIPTION),
                hedgehogJson.getInt(HAPPINESS_LEVEL),
                hedgehogJson.getInt(FITNESS_LEVEL),
                hedgehogJson.getBoolean(IS_UNLOCKED),
                hedgehogJson.getBoolean(IS_SELECTED)
        );
    }

    public Hedgehog(Cursor hedgehogCursor) {
        this(
                hedgehogCursor.getString(TodaysStepsFragment.COL_HEDGEHOG_NAME),
                hedgehogCursor.getString(TodaysStepsFragment.COL_HEDGEHOG_IMAGE_NAME),
                hedgehogCursor.getString(TodaysStepsFragment.COL_HEDGEHOG_SILHOUETTE_IMAGE_NAME),
                hedgehogCursor.getString(TodaysStepsFragment.COL_HEDGEHOG_DESCRIPTION),
                hedgehogCursor.getInt(TodaysStepsFragment.COL_HEDGEHOG_HAPPINESS_LEVEL),
                hedgehogCursor.getInt(TodaysStepsFragment.COL_HEDGEHOG_FITNESS_LEVEL),
                getBooleanValue(hedgehogCursor.getInt(TodaysStepsFragment.COL_HEDGEHOG_UNLOCK_STATUS)),
                getBooleanValue(hedgehogCursor.getInt(TodaysStepsFragment.COL_HEDGEHOG_SELECTED_STATUS))
        );
    }

    public Hedgehog(String name, String imageName, String silhouetteImageName, String description,
                    int happinessLevel, int fitnessLevel, boolean isUnlocked, boolean isSelected) {
        this.name = name;
        this.imageName = imageName;
        this.silhouetteImageName = silhouetteImageName;
        this.description = description;
        this.happinessLevel = happinessLevel;
        this.fitnessLevel = fitnessLevel;
        this.isUnlocked = isUnlocked;
        this.isSelected = isSelected;
    }

    private Hedgehog(Parcel parcel) {
        name = parcel.readString();
        imageName = parcel.readString();
        silhouetteImageName = parcel.readString();
        description = parcel.readString();
        happinessLevel = parcel.readInt();
        fitnessLevel = parcel.readInt();
        isUnlocked = parcel.readByte() != 0;
        isSelected = parcel.readByte() != 0;
    }

    private static boolean getBooleanValue(int i) {
        return i == 1 ? true : false;
    }

    public ContentValues createContentValues() {
        ContentValues hedgehogValues = new ContentValues();

        hedgehogValues.put(HedgehogsEntry.COLUMN_NAME, name);
        hedgehogValues.put(HedgehogsEntry.COLUMN_IMAGE_NAME, imageName);
        hedgehogValues.put(HedgehogsEntry.COLUMN_SILHOUETTE_IMAGE_NAME, silhouetteImageName);
        hedgehogValues.put(HedgehogsEntry.COLUMN_DESCRIPTION, description);
        hedgehogValues.put(HedgehogsEntry.COLUMN_HAPPINESS_LEVEL, happinessLevel);
        hedgehogValues.put(HedgehogsEntry.COLUMN_FITNESS_LEVEL, fitnessLevel);
        hedgehogValues.put(HedgehogsEntry.COLUMN_UNLOCK_STATUS, isUnlocked);
        hedgehogValues.put(HedgehogsEntry.COLUMN_SELECTED_STATUS, isSelected);

        return hedgehogValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(imageName);
        parcel.writeString(silhouetteImageName);
        parcel.writeString(description);
        parcel.writeInt(happinessLevel);
        parcel.writeInt(fitnessLevel);
        parcel.writeByte((byte) (isUnlocked ? 1 : 0));
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public String getSilhouetteImageName() {
        return silhouetteImageName;
    }

    public String getDescription() {
        return description;
    }

    public int getHappinessLevel() {
        return happinessLevel;
    }

    public int getFitnessLevelLevel() {
        return fitnessLevel;
    }

    public boolean getIsUnlocked() {
        return isUnlocked;
    }
}
