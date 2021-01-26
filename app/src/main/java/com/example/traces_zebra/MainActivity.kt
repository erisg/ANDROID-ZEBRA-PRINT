package com.example.traces_zebra

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.concurrent.Delayed
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private var connection: Connection? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printButton.setOnClickListener {
            connect()
        }

        blouthooButton.setOnClickListener {

        }
    }


    fun connect() {
        connection = BluetoothConnection("00:22:58:3C:3C:0D")
        try {
            connection?.open()
            setStatus("Connected", Color.GREEN)
            var printer: ZebraPrinter? = null
            if (connection!!.isConnected) {
                try {
                    printer = ZebraPrinterFactory.getInstance(connection)
                    val cod = "! U1 setvar \"device.languages\" \"zpl\"\r\n"
                    val format = "^XA " + "^DFR:TEMPLATE.ZPL^FS " + "^FO25,25^A0N,50,50^FDName^FS " + "^FO25,75^A0N,50,50^FN1^FS " + "^FO25,125^A0N,50,50^FDSurname^FS " + "^FO25,175^A0N,50,50^FN2^FS " + "^XZ"
                    //  connection?.write(convertExtendedAscii(cod))
                    printer.sendCommand(cod)
                    printer.calibrate()
                    printer.sendCommand("^XA^FO20,20^A0N,25,25^FDThis is a ZPL test.^FS^XZ")
                    //  connection?.write(format.toByteArray())
                    val retriever = String(printer.retrieveFormatFromPrinter("R:TEMPLATE.ZPL"))
                    val data = HashMap<Int, String>()
                    data[1] = "GERALDIN"
                    data[2] = "ARCHILA"
                    printer.printStoredFormat("R:TEMPLATE.ZPL", data)
                    Log.e("conecct", "si" + printer.printerControlLanguage)
                    setStatus("Determining Printer Language", Color.YELLOW)
                    val pl = SGD.GET("device.languages", connection)
                    setStatus("Printer Language $retriever", Color.BLUE)
                } catch (e: ConnectionException) {
                    Log.e("conecct", "äca" + e.message)
                    printer = null
                } catch (e: ZebraPrinterLanguageUnknownException) {
                    Log.e("conecct", "äca" + e.message)
                    printer = null

                }
            }
        } catch (e: ConnectionException) {
            Log.e("conecct", "äca" + e.message)
        } catch (e: Exception) {
            Log.e("conecct", "äca" + e.message)
        } finally {
            connection?.close()
        }

    }

    fun convertExtendedAscii(input: String): ByteArray? {
        val length = input.length
        val retVal = ByteArray(length)
        for (i in 0 until length) {
            val c = input[i]
            if (c.toInt() < 127) {
                retVal[i] = c.toByte()
            } else {
                retVal[i] = (c.toInt() - 256).toByte()
            }
        }
        return retVal
    }

    private fun getMacAddressFieldText(): String {
        //   return macAddressEditText.getText().toString()
        return ""
    }

    private fun setStatus(statusMessage: String, color: Int) {
        Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_LONG).show()
//        runOnUiThread {
//            statusField.setBackgroundColor(color)
//            statusField.setText(statusMessage)
//        }
//        DemoSleeper.sleep(1000)
    }
}