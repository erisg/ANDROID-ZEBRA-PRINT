package com.example.traces_zebra

import android.R
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.view.View
import android.widget.Toast
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.graphics.internal.ZebraImageAndroid
import com.zebra.sdk.printer.*


class SelectedPrinterManager {

    private val PREFS_NAME = "OurSavedAddress"
    private val bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS"
    private val tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS"
    private val tcpPortKey = "ZEBRA_DEMO_TCP_PORT"

    private val myDialog: ProgressDialog? = null
    private var connection: Connection? = null
    private var builder: AlertDialog.Builder? = null

    fun saveBluetoothAddress(context: Context, address: String?) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)
        val editor = settings.edit()
        editor.putString(bluetoothAddressKey, address)
        editor.apply()
    }

    fun getIp(context: Context): String? {
        val settings = context.getSharedPreferences(PREFS_NAME, 0)
        return settings.getString(tcpAddressKey, "")
    }

    fun getPort(context: Context): String? {
        val settings = context.getSharedPreferences(PREFS_NAME, 0)
        return settings.getString(tcpPortKey, "")
    }

    fun getBluetoothAddress(context: Context): String? {
        val settings = context.getSharedPreferences(PREFS_NAME, 0)
        return settings.getString(bluetoothAddressKey, "")
    }

    fun saveIp(context: Context, ip: String?) {
        val settings = context.getSharedPreferences(PREFS_NAME, 0)
        val editor = settings.edit()
        editor.putString(tcpAddressKey, ip)
        editor.apply()
    }

    fun savePort(context: Context, port: String?) {
        val settings = context.getSharedPreferences(PREFS_NAME, 0)
        val editor = settings.edit()
        editor.putString(tcpPortKey, port)
        editor.apply()
    }

    /**
     * Este método guarda la dirección ingresada de la impresora.
     */


    private fun getAndSaveSettings() {
        saveBluetoothAddress(this, getMacAddressFieldText())
        saveIp(re, getTcpAddress())
        savePort(this, getTcpPortNumber())
    }

    @Throws(ConnectionException::class)
    private fun getPrinterStatus() {
        val printerLanguage: String = SGD.GET("device.languages", connection) //This command is used to get the language of the printer.
        val displayPrinterLanguage = "Printer Language is $printerLanguage"
        SGD.SET("device.languages", "zpl", connection) //This command set the language of the printer to ZPL
        this@MainActivity.runOnUiThread(Runnable { Toast.makeText(this@MainActivity, "$displayPrinterLanguage\nLanguage set to ZPL", Toast.LENGTH_LONG).show() })
    }

    /**
     * Este método se utiliza para crear un nuevo cuadro de diálogo de alerta para firmar e imprimir e implementa las mejores prácticas para verificar el estado de la impresora.
     */
    fun doPerformTest() {
        if (isBluetoothSelected() === false) {
            try {
                val port: Int = getTcpPortNumber().toInt()
                connection = TcpConnection(getTcpAddress(), port)
            } catch (e: NumberFormatException) {
                helper.showErrorDialogOnGuiThread("Port number is invalid")
                return
            }
        } else {
            connection = BluetoothConnection(getMacAddressFieldText())
        }
        try {
            connection.open()
            val printer: ZebraPrinter = ZebraPrinterFactory.getInstance(connection)
            val linkOsPrinter: ZebraPrinterLinkOs = ZebraPrinterFactory.createLinkOsPrinter(printer)
            val printerStatus: PrinterStatus = if (linkOsPrinter != null) linkOsPrinter.currentStatus else printer.getCurrentStatus()
            getPrinterStatus()
            if (printerStatus.isReadyToPrint) {
                UiThreadStatement.runOnUiThread(Runnable { Toast.makeText(this@MainActivity, "Printer Ready", Toast.LENGTH_LONG).show() })
                val view: View = View.inflate(this@MainActivity, R.layout.signature_print_dialog, null)
                builder = AlertDialog.Builder(this@MainActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                builder.setView(view)
                builder.setPositiveButton(getString(R.string.print), DialogInterface.OnClickListener { dialog, which ->
                    try {
                        connection.open()
                        val signatureBitmap = Bitmap.createScaledBitmap(signatureArea.getBitmap(), 300, 200, false)
                        printer.printImage(ZebraImageAndroid(signatureBitmap), 0, 0, signatureBitmap.width, signatureBitmap.height, false)
                        dialog.dismiss()
                        connection.close()
                    } catch (e: ConnectionException) {
                        helper.showErrorDialogOnGuiThread(e.message)
                    }
                })
                builder.setNegativeButton("CANCELAR"), DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                builder.create()
                builder.show()
            } else if (printerStatus.isHeadOpen) {
                //     helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print")
            } else if (printerStatus.isPaused) {
                //     helper.showErrorMessage("Error: Printer Paused")
            } else if (printerStatus.isPaperOut) {
                //     helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print")
            } else {
                //      helper.showErrorMessage("Error: Please check the Connection of the Printer")
            }
            connection!!.close()
            getAndSaveSettings()
        } catch (e: ConnectionException) {
            helper.showErrorDialogOnGuiThread(e.message)
        } catch (e: ZebraPrinterLanguageUnknownException) {
            helper.showErrorDialogOnGuiThread(e.getMessage())
        } finally {
            helper.dismissLoadingDialog()
        }
    }
}