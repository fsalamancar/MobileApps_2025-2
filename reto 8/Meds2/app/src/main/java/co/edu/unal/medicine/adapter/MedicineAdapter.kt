package co.edu.unal.medicine.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.medicine.DetailActivity
import co.edu.unal.medicine.R
import co.edu.unal.medicine.model.Medicine

class MedicineAdapter(private val medicines: List<Medicine>) :
    RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val principioActivoTextView: TextView = itemView.findViewById(R.id.principio_activo_text_view)
        val nombreComercialTextView: TextView = itemView.findViewById(R.id.nombre_comercial_text_view)
        val fabricanteTextView: TextView = itemView.findViewById(R.id.fabricante_text_view)
        val precioTextView: TextView = itemView.findViewById(R.id.precio_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.principioActivoTextView.text = "Principio Activo: ${medicine.principio_activo}"
        holder.nombreComercialTextView.text = "Nombre Comercial: ${medicine.nombre_comercial}"
        holder.fabricanteTextView.text = "Fabricante: ${medicine.fabricante}"
        holder.precioTextView.text = "Precio: ${medicine.precio_por_tableta}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_MEDICINE, medicine)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicines.size
    }
}