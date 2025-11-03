package co.edu.unal.medicine.service

import co.edu.unal.medicine.model.Medicine
import retrofit2.http.GET

interface MedicineApiService {
    @GET("resource/3t73-n4q9.json")
    suspend fun getMedicines(): List<Medicine>
}