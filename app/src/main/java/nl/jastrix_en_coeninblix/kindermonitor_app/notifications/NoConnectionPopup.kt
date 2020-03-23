package nl.jastrix_en_coeninblix.kindermonitor_app.notifications

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication

class NoConnectionPopup(var message: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setPositiveButton("Pauzeer",
                    DialogInterface.OnClickListener { dialog, id ->
                        MonitorApplication.getInstance().noConnectionAlarmNotPauzed = false
                        MonitorApplication.getInstance().startNoConnectionPauzeTimer()
                    })
//                .setNegativeButton("Bel noodnummer",
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // call number
//                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}