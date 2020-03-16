package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.patientlistitem.view.*
import kotlinx.android.synthetic.main.patientlistitem.view.patientName
import kotlinx.android.synthetic.main.permissionlistitem.view.*
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserObjectWithOnlyUsername
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class PermissionAdapter(
    val context: Context,
    val items: ArrayList<UserData>,
    val listener: PermissionListener
) :
    RecyclerView.Adapter<PermissionAdapter.MyViewHolder>() {
    var noCallInProgress= true

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.patientName
        val image = itemView.prullenBak
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.permissionlistitem, parent, false) as View
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.setText(items[position].username)
        holder.image.setOnClickListener {
            delete_user(items[position].userID)
        }
        holder.itemView.setOnClickListener {
            listener.onItemClick(holder.adapterPosition, items[position])

        }
    }

    fun delete_user(id: Int) {
        if (noCallInProgress) {
            noCallInProgress = false

            val call = MonitorApplication.getInstance()
                .apiHelper.returnAPIServiceWithAuthenticationTokenAdded().deleteUserPermission(id)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        noCallInProgress = true

                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            errorHandling(errorMessage)

                        } catch (e: Exception) {
                            errorHandling(response.message())
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    errorHandling(t.message!!)
                }

                private fun errorHandling(message: String) {
                    //errorField.text = message
                   // errorField.visibility = View.VISIBLE
                    noCallInProgress = true
                }
            })
        }
    }


}