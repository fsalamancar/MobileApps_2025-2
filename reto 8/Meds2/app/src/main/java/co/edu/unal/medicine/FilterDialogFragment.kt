package co.edu.unal.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class FilterDialogFragment : DialogFragment() {

    interface FilterDialogListener {
        fun onApplyFilters(principioActivo: String, nombreComercial: String, fabricante: String)
        fun onClearFilters()
    }

    private var listener: FilterDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = activity as? FilterDialogListener

        val etPrincipioActivo = view.findViewById<EditText>(R.id.filter_principio_activo)
        val etNombreComercial = view.findViewById<EditText>(R.id.filter_nombre_comercial)
        val etFabricante = view.findViewById<EditText>(R.id.filter_fabricante)
        val btnApply = view.findViewById<Button>(R.id.apply_filters_button)
        val btnClear = view.findViewById<Button>(R.id.clear_filters_button)

        btnApply.setOnClickListener {
            val principioActivo = etPrincipioActivo.text.toString()
            val nombreComercial = etNombreComercial.text.toString()
            val fabricante = etFabricante.text.toString()
            listener?.onApplyFilters(principioActivo, nombreComercial, fabricante)
            dismiss()
        }

        btnClear.setOnClickListener {
            etPrincipioActivo.setText("")
            etNombreComercial.setText("")
            etFabricante.setText("")
            listener?.onClearFilters()
            dismiss()
        }
    }
}