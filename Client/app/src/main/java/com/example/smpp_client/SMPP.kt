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