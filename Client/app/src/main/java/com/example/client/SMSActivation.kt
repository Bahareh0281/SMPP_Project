package com.example.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SMSActivation : AppCompatActivity() {

    // Permission for reading SMS
    private val permission: String = Manifest.permission.READ_SMS

    // Request code for SMS permission
    private val requestCode: Int = 1

    // RecyclerView for displaying chat items
    private lateinit var recyclerView: RecyclerView

    // Adapter for the RecyclerView
    private lateinit var chatAdapter: ChatAdapter

    // List to hold chat items
    private lateinit var chatItems: MutableList<ChatAdapter.ChatItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        // Hide the action bar
        supportActionBar?.hide()

        // Check for SMS read permission and read SMS accordingly
        chatItems = if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            readSms() as MutableList<ChatAdapter.ChatItem>
        } else {
            // Read SMS if permission is already granted
            readSms() as MutableList<ChatAdapter.ChatItem>
        }

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create and set up the ChatAdapter
        chatAdapter = ChatAdapter(chatItems)
        recyclerView.adapter = chatAdapter

        // Set up back button to finish the activity
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    // Method to update the chat items in the adapter
    private fun updateChatItems(newChatItems: List<ChatAdapter.ChatItem>) {
        chatItems.clear()
        chatItems.addAll(newChatItems)
        chatAdapter.notifyDataSetChanged()
    }

    // Example method to update the chat items (you can call this whenever you have new chat items)
    private fun loadChatItems() {
        // Retrieve or generate your List<ChatItem>
        val newChatItems = readSms()
        updateChatItems(newChatItems)
    }

    // Method to read SMS from the device
    private fun readSms(): List<ChatAdapter.ChatItem> {
        // Columns to retrieve from the SMS content provider
        val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
        val textCol = Telephony.TextBasedSmsColumns.BODY
        val typeCol = Telephony.TextBasedSmsColumns.TYPE

        val projection = arrayOf(numberCol, textCol, typeCol)

        // Query the SMS content provider
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection, null, null, null
        )

        // Indices for the columns
        val numberColIdx = cursor!!.getColumnIndex(numberCol)
        val textColIdx = cursor.getColumnIndex(textCol)
        val typeColIdx = cursor.getColumnIndex(typeCol)

        // List to hold chat items
        val chatItems = mutableListOf<ChatAdapter.ChatItem>()

        // Iterate through the SMS messages
        while (cursor.moveToNext()) {
            val number = cursor.getString(numberColIdx)
            var text = cursor.getString(textColIdx)
            val type = cursor.getString(typeColIdx)

            // Filter and modify messages containing "SMPP"
            if (text.contains("SMPP")) {
                text = text.replace("\n sent by Ozeki SMPP SMS Gateway for Android - www.ozekisms.com","")
                text = text.replace("sent by Ozeki SMPP SMS Gateway for Android - www.ozekisms.com","")

                // Log the filtered message
                chatItems.add(ChatAdapter.ChatItem(number, text))
            }
        }

        // Close the cursor
        cursor.close()

        return chatItems
    }
}