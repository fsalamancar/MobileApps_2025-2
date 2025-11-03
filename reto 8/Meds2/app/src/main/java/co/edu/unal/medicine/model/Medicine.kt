package co.edu.unal.medicine.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Medicine(
    val principio_activo: String,
    val unidad_de_dispensacion: String,
    val concentracion: String,
    val unidad_base: String,
    val nombre_comercial: String,
    val fabricante: String,
    val precio_por_tableta: String,
    val factoresprecio: String,
    val numerofactor: String
) : Parcelable