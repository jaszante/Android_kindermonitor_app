package nl.jastrix_en_coeninblix.kindermonitor_app.ui.slideshow

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import nl.jastrix_en_coeninblix.kindermonitor_app.R


class SlideshowFragment : Fragment() {

    private lateinit var slideshowViewModel: SlideshowViewModel
    private lateinit var hMin: TextView
    private lateinit var hMax: TextView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProviders.of(this).get(SlideshowViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_slideshow, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentView = getView()!!
        hMin = currentView.findViewById(R.id.hMin)
        hMin.setOnClickListener {
            open_Number_picker(hMin, 0, 100)

        }
        hMax = currentView.findViewById(R.id.hMax)
        hMax.setOnClickListener {
            open_Number_picker(hMax, 100, 200)

        }
    }


    fun open_Number_picker(textview: TextView, min: Int, max: Int) {

        val linearLayout = layoutInflater.inflate(R.layout.view_number_picker, null) as LinearLayout
        val numberpicker = linearLayout.findViewById<View>(R.id.numberPicker1) as NumberPicker
        numberpicker.minValue = min
        numberpicker.maxValue = max

        val builder: AlertDialog = AlertDialog.Builder(this.context)
            .setPositiveButton("Submit", null)
            .setNegativeButton("Cancel", null)
            .setView(linearLayout)
            .setCancelable(false)
            .create()
        builder.show()
        //Setting up OnClickListener on positive button of AlertDialog
        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE)
            .setOnClickListener(View.OnClickListener {
                textview.text = numberpicker.value.toString()
                builder.cancel()
            })
    }
}