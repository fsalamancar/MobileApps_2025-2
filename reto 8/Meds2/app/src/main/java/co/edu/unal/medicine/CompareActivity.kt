package co.edu.unal.medicine

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.unal.medicine.model.Medicine
import co.edu.unal.medicine.viewmodel.MedicineViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class CompareActivity : AppCompatActivity() {

    private val medicineViewModel: MedicineViewModel by viewModels()
    private lateinit var compareQueryEditText: EditText
    private lateinit var compareButtonTrigger: Button
    private lateinit var compareBarChart: BarChart
    private lateinit var compareLoadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        compareQueryEditText = findViewById(R.id.compare_query_edit_text)
        compareButtonTrigger = findViewById(R.id.compare_button_trigger)
        compareBarChart = findViewById(R.id.compare_bar_chart)
        compareLoadingProgressBar = findViewById(R.id.compare_loading_progress_bar)

        setupChart(compareBarChart)

        compareButtonTrigger.setOnClickListener {
            val query = compareQueryEditText.text.toString()
            if (query.isNotBlank()) {
                medicineViewModel.compareMedicines(query)
            }
        }

        lifecycleScope.launch {
            medicineViewModel.comparisonResults.collect { results ->
                updateChart(results)
            }
        }

        lifecycleScope.launch {
            medicineViewModel.isComparing.collect { isComparing ->
                compareLoadingProgressBar.visibility = if (isComparing) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupChart(chart: BarChart) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setDoubleTapToZoomEnabled(false)
        chart.setHighlightPerDragEnabled(false)
        chart.setHighlightPerTapEnabled(false)

        val xAxis = chart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun updateChart(medicines: List<Medicine>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        medicines.forEachIndexed { index, medicine ->
            val price = medicine.precio_por_tableta.replace(",", ".").toFloatOrNull() ?: 0f
            entries.add(BarEntry(index.toFloat(), price))
            labels.add(medicine.nombre_comercial)
        }

        if (entries.isNotEmpty()) {
            val dataSet = BarDataSet(entries, "Precio por Tableta")
            dataSet.color = Color.BLUE
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 10f

            val barData = BarData(dataSet)
            barData.barWidth = 0.9f
            compareBarChart.data = barData
            compareBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            compareBarChart.xAxis.setDrawLabels(true)
            compareBarChart.notifyDataSetChanged()
            compareBarChart.invalidate()

            // Determine the best option (lowest price)
            val bestOption = medicines.minByOrNull { it.precio_por_tableta.replace(",", ".").toFloatOrNull() ?: Float.MAX_VALUE }
            if (bestOption != null) {
                // You might want to display this in a TextView or Toast
                // For now, let's just log it or show a Toast
                // Toast.makeText(this, "Mejor opción: ${bestOption.nombre_comercial} (${bestOption.precio_por_tableta})", Toast.LENGTH_LONG).show()
            }

        } else {
            compareBarChart.clear()
            compareBarChart.invalidate()
        }
    }
}