package com.llightning.funnumbers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

interface ISelectedData {
    void onSelectedData(Bundle bundle);
}

public class MainActivity extends AppCompatActivity implements ISelectedData{
    private SwipeRefreshLayout swipeRefreshLayout;
    private NumberPicker mode_selector;
    private final String[] mode_options = new String[]{"Trivia", "Math", "Date"};
    private String selected_mode;
    private TextView tv_input_instruction;
    private EditText et_input_number;
    private Button btn_submit_number;
    private TextView tv_selected_date;
    private final Date selected_date = new Date();
    private TextView tv_facts;
    private AdView mAdView;
    private final DialogFragment datePicker = new DatePickerFragment();
    private final static String numberapiBaseUrl = "http://numbersapi.com/";
    private final static int REQUEST_INTERNET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup views
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_main);
        mode_selector = findViewById(R.id.mode_picker);
        tv_input_instruction = findViewById(R.id.tv_input_instruction);
        et_input_number = findViewById(R.id.et_number);
        btn_submit_number = findViewById(R.id.btn_submit_number);
        tv_selected_date = findViewById(R.id.tv_enter_date);
        Button btn_random = findViewById(R.id.btn_random);
        tv_facts = findViewById(R.id.tv_facts);
        Button btn_about = findViewById(R.id.btn_about);
        TextView tv_about = findViewById(R.id.tv_about);
        tv_about.setMovementMethod(LinkMovementMethod.getInstance());
        // Set options for mode_selector
        initializeModePicker();
        mode_selector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                selected_mode = mode_options[numberPicker.getValue()];
                Log.d("Mode change", "Changed selected mode to " + selected_mode);
                handleModeChange();
            }
        });
        // Ask for permissions if needed
        managePermission();

        // Initialise Google Mobile Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Number input
        btn_submit_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFact();
            }
        });
        // Set a Date
        tv_selected_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show(getSupportFragmentManager(), "datePicker");
            }
        });
        // Random button
        btn_random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFact(true);
            }
        });
        // About button
        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AboutActivity.class);
                startActivity(intent);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFact();
            }
        });

    }

    private void initializeModePicker(){
        mode_selector.setMinValue(0);
        mode_selector.setMaxValue(mode_options.length-1);
        mode_selector.setDisplayedValues(mode_options);
        selected_mode = mode_options[0];
    }

    private void handleModeChange(){
        if(selected_mode.equals("Date")){
            et_input_number.setVisibility(View.GONE);
            btn_submit_number.setVisibility(View.GONE);
            tv_selected_date.setVisibility(View.VISIBLE);
            tv_input_instruction.setVisibility(View.GONE);
        }
        else{
            et_input_number.setVisibility(View.VISIBLE);
            btn_submit_number.setVisibility(View.VISIBLE);
            tv_selected_date.setVisibility(View.GONE);
            tv_input_instruction.setVisibility(View.VISIBLE);
        }
        tv_facts.setText("");
    }

    private void managePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkPermission(Manifest.permission.INTERNET)){
                Log.d("Permission", "Internet permission already granted");
            }
            else if (shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)){
                    requestPermissions(new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET);
                    Log.d("Permission", "Permission asked for Internet");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_INTERNET:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Permission granted, Thank you!", Toast.LENGTH_SHORT).show();
                    Log.d("Permission", "Internet permission granted");
                }
                else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Log.d("Permission", "Internet permission denied");
                    if (shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)){
                        Log.d("Permission", "Should ask: Creation of alert builder");
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("This permission is needed for this app. Please allow it");
                        builder.setTitle("Internet permission needed");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d("Permission", "User clicked on OK");
                                requestPermissions(new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET);
                            }
                        });
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d("Permission", "User clicked on No");
                                Toast.makeText(getApplicationContext(), "You won't be able to use this app! ;( ", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.show();
                    }
                }
                break;
            default:
                Log.e("Permission", "Invalid requestCode");
                break;
        }
    }

    private boolean checkPermission(String permission){
        return (ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSelectedData(Bundle bundle) {
        selected_date.setYear(bundle.getInt("year"));
        selected_date.setMonth(bundle.getInt("month"));
        selected_date.setDay(bundle.getInt("day"));
        String date_str = String.format("%d-%02d-%02d", selected_date.getYear(), selected_date.getMonth(), selected_date.getDay());
        tv_selected_date.setText(date_str);
        updateFact();
    }

    private void updateFact(boolean random){
        Log.d("updateFact", "Updating fact ...");
        FactUpdater factUpdater = new FactUpdater(random);
        factUpdater.execute();
    }

    private void updateFact(){
        updateFact(false);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private ISelectedData mCallback;

        @Override
        public void onAttach(@NonNull Activity activity) {
            super.onAttach(activity);
            mCallback = (ISelectedData) activity;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            int year, month, day;
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Bundle bundle = new Bundle();
            bundle.putInt("year", year);
            bundle.putInt("month", month+1);
            bundle.putInt("day", day);
            mCallback.onSelectedData(bundle);
        }
    }

    private class FactUpdater extends AsyncTask<Void, Void, Void>
    {
        private boolean random;
        private String factResult = null;

        FactUpdater(boolean random){
            this.random = random;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Set text to say the task is running in background
            tv_facts.setText(R.string.retrieving_fact);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String url;
            JSONObject jsonResponse;
            if(selected_mode.equals("Date")){
                if(random){
                    url = numberapiBaseUrl + "random/date?json";
                    jsonResponse = getJsonResponse(url);
                    String dateFact = processResponse(jsonResponse);
                    if (dateFact != null)
                        factResult = dateFact;
                    // get year info
                    url = numberapiBaseUrl + "random/year?json";
                    jsonResponse = getJsonResponse(url);
                    String yearFact = processResponse(jsonResponse);
                    if (yearFact != null && factResult != null)
                        factResult += System.getProperty("line.separator") + yearFact;
                }
                else{
                    // get date info
                    if (selected_date.getDay()!=-1 && selected_date.getMonth()!=-1 && selected_date.getYear()!=-1) {
                        url = numberapiBaseUrl + String.valueOf(selected_date.getMonth()) + "/" + String.valueOf(selected_date.getDay()) + "/date?json";
                        jsonResponse = getJsonResponse(url);
                        String dateFact = processResponse(jsonResponse);
                        if (dateFact != null)
                            factResult = dateFact;
                        // get year info
                        url = numberapiBaseUrl + String.valueOf(selected_date.getYear()) + "/year?json";
                        jsonResponse = getJsonResponse(url);
                        String yearFact = processResponse(jsonResponse);
                        if (yearFact != null && factResult != null)
                            factResult += System.getProperty("line.separator") + yearFact;
                    }
                    else {
                        factResult = "Please input a valid date";
                    }
                }
                tv_selected_date.setText(String.format("%d-%02d-%02d", selected_date.getYear(), selected_date.getMonth(), selected_date.getDay()));
            }
            else {
                String number_str = et_input_number.getText().toString();
                if(random){
                    number_str = "random";
                }
                if (number_str.length()>0) {
                    url = numberapiBaseUrl + number_str + "/" + selected_mode.toLowerCase() + "?json";
                    jsonResponse = getJsonResponse(url);
                    String numberFact = processResponse(jsonResponse);
                    if (numberFact != null)
                        factResult = numberFact;
                }
                else {
                    factResult = "Please input a number";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (factResult != null)
                tv_facts.setText(factResult);
            else
                tv_facts.setText(R.string.error_fact);
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }

        @Nullable
        private JSONObject getJsonResponse(String url){
            JSONObject jsonResponse = null;
            String response = HttpHandler.textFromUrl(url);
            if (response != null){
                try{
                    jsonResponse = new JSONObject(response);
                } catch (JSONException e) {
                    Log.e("FactUpdated", "Null response. Some error occurred");
                }
            }
            return jsonResponse;
        }

        @Nullable
        private String processResponse(JSONObject jsonResponse){
            String textResult = null;
            try {
                String type = jsonResponse.getString("type");
                textResult = jsonResponse.getString("text");
                int number = jsonResponse.getInt("number");
                if (type.equals("date")){
                    Date date = new Date(number);
                    selected_date.setDay(date.getDay());
                    selected_date.setMonth(date.getMonth());
                }
                else if (type.equals("year")){
                    selected_date.setYear(number);
                }
                else{
                    et_input_number.setText(String.valueOf(number));
                }
            } catch (JSONException e) {
                Log.e("FactUpdater", e.toString());
            }
            return textResult;
        }

    }

}