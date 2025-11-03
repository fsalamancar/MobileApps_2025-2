package co.edu.unal.medicine

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.medicine.adapter.MedicineAdapter
import co.edu.unal.medicine.viewmodel.MedicineViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), FilterDialogFragment.FilterDialogListener {

    private val medicineViewModel: MedicineViewModel by viewModels()
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var filterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.medicine_recycler_view)
        loadingProgressBar = findViewById(R.id.loading_progress_bar)
        filterButton = findViewById(R.id.filter_button)

        recyclerView.layoutManager = LinearLayoutManager(this)
        medicineAdapter = MedicineAdapter(emptyList()) // Initialize with empty list
        recyclerView.adapter = medicineAdapter

        filterButton.setOnClickListener {
            // Open filter dialog
            val filterDialog = FilterDialogFragment()
            filterDialog.show(supportFragmentManager, "FilterDialog")
        }

        // Observe filtered medicines
        lifecycleScope.launch {
            medicineViewModel.medicines.collect { medicines ->
                medicineAdapter = MedicineAdapter(medicines) // Create new adapter with filtered list
                recyclerView.adapter = medicineAdapter
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            medicineViewModel.isLoading.collect { isLoading ->
                loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onApplyFilters(principioActivo: String, nombreComercial: String, fabricante: String) {
        medicineViewModel.filterMedicines(principioActivo, nombreComercial, fabricante)
    }

    override fun onClearFilters() {
        medicineViewModel.clearFilters()
    }
}