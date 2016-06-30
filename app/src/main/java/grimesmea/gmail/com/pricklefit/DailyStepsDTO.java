package grimesmea.gmail.com.pricklefit;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;

/**
 * Created by comrade.marie on 6/29/2016.
 */
public class DailyStepsDTO implements Parcelable, Comparable<DailyStepsDTO> {

    public static final Parcelable.Creator<DailyStepsDTO> CREATOR = new Parcelable.Creator<DailyStepsDTO>() {
        public DailyStepsDTO createFromParcel(Parcel in) {
            return new DailyStepsDTO(in);
        }

        public DailyStepsDTO[] newArray(int size) {
            return new DailyStepsDTO[size];
        }
    };
    private final long day;
    private final int steps;
    private final String formattedDay;

    public DailyStepsDTO(final long day, final int steps) {
        this.day = day;
        this.steps = steps;
        this.formattedDay = new SimpleDateFormat("EEE").format(day);
    }


    private DailyStepsDTO(Parcel in) {
        day = in.readLong();
        steps = in.readInt();
        formattedDay = new SimpleDateFormat("EEE").format(day);
    }

    public String getFormattedDay() {
        return formattedDay;
    }

    public int getSteps() {
        return steps;
    }

    public long getDay() {
        return day;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(day);
        dest.writeInt(steps);
    }

    @Override
    public int compareTo(DailyStepsDTO another) {
        if (this.getDay() > another.getDay()) {
            return -1;
        }
        return 1;
    }
}
