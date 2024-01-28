package com.example.diediabetes;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarbCalculator extends Activity {

    private LinearLayout foodEntriesContainer;
    private TextView resultTextView;
    private final ArrayList<EditText> foodTypeEditTexts = new ArrayList<>();
    private final ArrayList<EditText> weightEditTexts = new ArrayList<>();
    private double totalCalories = 0;
    private double totalCarbs = 0;
    private final AtomicInteger tasksCompleted = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carb_calculator);

        foodEntriesContainer = findViewById(R.id.foodEntriesContainer);
        resultTextView = findViewById(R.id.resultView);

        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> {addFoodEntryField(); calculateTotalNutrition();});

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> navigateToNewPage());

        Button buttonHome = findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(v -> {
            /*Intent intent = new Intent(CarbCalculator.this, logsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();*/
        });
    }

    private void navigateToNewPage() {
        if (isValidEntries()) {
            Intent intent = new Intent(this, BolusCalculator.class);
            intent.putExtra("TotalCarbs", totalCarbs);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error: Please fill in all information correctly", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidEntries() {
        for (int i = 0; i < foodTypeEditTexts.size(); i++) {
            String foodType = foodTypeEditTexts.get(i).getText().toString().trim();
            String weight = weightEditTexts.get(i).getText().toString().trim();

            if (foodType.isEmpty() || weight.isEmpty()) {
                return false;
            }
            try {
                double weightValue = Double.parseDouble(weight);
                if (weightValue <= 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }


    private void addFoodEntryField() {
        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        entryLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create EditText for food type
        EditText foodTypeEditText = new EditText(this);
        foodTypeEditText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        foodTypeEditText.setHint("Enter food type");
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        foodTypeEditText.setFilters(new InputFilter[]{filter});

        EditText weightEditText = new EditText(this);
        weightEditText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        weightEditText.setHint("Enter weight in grams");
        weightEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        entryLayout.addView(foodTypeEditText);
        entryLayout.addView(weightEditText);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                calculateTotalNutrition();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        foodTypeEditText.addTextChangedListener(textWatcher);
        weightEditText.addTextChangedListener(textWatcher);

        // Create a Delete Button
        Button deleteButton = new Button(this);
        deleteButton.setText("X");
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Set click listener for the Delete Button
        deleteButton.setOnClickListener(v -> {
            foodEntriesContainer.removeView(entryLayout);
            foodTypeEditTexts.remove(foodTypeEditText);
            weightEditTexts.remove(weightEditText);
            calculateTotalNutrition();
        });

        entryLayout.addView(deleteButton);
        foodEntriesContainer.addView(entryLayout);
        foodTypeEditTexts.add(foodTypeEditText);
        weightEditTexts.add(weightEditText);
    }


    @SuppressLint("DefaultLocale")
    private void calculateTotalNutrition() {
        totalCalories = 0;
        totalCarbs = 0;
        tasksCompleted.set(0);
        List<FetchNutritionTask> tasks = new ArrayList<>();

        for (int i = 0; i < foodTypeEditTexts.size(); i++) {
            String foodType = foodTypeEditTexts.get(i).getText().toString().trim();
            String weight = weightEditTexts.get(i).getText().toString().trim();

            if (!foodType.isEmpty() && !weight.isEmpty()) {
                try {
                    double weightValue = Double.parseDouble(weight);
                    if (weightValue > 0) {
                        FetchNutritionTask task = new FetchNutritionTask();
                        tasks.add(task);
                        task.execute(foodType, weight);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        new Thread(() -> {
            for (FetchNutritionTask task : tasks) {
                try {
                    task.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> resultTextView.setText(String.format("Total Calories: %.2f kcal\nTotal Carbs: %.2f grams", totalCalories, totalCarbs)));
        }).start();
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchNutritionTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String foodType = params[0];
            String weight = params[1];
            try {
                String API_ENDPOINT = "https://api.edamam.com/api/nutrition-data";
                String APP_ID = "9ed2e9f6";
                String APP_KEY = "74bcad5ce6fa0d9586f55535978e4202";
                URL url = new URL(API_ENDPOINT + "?app_id=" + APP_ID + "&app_key=" + APP_KEY + "&ingr=" + weight + "g%20" + foodType);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null && !response.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject totalNutrients = jsonObject.getJSONObject("totalNutrients");

                    if (totalNutrients.has("ENERC_KCAL") && totalNutrients.has("CHOCDF")) {
                        JSONObject caloriesJson = totalNutrients.getJSONObject("ENERC_KCAL");
                        JSONObject carbsJson = totalNutrients.getJSONObject("CHOCDF");

                        synchronized (this) {
                            totalCalories += caloriesJson.getDouble("quantity");
                            totalCarbs += carbsJson.getDouble("quantity");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tasksCompleted.incrementAndGet();
        }
    }
}
