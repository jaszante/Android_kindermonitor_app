package nl.jastrix_en_coeninblix.kindermonitor_app.graphPage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import java.text.NumberFormat


class GraphPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_page)

        val graph = findViewById<GraphView>(R.id.graph)
        graph.setTitle("test123")
        val series = LineGraphSeries(
            arrayOf(
                DataPoint(0.0, 1.0),
                DataPoint(1.0, 5.0),
                DataPoint(2.00, 3.0),
                DataPoint(3.0, 35.0),
                DataPoint(4.0, 100.0),
                DataPoint(4.5, 150.0),
                DataPoint(5.0, 65.0)
            )
        )
        val view = graph.getViewport()
        view.borderColor = getColor(R.color.colorPrimaryDark)
        view.backgroundColor = getColor(R.color.white)
        view.isScrollable = true
        graph.addSeries(series)
    }
}
