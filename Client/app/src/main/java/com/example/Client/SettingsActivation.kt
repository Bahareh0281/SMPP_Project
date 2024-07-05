package com.example.Client

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class SettingsActivation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        // Retrieve shared preferences for storing configuration settings
        val sharedPref = this.getSharedPreferences("gateway_config", Context.MODE_PRIVATE)

        // Initialize EditText fields with stored or default values
        val hostEditText = findViewById<EditText>(R.id.editTextHost)
        hostEditText.setText(sharedPref.getString("host", "172.20.10.13"))

        val portEditText = findViewById<EditText>(R.id.editTextPort)
        portEditText.setText(sharedPref.getInt("port", 9500).toString())

        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)
        usernameEditText.setText(sharedPref.getString("username", "smppuser"))

        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        passwordEditText.setText(sharedPref.getString("password", "Ouo5nQM8"))

        val keyEditText = findViewById<EditText>(R.id.editKey)
        keyEditText.setText(sharedPref.getString("key", ""))

        // Save button click listener to validate and save configuration
        val submitButton = findViewById<Button>(R.id.buttonSave)
        submitButton.setOnClickListener {
            val host = hostEditText.text.toString()
            val port = portEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val key = keyEditText.text.toString()
            val fixedKey = "1Q79Babrt983JHGS09312nsKJ!"

            // Check if the entered key matches the fixed key
            if (key != fixedKey) {
                // Show toast message for incorrect key
                Toast.makeText(this, "The entered key is incorrect.", Toast.LENGTH_SHORT).show()
                keyEditText.text.clear()
            } else {
                // Save configuration to shared preferences
                saveConfig(host, port, username, password, key)

                // Navigate to MainActivity upon successful save
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        // Back button click listener to finish the activity and go back
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    // Function to save configuration settings to shared preferences
    private fun saveConfig(host: String, port: String, username: String, password: String, key: String) {
        val sharedPref = this.getSharedPreferences("gateway_config", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("host", host)
            putInt("port", port.toInt())
            putString("username", username)
            putString("password", password)
            putString("key", key)
            apply()
        }
    }
}
