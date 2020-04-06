package nl.jastrix_en_coeninblix.kindermonitor_app.graphPage

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class XasFormatter : ValueFormatter() {

companion object {
    var XasList = ArrayList<Float>()
}
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {



        return XasList.getOrNull(value.toInt()).toString()
    }



}
