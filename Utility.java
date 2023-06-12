package com.dktes.medileaf;

import java.time.LocalDateTime;

public class Utility {

    // This utility class is used for miscellaneous operations in the app.
    // Currently, we only have one operation to greet user according to the time of the day

    public static String getMessage() {
        /*
        * When the app starts, user will be greeted with a warm message depending on time of the day.
        * For example, from midnight 12 to morning 11:59, the app would greet user with a "Good Morning"
        * */
        int hourOfDay = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hourOfDay = LocalDateTime.now().getHour();
        }

        if (0 <= hourOfDay && hourOfDay < 12) {
            return "Good Morning";
        } else if(12 <= hourOfDay && hourOfDay < 16) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

}
