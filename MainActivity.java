package com.example.diediabetes;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.example.diediabetes.R;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {

    private LinearLayout foodEntriesContainer;
    private TextView resultTextView;
    private ArrayList<EditText> foodTypeEditTexts = new ArrayList<>();
    private ArrayList<EditText> weightEditTexts = new ArrayList<>();
    private final String API_ENDPOINT = "https://api.edamam.com/api/nutrition-data";
    private final String APP_ID = "9ed2e9f6";
    private final String APP_KEY = "74bcad5ce6fa0d9586f55535978e4202";
    private double totalCalories = 0;
    private double totalCarbs = 0;
    private AtomicInteger tasksCompleted = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foodEntriesContainer = findViewById(R.id.foodEntriesContainer);
        resultTextView = findViewById(R.id.resultView);
        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> {addFoodEntryField(); calculateTotalNutrition();});

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> navigateToNewPage());
    }

    private void navigateToNewPage() {
        if (isValidEntries()) {
            Intent intent = new Intent(this, BolusCalculator.class);
            intent.putExtra("TotalCarbs", totalCarbs);
            startActivity(intent);
        } else {
            // Show an error message to the user
            Toast.makeText(this, "Error: Please fill in all information correctly", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidEntries() {
        for (int i = 0; i < foodTypeEditTexts.size(); i++) {
            String foodType = foodTypeEditTexts.get(i).getText().toString().trim();
            String weight = weightEditTexts.get(i).getText().toString().trim();

            // Check if food type or weight is empty
            if (foodType.isEmpty() || weight.isEmpty()) {
                return false;
            }

            // Check if weight is a valid number
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
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        foodTypeEditText.setFilters(new InputFilter[]{filter});

        // Create EditText for weight
        EditText weightEditText = new EditText(this);
        weightEditText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        weightEditText.setHint("Enter weight in grams");
        weightEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Add EditTexts to the entry layout
        entryLayout.addView(foodTypeEditText);
        entryLayout.addView(weightEditText);

        // Add TextWatcher to trigger recalculation when text changes
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
                } catch (NumberFormatException e) {
                    // Invalid weight, skip this entry
                }
            }
        }

        // Wait for all tasks to complete and then aggregate results
        new Thread(() -> {
            for (FetchNutritionTask task : tasks) {
                try {
                    task.get(); // Waits for the task to complete
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> resultTextView.setText(String.format("Total Calories: %.2f kcal\nTotal Carbs: %.2f grams", totalCalories, totalCarbs)));
        }).start();
    }

    private class FetchNutritionTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String foodType = params[0];
            String weight = params[1];
            try {
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

                    // Check if the response contains the calorie and carb data
                    if (totalNutrients.has("ENERC_KCAL") && totalNutrients.has("CHOCDF")) {
                        JSONObject caloriesJson = totalNutrients.getJSONObject("ENERC_KCAL");
                        JSONObject carbsJson = totalNutrients.getJSONObject("CHOCDF");

                        synchronized (this) {
                            // Update the global totals
                            totalCalories += caloriesJson.getDouble("quantity");
                            totalCarbs += carbsJson.getDouble("quantity");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Optionally, update the UI or log an error message
                }
            }

            // Increment tasksCompleted after parsing response
            tasksCompleted.incrementAndGet();
        }
    }
}
