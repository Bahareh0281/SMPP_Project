package com.example.Client

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cloudhopper.commons.charset.CharsetUtil
import com.cloudhopper.smpp.SmppBindType
import com.cloudhopper.smpp.SmppSessionConfiguration
import com.cloudhopper.smpp.impl.DefaultSmppClient
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler
import com.cloudhopper.smpp.pdu.DeliverSm
import com.cloudhopper.smpp.pdu.PduRequest
import com.cloudhopper.smpp.pdu.PduResponse
import com.cloudhopper.smpp.pdu.SubmitSm
import com.cloudhopper.smpp.type.Address
import com.cloudhopper.smpp.type.SmppBindException
import com.cloudhopper.smpp.type.SmppChannelException
import com.cloudhopper.smpp.type.SmppInvalidArgumentException
import com.cloudhopper.smpp.type.SmppTimeoutException
import com.cloudhopper.smpp.type.UnrecoverablePduException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class SMPP(private val context: Context) : DefaultSmppSessionHandler() {

    private var host: String? = null
    private var port: String? = null
    private var user: String? = null
    private var password: String? = null
    private var key: String? = null

    // Method to configure SMPP connection details
    fun configure(host: String?, port: String?, user: String?, password: String?, key: String?) {
        this.host = host
        this.port = port
        this.user = user
        this.password = password
        this.key = key
    }

    // Method to send an SMS
    fun sendSMS(number: String, text: String): Boolean {
        try {
            // Initialize SMPP client
            val client = DefaultSmppClient()
            val session = client.bind(getSessionConfig(SmppBindType.TRANSCEIVER))
            val sharedPref = context.getSharedPreferences(
                "gateway_config",
                AppCompatActivity.MODE_PRIVATE
            )

            // Split the message into parts if necessary
            val parts = splitMessage(text, "UTF-16BE")
            // Extract serving cell signal strength
            val servingpower = extractServingCellSignalStrength(parts[0])
            if (servingpower != null) {
                // Send message parts if signal strength is less than -50
                if (servingpower < -50) {
                    for (part in parts) {
                        val sm = createSubmitSm("989126211842", "98$number", part, "UCS-2")
                        println("Try to send message part")
                        session.submit(sm, TimeUnit.SECONDS.toMillis(50))
                        Log.v("smpp", "hello")
                        println("Message part sent")
                    }
                } else {
                    println("No need to change serving cell")
                }
            }

            // Wait for 10 seconds before closing the session
            println("Wait 10 seconds")
            TimeUnit.SECONDS.sleep(10)

            // Close and destroy the session
            println("Destroy session")
            session.close()
            session.destroy()

            // Destroy the client
            println("Destroy client")
            client.destroy()

            println("Bye!")
            return true

        } catch (ex: SmppTimeoutException) {
            Log.v("session", "SmppTimeoutException")
        } catch (ex: SmppChannelException) {
            Log.v("session", "SmppChannelException")
        } catch (ex: SmppBindException) {
            Log.v("session", "SmppBindException")
        } catch (ex: UnrecoverablePduException) {
            Log.v("session", "UnrecoverablePduException")
        } catch (ex: InterruptedException) {
            Log.v("session", "InterruptedException")
        }
        Log.v("session", "WTDFFF")
        return false
    }

    // Method to get SMPP session configuration
    private fun getSessionConfig(type: SmppBindType): SmppSessionConfiguration? {
        val sessionConfig = SmppSessionConfiguration()
        sessionConfig.type = type

        // Retrieve configuration from shared preferences
        val sharedPref = context.getSharedPreferences("gateway_config", Context.MODE_PRIVATE)

        sessionConfig.host = sharedPref.getString("host", "")
        sessionConfig.port = sharedPref.getInt("port", 0)
        sessionConfig.systemId = sharedPref.getString("username", "")
        sessionConfig.password = sharedPref.getString("password", "")

        return sessionConfig
    }

    // Method to create a SubmitSm PDU
    @Throws(SmppInvalidArgumentException::class)
    fun createSubmitSm(src: String?, dst: String?, text: String?, charset: String?): SubmitSm? {
        val sm = SubmitSm()

        sm.sourceAddress = Address(5.toByte(), 0.toByte(), src)
        sm.destAddress = Address(1.toByte(), 1.toByte(), dst)
        sm.dataCoding = 8.toByte()
        sm.shortMessage = CharsetUtil.encode(text, charset)
        return sm
    }

    // Method to split a message into parts based on charset
    private fun splitMessage(message: String, charset: String): List<String> {
        val maxByteLength = 240
        val parts = mutableListOf<String>()
        val charsetEncoder = Charset.forName(charset).newEncoder()
        val byteBuffer = charsetEncoder.encode(java.nio.CharBuffer.wrap(message))

        var start = 0
        // Split message into parts of maxByteLength bytes
        val end = (start + maxByteLength).coerceAtMost(byteBuffer.limit())
        val partBytes = ByteArray(end - start)
        byteBuffer.get(partBytes, 0, partBytes.size)
        parts.add(String(partBytes, Charset.forName(charset)))
        start = end

        return parts
    }

    // Method to extract serving cell signal strength from a message
    fun extractServingCellSignalStrength(message: String): Int? {
        val parts = message.split(";")

        if (parts.isNotEmpty()) {
            val servingCell = parts[0]
            val servingCellParts = servingCell.split(",")
            if (servingCellParts.size == 2) {
                return servingCellParts[1].toIntOrNull()
            }
        }
        return null
    }

    // Method to handle received PDU requests
    override fun firePduRequestReceived(pduRequest: PduRequest<*>): PduResponse? {
        val response = pduRequest.createResponse()
        val sms = pduRequest as DeliverSm
        if (sms.dataCoding.toInt() == 0) {
            println("From: " + sms.sourceAddress.address)
            println("To: " + sms.destAddress.address)
            println("Content: " + String(sms.shortMessage))
        }
        return response
    }
}
