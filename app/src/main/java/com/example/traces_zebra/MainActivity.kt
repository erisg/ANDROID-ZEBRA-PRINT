package com.example.traces_zebra

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printButton.setOnClickListener {
            printConfigLabelUsingDnsName("ZEBRA")
        }
    }

    @Throws(ConnectionException::class)
    private fun sendZplOverTcp(theIpAddress: String) {
        // Instantiate connection for ZPL TCP port at given address
        val thePrinterConn: Connection = TcpConnection(theIpAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT)
        try {
            // Open the connection - physical connection is established here.
            thePrinterConn.open()

            // This example prints "This is a ZPL test." near the top of the label.
            val zplData = "^XA^FO20,20^A0N,25,25^FDThis is a ZPL test.^FS^XZ"

            // Send the data to printer as a byte array.
            thePrinterConn.write(zplData.toByteArray())
        } catch (e: ConnectionException) {
            // Handle communications error here.
            e.printStackTrace()
        } finally {
            // Close the connection to release resources.
            thePrinterConn.close()
        }
    }

    @Throws(ConnectionException::class)
    private fun sendCpclOverTcp(theIpAddress: String) {
        // Instantiate connection for CPCL TCP port at given address
        val thePrinterConn: Connection = TcpConnection(theIpAddress, TcpConnection.DEFAULT_CPCL_TCP_PORT)
        try {
            // Open the connection - physical connection is established here.
            thePrinterConn.open()

            // This example prints "This is a CPCL test." near the top of the label.
            val cpclData = """
        ! 0 200 200 210 1
        TEXT 4 0 30 40 This is a CPCL test.
        FORM
        PRINT
        
        """.trimIndent()

            // Send the data to printer as a byte array.
            thePrinterConn.write(cpclData.toByteArray())
        } catch (e: ConnectionException) {
            // Handle communications error here.
            e.printStackTrace()
        } finally {
            // Close the connection to release resources.
            thePrinterConn.close()
        }
    }

    @Throws(ConnectionException::class)
    private fun printConfigLabelUsingDnsName(dnsName: String) {
        val connection: Connection = TcpConnection(dnsName, 9100)
        try {
            connection.open()
            val p = ZebraPrinterFactory.getInstance(connection)
            p.printConfigurationLabel()
        } catch (e: ConnectionException) {
            e.printStackTrace()
        } catch (e: ZebraPrinterLanguageUnknownException) {
            e.printStackTrace()
        } finally {
            // Close the connection to release resources.
            connection.close()
        }
    }

}