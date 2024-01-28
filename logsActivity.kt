package com.example.myapplication2

//import androidx.appcompat.app.AlertDialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.LogsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class logsActivity : AppCompatActivity() {
    private lateinit var binding: LogsBinding;

    private var userData = ArrayList<Int>()
    private var mapOfUserData = HashMap<Int, ArrayList<String>>();
    //                <---  Format --->
    //      Number: [ Date, Time, Units of Insulin, Carbohydrates Consumed, Glucose Reading,
    //                      What event is the check-in (before dinner, after lunch)
    //                      ]
    //                      9pm --> nighttime
    //                      before breakfast, 2hrs after lunch/dinner (at least 3 times)
    //
    private lateinit var addUserData: Button;
    private lateinit var calculatorButton: Button;
    private lateinit var userDataList: ListView;
    private lateinit var arrayAdapter: ArrayAdapter<*>;
    private var test = ArrayList<logData>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        // Setting of variables
        binding = LogsBinding.inflate(layoutInflater);
        setContentView(binding.root);
        userDataList = binding.userDataList;
        addUserData = binding.addData;
        calculatorButton = binding.calculatorbutton;
        calculatorButton.setOnClickListener({view -> changeToCarbCalculatorScreen()});
        addUserData.setOnClickListener( {view -> showAlertDialog("Enter Requested Information","BCSD")});
        // Saving the data for user regardless if user exits app
        userDataList.setOnItemClickListener(
            { parent, view, position, id ->
                showSinglePage(parent, view, position, id) });
        updateList();
    }
    fun changeToCarbCalculatorScreen(){
        val intent = Intent(this, CarbCalculator::class.java);
//        intent.putExtra("userdatalist",test );
        startActivity(intent);
    }
    fun showSinglePage(parent: AdapterView<*>, view: View, position: Int, id: Long){
        val intent = Intent(this, singlePage::class.java);
        val singleData = userDataList.getItemAtPosition(position) as logData;

        intent.putExtra("date", singleData.getDate());
        intent.putExtra("time", singleData.getTime());
        intent.putExtra("mealType", singleData.getMealType());
        intent.putExtra("glucoseReading", singleData.getGlucoseReading());
        intent.putExtra("carbsConsumed", singleData.getCarbsConsumed());
        intent.putExtra("insulinTaken", singleData.getInsulinTaken());
        startActivity(intent)

    }
    fun showAlertDialog(title: String?, msg: String?) {
        val alertDialog = AlertDialog.Builder(this)

        // Modify the layout that will appear on the alert
        val lila1 = LinearLayout(this)
        lila1.orientation = LinearLayout.VERTICAL
        val text = EditText(this);
        text.hint = "Enter Numbers"
        val text2 = EditText(this);
        text2.hint = "Enter Time"
        val text3 = EditText(this);
        text3.hint = "Enter Amount"
        lila1.addView(text)
        lila1.addView(text2)
        lila1.addView(text3)


        // Setting the alertDialog and modify it to appear on mobile
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.setView(lila1)

//        alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton("OK") { dialog, which ->
            Toast.makeText(applicationContext,
                "You entered some data ykwim", Toast.LENGTH_SHORT).show();
            Log.d("logs-text1", text.text.toString());
            Log.d("logs-text2", text2.text.toString());
            Log.d("logs-text3", text3.text.toString());
//            userData.add(text.text.toString().toInt());
            test.add(logData("1", "2",
                "3", text.text.toString(), "5", "6'"));
            updateList();
        }

        // Showing Alert Message
        alertDialog.show()
    }
// Updates the list and show it on the webpage
    private fun updateList(): Void? {
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, test);
        userDataList.adapter = arrayAdapter;
        return null;

    }
@SuppressLint("SetTextI18n")
    private fun addInfo(): Void? {

        userData.add(5);
        updateList();
        return null;
    }

}
