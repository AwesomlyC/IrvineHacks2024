package com.example.diediabetes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BolusCalculator extends Activity {

    private EditText editTextCarbRatio, editTextGlucoseReading;
    private double totalCarbsConsumed;
    private CheckBox checkBoxCondition1, checkBoxCondition2, checkBoxCondition3, checkBoxCondition4, checkBoxCondition5;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bolus_calculator);

        textViewResult = findViewById(R.id.textViewResult);
        Intent intent = getIntent();
        totalCarbsConsumed = intent.getDoubleExtra("TotalCarbs", 0);
        editTextCarbRatio = findViewById(R.id.editTextCarbRatio);
        editTextGlucoseReading = findViewById(R.id.editTextGlucoseReading);
        checkBoxCondition1 = findViewById(R.id.checkBoxCondition1);
        checkBoxCondition2 = findViewById(R.id.checkBoxCondition2);
        checkBoxCondition3 = findViewById(R.id.checkBoxCondition3);
        checkBoxCondition4 = findViewById(R.id.checkBoxCondition4);

        Button buttonReturn = findViewById(R.id.buttonReturn);
        buttonReturn.setOnClickListener(v -> {
            finish();
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                calculateBolus();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        editTextCarbRatio.addTextChangedListener(textWatcher);
        editTextGlucoseReading.addTextChangedListener(textWatcher);

        Button buttonDone = findViewById(R.id.buttonDone);
        buttonDone.setOnClickListener(v -> {
            if (areFieldsFilled()) {
                finish();
            } else {
                Toast.makeText(BolusCalculator.this, "Error: Please fill in all fields correctly", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void calculateBolus() {
        try {
            double carbRatio = Double.parseDouble(editTextCarbRatio.getText().toString());
            double glucoseReading = Double.parseDouble(editTextGlucoseReading.getText().toString());
            double carbsConsumed = totalCarbsConsumed;

            if (carbRatio == 0) {
                textViewResult.setText("Carbohydrate ratio cannot be zero.");
                return;
            }

            double carbBolus = carbsConsumed / carbRatio;
            double glucoseCorrectionFactor = 50;
            double glucoseCorrectionBolus = (glucoseReading - 120) / glucoseCorrectionFactor;

            if (shouldOmitGlucoseCorrection(glucoseReading)) {
                glucoseCorrectionBolus = 0;
            }

            double totalBolus = carbBolus + glucoseCorrectionBolus;
            double roundedBolus = roundBolus(totalBolus);

            textViewResult.setText(String.format("Total Insulin Bolus: %.1f units", roundedBolus));
        } catch (NumberFormatException e) {
            textViewResult.setText("Invalid input");
        }
    }

    private boolean areFieldsFilled() {
        String carbRatio = editTextCarbRatio.getText().toString();
        String glucoseReading = editTextGlucoseReading.getText().toString();
        return !carbRatio.isEmpty() && !glucoseReading.isEmpty() && !carbRatio.equals("0");
    }

    private boolean shouldOmitGlucoseCorrection(double glucoseReading) {
        return glucoseReading < 120 || checkBoxCondition1.isChecked() || checkBoxCondition2.isChecked()
                || checkBoxCondition3.isChecked() || checkBoxCondition4.isChecked();
    }

    private double roundBolus(double bolus) {
        double fractionalPart = bolus - (int) bolus;
        if (fractionalPart < 0.4) {
            return Math.floor(bolus);
        } else if (fractionalPart <= 0.7) {
            return Math.floor(bolus) + 0.5;
        } else {
            return Math.ceil(bolus);
        }
    }
}
