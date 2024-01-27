package com.example.myapplication2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import com.example.myapplication2.databinding.LoginBinding
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: LoginBinding
    private lateinit var username: EditText;
    private lateinit var loginButton: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = binding.username;
        loginButton = binding.loginButton;
        loginButton.setOnClickListener( {view -> login()});

    }
    @SuppressLint("SetTextI18n")
    fun login() : Void? {
        println("TEST");
        Log.d("login", username.getText().toString());
        finish();
        var LoginActivity: Intent = Intent()
        startActivity()

        return null;
    }

}