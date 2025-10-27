package co.edu.unal.gps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place

data class PoiItem(val place: Place, val distance: Double)

class PoiAdapter(private val poiList: MutableList<PoiItem>) :
    RecyclerView.Adapter<PoiAdapter.PoiViewHolder>() {

    class PoiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poiName: TextView = itemView.findViewById(R.id.poi_name)
        val poiDistance: TextView = itemView.findViewById(R.id.poi_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.poi_item, parent, false)
        return PoiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {
        val currentItem = poiList[position]
        holder.poiName.text = currentItem.place.name
        holder.poiDistance.text = String.format("%.2f km", currentItem.distance)
    }

    override fun getItemCount() = poiList.size

    fun updateData(newPoiList: List<PoiItem>) {
        poiList.clear()
        poiList.addAll(newPoiList)
        notifyDataSetChanged()
    }
}