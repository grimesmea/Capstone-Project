package grimesmea.gmail.com.pricklefit;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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

    private final int MIN_HAPPINESS_LEVEL = 0;
    private final int MAX_HAPPINESS_LEVEL = 5;

    int id;
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

    public Hedgehog(Cursor cursor) {
        this(
                cursor.getInt(TodayStepsFragment.COL_HEDGEHOG_ID),
                cursor.getString(TodayStepsFragment.COL_HEDGEHOG_NAME),
                cursor.getString(TodayStepsFragment.COL_HEDGEHOG_IMAGE_NAME),
                cursor.getString(TodayStepsFragment.COL_HEDGEHOG_SILHOUETTE_IMAGE_NAME),
                cursor.getString(TodayStepsFragment.COL_HEDGEHOG_DESCRIPTION),
                cursor.getInt(TodayStepsFragment.COL_HEDGEHOG_HAPPINESS_LEVEL),
                cursor.getInt(TodayStepsFragment.COL_HEDGEHOG_FITNESS_LEVEL),
                getBooleanValue(cursor.getInt(TodayStepsFragment.COL_HEDGEHOG_UNLOCK_STATUS)),
                getBooleanValue(cursor.getInt(TodayStepsFragment.COL_HEDGEHOG_SELECTED_STATUS))
        );
    }

    public Hedgehog(int id, String name, String imageName, String silhouetteImageName, String description,
                    int happinessLevel, int fitnessLevel, boolean isUnlocked, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.imageName = imageName;
        this.silhouetteImageName = silhouetteImageName;
        this.description = description;
        this.happinessLevel = happinessLevel;
        this.fitnessLevel = fitnessLevel;
        this.isUnlocked = isUnlocked;
        this.isSelected = isSelected;
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
        id = parcel.readInt();
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
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(imageName);
        parcel.writeString(silhouetteImageName);
        parcel.writeString(description);
        parcel.writeInt(happinessLevel);
        parcel.writeInt(fitnessLevel);
        parcel.writeByte((byte) (isUnlocked ? 1 : 0));
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }

    public int calculateHappinessChange(int dailySteps, int dailyStepGoal) {
        if (dailySteps / fitnessLevel >= dailyStepGoal / 2) {
            return 1;
        } else if (dailySteps / fitnessLevel < dailyStepGoal / 4) {
            return -1;
        } else {
            return 0;
        }
    }

    public int calculateNewHappinessLevel(List<Integer> happinessChanges) {
        int newHappinessLevel = happinessLevel;
        for (int happinessChange : happinessChanges) {
            int potentialNewHappinessLevel = newHappinessLevel + happinessChange;
            if (potentialNewHappinessLevel >= MIN_HAPPINESS_LEVEL && potentialNewHappinessLevel <= MAX_HAPPINESS_LEVEL) {
                newHappinessLevel = potentialNewHappinessLevel;
            }
        }
        return newHappinessLevel;
    }

    public void updateHappinessLevel(int newHappinessLevel, Activity activity) {
        if (newHappinessLevel != happinessLevel) {
            ContentValues happinessLevelValue = new ContentValues();
            happinessLevelValue.put(HedgehogsEntry.COLUMN_HAPPINESS_LEVEL, newHappinessLevel);
            activity.getContentResolver().update(
                    HedgehogsEntry.buildHedgehogUri(this.getId()),
                    happinessLevelValue,
                    null,
                    null
            );
        }
    }

    public void checkForUnlock(List<DailyStepsDTO> dailyStepTotals, int dailyStepGoal, Activity activity) {
        if (isUnlocked) {
            return;
        }

        for (DailyStepsDTO dailySteps : dailyStepTotals) {
            if (dailySteps.getSteps() >= (dailyStepGoal / 2 * fitnessLevel)) {
                unlockHedgehog(activity);
                return;
            }
        }
    }

    public void unlockHedgehog(Activity activity) {
        if (isUnlocked) {
            return;
        }

        ContentValues unlockStatusValue = new ContentValues();
        unlockStatusValue.put(HedgehogsEntry.COLUMN_UNLOCK_STATUS, (byte) 1);
        activity.getContentResolver().update(
                HedgehogsEntry.buildHedgehogUri(this.getId()),
                unlockStatusValue,
                null,
                null
        );

        Context context = activity;
        CharSequence text = name + " is now unlocked!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public int getId() {
        return id;
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

    public int getFitnessLevel() {
        return fitnessLevel;
    }

    public boolean getIsUnlocked() {
        return isUnlocked;
    }

    public boolean getIsSelected() {
        return isSelected;
    }
}
