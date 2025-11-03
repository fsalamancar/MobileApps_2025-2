package co.edu.unal.medicine

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.medicine.adapter.MedicineAdapter
import co.edu.unal.medicine.viewmodel.MedicineViewModel
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private val medicineViewModel: MedicineViewModel by viewModels()
    private lateinit var searchAdapter: MedicineAdapter
    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchLoadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditText = findViewById(R.id.search_query_edit_text)
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view)
        searchLoadingProgressBar = findViewById(R.id.search_loading_progress_bar)

        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = MedicineAdapter(emptyList())
        searchResultsRecyclerView.adapter = searchAdapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                medicineViewModel.searchMedicines(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        lifecycleScope.launch {
            medicineViewModel.searchResults.collect { results ->
                searchAdapter = MedicineAdapter(results)
                searchResultsRecyclerView.adapter = searchAdapter
            }
        }

        lifecycleScope.launch {
            medicineViewModel.isSearching.collect { isSearching ->
                searchLoadingProgressBar.visibility = if (isSearching) View.VISIBLE else View.GONE
            }
        }
    }
}