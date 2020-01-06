package nl.jastrix_en_coeninblix.kindermonitor_app.ui.slideshow


import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import nl.jastrix_en_coeninblix.kindermonitor_app.R


class SlideshowFragment : Fragment() {

    private lateinit var slideshowViewModel: SlideshowViewModel
    private lateinit var hMin: TextView
    private lateinit var hMax: TextView

    private lateinit var aMin: TextView
    private lateinit var aMax: TextView

    private lateinit var sMin: TextView
    private lateinit var sMax: TextView

    private lateinit var tMin: TextView
    private lateinit var tMax: TextView

    private var changed: Boolean = false
    private lateinit var buttonSave: Button
    private lateinit var spinner: Spinner


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
            open_Number_picker(hMin, 50, 200)

        }
        hMax = currentView.findViewById(R.id.hMax)
        hMax.setOnClickListener {
            open_Number_picker(hMax, 50, 200)

        }

        aMin = currentView.findViewById(R.id.aMin)
        aMin.setOnClickListener {
            open_Number_picker(aMin, 10, 80)

        }
        aMax = currentView.findViewById(R.id.aMax)
        aMax.setOnClickListener {
            open_Number_picker(aMax, 10, 80)

        }

        sMin = currentView.findViewById(R.id.sMin)
        sMin.setOnClickListener {
            open_Number_picker(sMin, 85, 100)

        }
        sMax = currentView.findViewById(R.id.sMax)
        sMax.setOnClickListener {
            open_Number_picker(sMax, 85, 100)
        }

        tMin = currentView.findViewById(R.id.tMin)
        tMin.setOnClickListener {
            open_Number_picker(tMin, 35, 41)

        }
        tMax = currentView.findViewById(R.id.tMax)
        tMax.setOnClickListener {
            open_Number_picker(tMax, 35, 41)
        }

        buttonSave = currentView.findViewById(R.id.buttonSave)
        buttonSave.setOnClickListener {
            // dingen voor API call

            buttonSave.visibility = View.INVISIBLE
        }
        spinner = currentView.findViewById(R.id.spinner_alarm)
        val array: Array<String> = arrayOf("30 sec", "60 sec", "90 sec", "120 sec ", "180 sec")

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = adapter.getItem(position)
            }

        }

    }


    fun open_Number_picker(textview: TextView, min: Int, max: Int) {

        val linearLayout = layoutInflater.inflate(R.layout.view_number_picker, null) as LinearLayout
        val numberpicker = linearLayout.findViewById<View>(R.id.numberPicker1) as NumberPicker
        numberpicker.minValue = min
        numberpicker.maxValue = max

        val builder: AlertDialog = AlertDialog.Builder(this.context)
            .setPositiveButton("Selecteer", null)
            .setNegativeButton("Annuleren", null)
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
                changed = true
                buttonSave.visibility = View.VISIBLE

            })
    }
}