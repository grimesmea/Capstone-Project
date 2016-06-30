package grimesmea.gmail.com.pricklefit;

import java.text.SimpleDateFormat;

/**
 * Created by comrade.marie on 6/29/2016.
 */
public class DailyStepsDTO implements Comparable<DailyStepsDTO> {

    private final long day;
    private final int steps;
    private final String formattedDay;

    public DailyStepsDTO(final long day, final int steps) {
        this.day = day;
        this.steps = steps;
        this.formattedDay = new SimpleDateFormat("EEE").format(day);
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
    public int compareTo(DailyStepsDTO another) {
        if (this.getDay() > another.getDay()) {
            return -1;
        }
        return 1;
    }
}
