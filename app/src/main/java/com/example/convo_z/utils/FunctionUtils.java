package com.example.convo_z.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.example.convo_z.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FunctionUtils {
    public static String emptyDetector(String fullName, String email, String password, String rePassword) {
        String result = "";
        if (fullName.isEmpty()) {
            result += 0;
        } else {
            result += 1;
        }
        if (email.isEmpty()) {
            result += 0;
        } else {
            result += 1;
        }
        if (password.isEmpty()) {
            result += 0;
        } else {
            result += 1;
        }
        if (rePassword.isEmpty()) {
            result += 0;
        } else {
            result += 1;
        }
        return result;
    }

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

    public static ArrayList<String> getContactsList(android.content.Context context) {
        ArrayList<String> contacts = new ArrayList<>();

        Cursor phones = null;
        try {
            String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
            phones = Objects.requireNonNull(context).getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, SELECTION, null, null);
        } catch (Exception e) {
            Toast.makeText(context, "An error occurred,please try again", Toast.LENGTH_SHORT).show();
        }

        while (true) {
            assert phones != null;
            if (!(phones.moveToNext() && phones.getCount() > 0)) break;
            //  String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replaceAll("\\s", "");
            if (phoneNumber.length() >= 10) {
                if (phoneNumber.length() == 10) {
                    phoneNumber = "+91" + phoneNumber;
                } else if (phoneNumber.startsWith("91")) {
                    phoneNumber = "+" + phoneNumber;
                }
                contacts.add(phoneNumber);
            }
        }
        phones.close();
        return contacts;
    }

    public static ProgressDialog getProgressDialog(String title, String message, Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        //  progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_progress_dialog));
        return progressDialog;
    }

    public static Snackbar getSnackBar(String message, View view) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> {
        });
        return snackbar;
    }
}
