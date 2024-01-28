package com.example.myapplication2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.SingleDataBinding

class singlePage: AppCompatActivity() {
    private lateinit var binding: SingleDataBinding;

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState);
        binding = SingleDataBinding.inflate(layoutInflater);
        setContentView(binding.root);

//        val info = intent.getStringExtra("glucoseReading");
//        Log.d("single-page-info", info.toString());
        populatePage(intent);
    }

    // Helper Function
    @SuppressLint("SetTextI18n")
    fun populatePage(intent: Intent){
        // Set the textboxes to variables to modify their text
        val dateText = findViewById(R.id.single_date) as TextView;
        val timeText = findViewById(R.id.single_time) as TextView;
        val mealTypeText = findViewById(R.id.single_mealType) as TextView;
        val glucoseReadingText = findViewById(R.id.single_glucoseReading) as TextView;
        val carbsConsumedText = findViewById(R.id.single_carbsConsumed) as TextView;
        val insulinTakenText = findViewById(R.id.single_insulinTaken) as TextView;
        // Modifying texts
        dateText.text = "DATE: " + intent.getStringExtra("date") + " Some date"
        timeText.text = "TIME: " + intent.getStringExtra("time") + " AM/PM";
        mealTypeText.text = "Type of Meal: " + intent.getStringExtra("mealType");
        glucoseReadingText.text = "Glucose Reading: " + intent.getStringExtra("glucoseReading") + " mg/dL";
        carbsConsumedText.text = "Carbohydrates Consumed: " + intent.getStringExtra("carbsConsumed") + " grams";
        insulinTakenText.text = "Insulin Taken: " + intent.getStringExtra("insulinTaken") + " she'll fix it";
    }
}