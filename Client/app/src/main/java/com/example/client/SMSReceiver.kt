package com.example.testsmpp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SMSReceiver : BroadcastReceiver() {

    // Override the onReceive method to handle the incoming SMS broadcast
    override fun onReceive(context: Context, intent: Intent) {
        // Get the extras from the intent
        val bundle = intent.extras
        if (bundle != null) {
            // Retrieve the SMS message received as an array of PDUs (Protocol Data Units)
            val pdus = bundle["pdus"] as Array<*>
            pdus.forEach {
                // Create an SmsMessage object from the PDU byte array
                val smsMessage = SmsMessage.createFromPdu(it as ByteArray)
                // Get the sender's phone number
                val sender = smsMessage.displayOriginatingAddress
                // Get the body of the SMS message
                val messageBody = smsMessage.displayMessageBody

                // Log the received SMS message for debugging purposes
                Log.d("SMSReceiver", "Received SMS: $messageBody from $sender")

                // Check if the message body contains the acknowledgment phrase
                if (messageBody.contains("پیام شما دریافت شد")) {
                    // Create an intent to start MainActivity with the acknowledgment details
                    val ackIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("isReceive", true)
                        putExtra("phoneNumber", sender)
                        putExtra("text", messageBody)
                        putExtra("ackReceive", true)
                        putExtra("ackPhoneNumber", sender)
                        putExtra("ackText", messageBody)
                    }
                    // Start MainActivity with the acknowledgment intent
                    context.startActivity(ackIntent)
                }
            }
        }
    }
}
