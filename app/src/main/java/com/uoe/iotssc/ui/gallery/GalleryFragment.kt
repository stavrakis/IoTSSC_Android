package com.uoe.iotssc.ui.gallery

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.hardware.Camera
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.uoe.iotssc.*
import org.json.JSONArray
import org.json.JSONObject

data class ActivityClass(val activity: Int, val time_start: String, val time_end: String, val device: String)

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    lateinit var queue: RequestQueue
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_gallery, container, false)

        val lines = listOf("item 1", "item ss2", "item 3")

        queue = Volley.newRequestQueue(this.requireContext())
        sharedPrefs = this.requireContext().getSharedPreferences("user", MODE_PRIVATE)

        getHistory()

        return root
    }

    fun getHistory() {
        val url = "$url_base/get/history"
        var details = HashMap<String, String?>()
        details["token"] = sharedPrefs.getString("login_token", null)

        val jsonRequest =
                JsonObjectRequest(Request.Method.POST, url, JSONObject(details as Map<*, *>),
                        { response -> onGetHistoryCallback(response) },
                        { volleyError -> Toast.makeText(this.context, volleyError.message, Toast.LENGTH_LONG) })
        queue.add(jsonRequest)
    }

    fun onGetHistoryCallback(response: JSONObject) {
        var jsonReply = response.getJSONArray("data")

        val recyclerview = root.findViewById<RecyclerView>(R.id.recyclerview1)
        // Assign retrieved history to RecyclerView adapter as data source
        val adapter = CustomAdapter(jsonReply)
        val layoutmanager = LinearLayoutManager(this.context)
        recyclerview.layoutManager = layoutmanager
        recyclerview.adapter = adapter
    }
}


internal class CustomAdapter(private var itemsList: JSONArray) :
        RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemTextView: TextView = view.findViewById(R.id.itemTextView)
        var textView3: TextView = view.findViewById(R.id.textView3)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemsList.getJSONObject(position)
        if (item.getInt("activity") == 1) holder.itemTextView.text = "Running"
        else holder.itemTextView.text = "Walking"
        holder.textView3.text = "${item.getString("time_start")} - ${item.getString("time_end")}"
    }
    override fun getItemCount(): Int {
        return itemsList.length()
    }
}