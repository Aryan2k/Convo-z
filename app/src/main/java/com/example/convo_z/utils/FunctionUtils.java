package com.example.convo_z.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import androidx.annotation.RequiresApi;
import androidx.viewbinding.ViewBinding;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class FunctionUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String timeSetter(String time) {

        String result = "";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        Date sDate = new Date(Long.parseLong(time));
        Date eDate = new Date(new Date().getTime());

        String startTime = simpleDateFormat.format(sDate);
        String endTime = simpleDateFormat.format(eDate);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        LocalTime start = LocalTime.parse(startTime, timeFormatter);
        LocalTime end = LocalTime.parse(endTime, timeFormatter);
        Duration diff = Duration.between(start, end);

        long hours = Math.abs(diff.toHours());
        long minutes = Math.abs(diff.minusHours(hours).toMinutes());


        if (hours < 1) {
            if (minutes == 0)
                result += "Just Now";
            else if (minutes == 1)
                result += "A minute ago";
            else
                result += minutes + " minutes ago";
        } else {

            String minute;
            String hour;

            int min = start.getMinute();
            if (min < 10) {
                minute = "0" + min;
            } else {
                minute = String.valueOf(min);
            }

            int hr = start.getHour();
            if (hr < 10) {
                hour = "0" + hr;
            } else {
                hour = String.valueOf(hr);
            }

            if (start.isAfter(end))
                result += "Yesterday, " + hour + ":" + minute;
            else
                result += "Today, " + hour + ":" + minute;
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void hideSystemUI(ViewBinding binding) {
        binding.getRoot().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showSystemUI(ViewBinding binding) {
        binding.getRoot().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
