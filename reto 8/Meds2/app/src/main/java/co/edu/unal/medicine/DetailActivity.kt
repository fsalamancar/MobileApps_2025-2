package co.edu.unal.medicine

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import co.edu.unal.medicine.model.Medicine

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEDICINE = "extra_medicine"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val medicine = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_MEDICINE, Medicine::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_MEDICINE)
        }

        medicine?.let {
            findViewById<TextView>(R.id.detail_principio_activo).text = "Principio Activo: ${it.principio_activo}"
            findViewById<TextView>(R.id.detail_nombre_comercial).text = "Nombre Comercial: ${it.nombre_comercial}"
            findViewById<TextView>(R.id.detail_fabricante).text = "Fabricante: ${it.fabricante}"
            findViewById<TextView>(R.id.detail_concentracion).text = "Concentración: ${it.concentracion}"
            findViewById<TextView>(R.id.detail_unidad_de_dispensacion).text = "Unidad de Dispensación: ${it.unidad_de_dispensacion}"
            findViewById<TextView>(R.id.detail_unidad_base).text = "Unidad Base: ${it.unidad_base}"
            findViewById<TextView>(R.id.detail_precio_por_tableta).text = "Precio por Tableta: ${it.precio_por_tableta}"
            findViewById<TextView>(R.id.detail_factoresprecio).text = "Factores de Precio: ${it.factoresprecio}"
            findViewById<TextView>(R.id.detail_numerofactor).text = "Número de Factor: ${it.numerofactor}"
        }
    }
}