package com.example.client

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // Variables for sending SMS
    var sendVal = false
    var number = ""
    var text = ""

    // Variables for receiving SMS
    var receiveVal = false
    var rNumber = ""
    var rText = ""

    // Flag for send status
    var sendStatus = false

    // Required permissions array
    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )
    // Request code for permissions
    private val requestCode = 2

    // LocationAndCellInfo instance
    private lateinit var locationAndCellInfo: LocationAndCellInfo

    // Handler and HandlerThread for background operations
    private lateinit var sendHandler: Handler
    private lateinit var sendHandlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var sendSmsRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.hide()

        // Initialize LocationAndCellInfo
        locationAndCellInfo = LocationAndCellInfo(this)
        locationAndCellInfo.initialize()

        // Get the number input field
        val numberEditText = findViewById<EditText>(R.id.number_input)
        val smppClient = Smpp(this)

        // Start a handler thread for sending SMS
        sendHandlerThread = HandlerThread("SendHandlerThread")
        sendHandlerThread.start()
        sendHandler = Handler(sendHandlerThread.looper)

        // Set up the settings button
        val settingButton = findViewById<ImageButton>(R.id.settings_button)
        settingButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Set up the SMS button
        val smsButton = findViewById<ImageButton>(R.id.sms_button)
        smsButton.setOnClickListener {
            val intent = Intent(this, SMSActivity::class.java)
            startActivity(intent)
        }

        // Initialize handler and runnable for periodic SMS sending
        handler = Handler(Looper.getMainLooper())
        sendSmsRunnable = object : Runnable {
            override fun run() {
                // Set number and text for SMS
                number = "9126211842"
                text = locationAndCellInfo.getCellInfo()
                sendVal = true
                // Post the sendSMS task to the handler
                sendHandler.post {
                    sendSMS(smppClient)
                }
                // Schedule the next run after 120 seconds
                handler.postDelayed(this, 120000)
            }
        }
        // Start the periodic SMS sending
        handler.post(sendSmsRunnable)

        // Set up the exit button
        val exitButton = findViewById<Button>(R.id.exit_button)
        exitButton.setOnClickListener {
            // Reset statuses and send an SMS with cell information
            showSendStatus(false, false)
            showAckStatus(false, false)
            number = "9126211842"
            text = locationAndCellInfo.getCellInfo()
            sendVal = true
            sendHandler.post {
                sendSMS(smppClient)
            }
        }

        // Check for required permissions and request if not granted
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        } else {
            // Start the SMS listener if permissions are granted
            startSMSListener()
        }
    }

    // Function to send SMS
    private fun sendSMS(smppClient: Smpp) {
        try {
            if (sendVal) {
                sendStatus = smppClient.sendSMS(number, text)
                Log.d("MainActivity", "Cell Info_Thread: $text")
                Log.d("Status", "Cell Info: $sendStatus")
                sendVal = false

                // Update UI based on send status
                runOnUiThread {
                    if (sendStatus)
                        showSendStatus(true, false)
                    else
                        showSendStatus(false, true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Quit the handler thread safely
        sendHandlerThread.quitSafely()
    }

    // Function to check if all required permissions are granted
    private fun hasPermissions(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // Handle the result of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            // Check if all permissions are granted
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startSMSListener()
            } else {
                // Log an error if permissions are not granted
                Log.e("MainActivity", "Required permissions not granted")
            }
        }
    }

    // Handle new intents (used for receiving SMS messages)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Get values from the intent
        receiveVal = intent.getBooleanExtra("isReceive", false)
        rNumber = intent.getStringExtra("phoneNumber").toString()
        rText = intent.getStringExtra("text").toString()

        // Get values for acknowledgment
        val aReceiveVal = intent.getBooleanExtra("ackReceive", false)
        val aNumber = intent.getStringExtra("ackPhoneNumber").toString()
        val aText = intent.getStringExtra("ackText").toString()

        // Log received message
        if (receiveVal) {
            Log.d("MainActivity", "Received message from: $rNumber with text: $rText")
        }

        // Log received acknowledgment and update UI
        if (aReceiveVal) {
            Log.d("MainActivity", "Received ACK from: $aNumber with text: $aText")
            showSendStatus(true, false)

            if (aText == text && aNumber == number)
                showAckStatus(true, false)
            else
                showAckStatus(false, true)
        }
    }

    // Function to start listening for SMS messages
    private fun startSMSListener() {
        val smsReceiver = SMSReceiver()
        val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)
    }

    // Function to update the acknowledgment status UI
    private fun showAckStatus(sa: Boolean, fa: Boolean) {
        val saTextView = findViewById<TextView>(R.id.status_sa_textview)
        val faTextView = findViewById<TextView>(R.id.status_fa_textview)

        saTextView.visibility = if (sa) View.VISIBLE else View.INVISIBLE
        faTextView.visibility = if (fa) View.VISIBLE else View.INVISIBLE
    }

    // Function to update the send status UI
    private fun showSendStatus(sm: Boolean, fm: Boolean) {
        val smTextView = findViewById<TextView>(R.id.status_sm_textview)
        val fmTextView = findViewById<TextView>(R.id.status_fm_textview)

        smTextView.visibility = if (sm) View.VISIBLE else View.INVISIBLE
        fmTextView.visibility = if (fm) View.VISIBLE else View.INVISIBLE
    }
}