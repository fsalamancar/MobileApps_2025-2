package co.edu.unal.medicine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.medicine.model.Medicine
import co.edu.unal.medicine.service.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MedicineViewModel : ViewModel() {

    private val _allMedicines = MutableStateFlow<List<Medicine>>(emptyList())
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines

    private val _searchResults = MutableStateFlow<List<Medicine>>(emptyList())
    val searchResults: StateFlow<List<Medicine>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _comparisonResults = MutableStateFlow<List<Medicine>>(emptyList())
    val comparisonResults: StateFlow<List<Medicine>> = _comparisonResults

    private val _isComparing = MutableStateFlow(false)
    val isComparing: StateFlow<Boolean> = _isComparing

    init {
        fetchMedicines()
    }

    private fun fetchMedicines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedList = RetrofitInstance.api.getMedicines()
                _allMedicines.value = fetchedList
                _medicines.value = fetchedList // Initially, filtered list is all medicines
            } catch (e: Exception) {
                // Handle error, maybe set an error state
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterMedicines(principioActivo: String, nombreComercial: String, fabricante: String) {
        viewModelScope.launch {
            val filteredList = _allMedicines.value.filter {
                it.principio_activo.contains(principioActivo, ignoreCase = true) &&
                it.nombre_comercial.contains(nombreComercial, ignoreCase = true) &&
                it.fabricante.contains(fabricante, ignoreCase = true)
            }
            _medicines.value = filteredList
        }
    }

    fun clearFilters() {
        viewModelScope.launch {
            _medicines.value = _allMedicines.value
        }
    }

    fun searchMedicines(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                val lowerCaseQuery = query.lowercase()
                _searchResults.value = _allMedicines.value.filter { medicine ->
                    medicine.principio_activo.lowercase().contains(lowerCaseQuery) ||
                    medicine.nombre_comercial.lowercase().contains(lowerCaseQuery) ||
                    medicine.fabricante.lowercase().contains(lowerCaseQuery)
                }
            }
            _isSearching.value = false
        }
    }

    fun compareMedicines(query: String) {
        viewModelScope.launch {
            _isComparing.value = true
            if (query.isBlank()) {
                _comparisonResults.value = emptyList()
            } else {
                val lowerCaseQuery = query.lowercase()
                _comparisonResults.value = _allMedicines.value.filter { medicine ->
                    medicine.nombre_comercial.lowercase().contains(lowerCaseQuery) ||
                    medicine.principio_activo.lowercase().contains(lowerCaseQuery)
                }
            }
            _isComparing.value = false
        }
    }
}