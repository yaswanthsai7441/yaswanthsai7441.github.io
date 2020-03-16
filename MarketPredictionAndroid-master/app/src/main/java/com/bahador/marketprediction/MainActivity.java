package com.bahador.marketprediction;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Objects;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILES = "file:///android_asset/optimized_stock_prediction.pb";
    private static final String INPUT_NODES = "x_input";
    private static final int[] INPUT_SIZE = {1, 5};
    private static final String OUTPUT_NODE = "y_output";
    private TensorFlowInferenceInterface inferenceInterface;
    private DatePickerDialog.OnDateSetListener startDateSetListener, endDateSetListener;

    TextView startDate, endDate, result;
    Button getPredictionResult;
    Spinner stockSpinner;
    Calendar calendar = Calendar.getInstance();

    private LocalSelectedDate mStartDate, mEndDate;
    private String symbol, initialDate, finalDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(getAssets(), MODEL_FILES);
        stockSpinner = findViewById(R.id.stock_spinner);
        startDate = findViewById(R.id.start_date_textView);
        endDate = findViewById(R.id.end_date_textView);
        result = findViewById(R.id.result_textView);
        getPredictionResult = findViewById(R.id.get_prediction_result);

        ArrayAdapter<String> spinner = new ArrayAdapter<String>
                (this, R.layout.spinner_item, StockMarkets.STOCK_MARKETS) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
                assert parent != null;
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0)
                    textView.setTextColor(GRAY);
                else
                    textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                return view;
            }
        };

        spinner.setDropDownViewResource(R.layout.spinner_item);
        stockSpinner.setAdapter(spinner);
        stockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemSelected = (String) parent.getItemAtPosition(position);
                if (position > 0)
                    Toast.makeText(getApplicationContext(), "Selected item: " + itemSelected, Toast.LENGTH_SHORT).show();
                symbol = itemSelected;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year, month, day;
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog
                        (MainActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                startDateSetListener,
                                year, month, day);
                Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        startDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                mStartDate = new LocalSelectedDate(year, month, dayOfMonth);
                startDate.setText(year + "/" + String.format("%02d", month) + "/" + String.format("%02d", dayOfMonth));
                initialDate = startDate.getText().toString();
                initialDate = initialDate.replace("/", "");

            }
        };

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year, month, day;
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog
                        (MainActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                endDateSetListener, year, month, day);
                Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        endDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                mEndDate = new LocalSelectedDate(year, month, dayOfMonth);
                endDate.setText(year + "/" + String.format("%02d", month) + "/" + String.format("%02d", dayOfMonth));
                finalDate = endDate.getText().toString();
                finalDate = finalDate.replace("/", "");
            }
        };

        getPredictionResult.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
//                Date today = new Date();
//                LocalDate localDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); to compare date
                Toast.makeText(getApplicationContext(), initialDate, Toast.LENGTH_SHORT).show();
                String urlString = "https://marketdata.websol.barchart.com/getHistory.json?" +
                        "apikey=92cf55f827a639c549214a6462bcd484&symbol=" +
                        symbol + "&type=daily&startDate=" + initialDate + "&endDate=" + finalDate;
                new JsonTask().execute(urlString);
            }
        });
    }

    private void parseJSONData(String jsonString) {
        try {
            float open, close, high, low, volume;

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray result = jsonObject.getJSONArray("results");
            JSONObject todaysDaya = result.getJSONObject(result.length() - 1);
            JSONObject yesterdaysData = result.getJSONObject(result.length() - 2);

            open = (float) (todaysDaya.getDouble("open") - yesterdaysData.getDouble("open"));
            open = (open >= 0) ? 1 : 0;// if (open >= 0) open = 1 else open = 0;

            close = (float) (todaysDaya.getDouble("close") - yesterdaysData.getDouble("close"));
            close = (close >= 0) ? 1 : 0;

            high = (float) (todaysDaya.getDouble("high") - yesterdaysData.getDouble("high"));
            high = (high >= 0) ? 1 : 0;

            low = (float) (todaysDaya.getDouble("low") - yesterdaysData.getDouble("low"));
            low = (low >= 0) ? 1 : 0;

            volume = (float) (todaysDaya.getDouble("volume") - yesterdaysData.getDouble("volume"));
            volume = (volume >= 0) ? 1 : 0;

            float[] inputs = {open, close, high, low, volume};
            runInfrence(inputs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void runInfrence(float[] inputs) {
        inferenceInterface.fillNodeFloat(INPUT_NODES, INPUT_SIZE, inputs);
        inferenceInterface.runInference(new String[]{OUTPUT_NODE});
        float[] results = new float[2]; // [0, 1], [1, 0]
        inferenceInterface.readNodeFloat(OUTPUT_NODE, results);
        displayResults(results);
    }

    @SuppressLint("SetTextI18n")
    private void displayResults(float[] results) {
        if (results[0] >= results[1]) {
            result.setText("Model predicts price will increase.");
            result.setTextColor(GREEN);
        } else {
            result.setText("Model predicts price will decrease.");
            result.setTextColor(RED);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class JsonTask extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please Wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            parseJSONData(s);
        }
    }
}