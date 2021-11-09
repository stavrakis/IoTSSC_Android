package com.uoe.iotssc.ui.home

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.uoe.iotssc.MainActivity2
import com.uoe.iotssc.R
import com.uoe.iotssc.url_base
import java.util.*
import kotlin.concurrent.timerTask

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    lateinit var sharedPrefs : SharedPreferences
    lateinit var queue : RequestQueue
    lateinit var timer: Timer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        sharedPrefs = activity?.getSharedPreferences("user", MODE_PRIVATE)!!
        queue = Volley.newRequestQueue(this.context)
        return root
    }


}