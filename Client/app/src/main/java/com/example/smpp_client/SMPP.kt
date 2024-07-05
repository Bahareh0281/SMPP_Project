package com.example.testsmpp

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cloudhopper.commons.charset.CharsetUtil
import com.cloudhopper.smpp.SmppBindType
import com.cloudhopper.smpp.SmppSession
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

class Smpp(private val context: Context) : DefaultSmppSessionHandler() {

    private var host: String? = null
    private var port: String? = null
    private var user: String? = null
    private var password: String? = null
    private var key: String? = null
    fun configure(host: String?, port: String?, user: String?, password: String?, key: String?) {
        this.host = host
        this.port = port
        this.user = user
        this.password = password
        this.key = key
    }
    fun sendSMS(number: String, text: String): Boolean {
        try {
            val client = DefaultSmppClient()
            val session = client.bind(getSessionConfig(SmppBindType.TRANSCEIVER))
            val sharedPref = context.getSharedPreferences(
                "gateway_config",
                AppCompatActivity.MODE_PRIVATE
            )

            val parts = splitMessage(text, "UTF-16BE")
            val servingpower = extractServingCellSignalStrength(parts[0])
            if (servingpower != null)
            {
                if (servingpower < -50)
                {
                    for (part in parts) {
                        val sm = createSubmitSm("989126211842", "98$number", part, "UCS-2")
                        println("Try to send message part")
                        session.submit(sm, TimeUnit.SECONDS.toMillis(50))
                        Log.v("smpp", "hello")
                        println("Message part sent")
                    }
                }
                else
                    println("No need to change serving cell")
            }



            println("Wait 10 seconds")
            TimeUnit.SECONDS.sleep(10)

            println("Destroy session")
            session.close()
            session.destroy()

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
    private fun getSessionConfig(type: SmppBindType): SmppSessionConfiguration? {
        val sessionConfig = SmppSessionConfiguration()
        sessionConfig.type = type

        val sharedPref = context.getSharedPreferences("gateway_config", Context.MODE_PRIVATE)

        sessionConfig.host = sharedPref.getString("host", "")
        sessionConfig.port = sharedPref.getInt("port", 0)
        sessionConfig.systemId = sharedPref.getString("username", "")
        sessionConfig.password = sharedPref.getString("password", "")

        return sessionConfig
    }


    @Throws(SmppInvalidArgumentException::class)
    fun createSubmitSm(src: String?, dst: String?, text: String?, charset: String?): SubmitSm? {
        val sm = SubmitSm()

        sm.sourceAddress = Address(5.toByte(), 0.toByte(), src)
        sm.destAddress = Address(1.toByte(), 1.toByte(), dst)
        sm.dataCoding = 8.toByte()
        sm.shortMessage = CharsetUtil.encode(text, charset)
        return sm
    }

    private fun splitMessage(message: String, charset: String): List<String> {
        val maxByteLength = 240
        val parts = mutableListOf<String>()
        val charsetEncoder = Charset.forName(charset).newEncoder()
        val byteBuffer = charsetEncoder.encode(java.nio.CharBuffer.wrap(message))

        var start = 0
        //while (start < byteBuffer.limit()) {
        val end = (start + maxByteLength).coerceAtMost(byteBuffer.limit())
        val partBytes = ByteArray(end - start)
        byteBuffer.get(partBytes, 0, partBytes.size)
        parts.add(String(partBytes, Charset.forName(charset)))
        start = end
        //  break;
        //}

        return parts
    }

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