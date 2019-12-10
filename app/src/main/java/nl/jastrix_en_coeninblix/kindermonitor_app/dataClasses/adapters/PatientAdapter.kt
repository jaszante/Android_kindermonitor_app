package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.patientlistitem.view.*
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientListItem
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID

class PatientAdapter(val context: Context,val items: ArrayList<PatientWithID>, val listener: PatientListener) :
    RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.patientName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.patientlistitem, parent, false) as View
        return MyViewHolder(view)

    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.setText(items[position].firstname + " " + items[position].lastname)
        holder.itemView.setOnClickListener{
            listener.onItemClick(holder.adapterPosition, items[position])
        }
    }
}


