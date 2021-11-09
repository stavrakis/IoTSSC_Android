package com.uoe.iotssc

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

const val url_base = "<url>"
const val username = "<username>"
const val password = "<password>"

class MainActivity2 : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var queue: RequestQueue
    private lateinit var sharedPrefs: SharedPreferences
    lateinit var timer : Timer
    private lateinit var firebasetoken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialise drawer layout
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home2, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPrefs = getSharedPreferences("user", MODE_PRIVATE)
        queue = Volley.newRequestQueue(this)

        // Get stored login token
        val loginToken = sharedPrefs.getString("login_token", "")
        Log.d("Service", "token: $loginToken")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Error", "FCM registration token retrieval failed", task.exception)
                return@OnCompleteListener
            }

            firebasetoken = task.result!!
            Log.d("Service", "Got firebase token: ${firebasetoken}")
            checkLogin()
        })


    }

    private fun updateFirebaseToken() {
        val url = "$url_base/update_fbtoken"
        val details = HashMap<String, String?>()
        details["username"] = username
        details["token"] = sharedPrefs.getString("login_token", null)
        details["fb_token"] = firebasetoken

        val jsonRequest =
                JsonObjectRequest(
                        Request.Method.POST, url, JSONObject(details as Map<*,*>),
                        { response -> Log.d("Service", "Firebase token update result: ${response.getInt("status")}")},
                        { volleyError -> Log.d("Error", volleyError.message.toString())})
        queue.add(jsonRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity2, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkLogin() {
        val url = "$url_base/check_token"
        val details = HashMap<String, String?>()
        details["username"] = username
        details["token"] = sharedPrefs.getString("login_token", "")
        Log.d("Service", "Checking login token ${details["token"]}")

        val jsonRequest =
            JsonObjectRequest(
                Request.Method.POST, url, JSONObject(details as Map<*,*>),
                { response -> onCheckLoginCallback(response)},
                { volleyError -> Log.d("Error", volleyError.message.toString())})
        queue.add(jsonRequest)
    }

    private fun onCheckLoginCallback(response: JSONObject) {
        Log.d("Service", "checklogin result: ${response.getInt("status")}")

        // If token is invalid, login to get a new one
        if (response.getInt("status") != 1) login()
        else Log.d("Service", "Logged in using existing token ${sharedPrefs.getString("login_token", null)}")
        updateFirebaseToken()
    }

    private fun login() {
        val url = "$url_base/login"
        val details = HashMap<String, String>()
        details["username"] = username
        details["password"] = password

        val jsonRequest =
            JsonObjectRequest(
                Request.Method.POST, url, JSONObject(details as Map<*, *>),
                { response -> onLoginCallback(response) },
                { volleyError -> Toast.makeText(this, volleyError.message, Toast.LENGTH_LONG) })
        queue.add(jsonRequest)
    }

    fun getNow(view: View) {
        Log.d("Service", "run getnow")

        val textView = findViewById<TextView>(R.id.label2)
        val url = "$url_base/get/now?token=${sharedPrefs.getString("login_token", "")}"
        val getNowRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            {response ->  run {
                if (response.getInt("status") == 1) {
                    if (response.getString("activity") == "0") textView.text = "Current activity: Walking"
                    else if (response.getString("activity") == "1") textView.text = "Current activity: Running"
                    else if (response.getString("activity") == "none") textView.text = "Current Activity: None"
                    else textView.text = "Current Activity: unknown"
                }
            }},
            {vollerError -> textView.text = vollerError.message})
        queue.add(getNowRequest)
    }

    fun live_view(view: View) {
        val btn = view.findViewById<ToggleButton>(R.id.toggleButton2)
        if (btn.isChecked) { // If toggled on, fetch data repeatedly
            timer = Timer()
            timer.scheduleAtFixedRate(timerTask {
                getNow(view)
            }, 0,1000*2)
        } else { // If toggled off, switch off data requests
            timer.cancel()
            val textView = findViewById<TextView>(R.id.label2)
            textView.text = "Live tracking disabled"
        }
    }

    private fun onLoginCallback(response: JSONObject) {
        if (response.getInt("status") == 1) {
            Log.d("Service", "Got login token: ${response.getString("token")}")

            // Store login token
            val prefsEditor = sharedPrefs.edit()
            prefsEditor.putString("login_token", response.getString("token"))
            prefsEditor.putString("username", username)
            prefsEditor.commit()
        } else {
            Toast.makeText(this, "Wrong login details", Toast.LENGTH_LONG)
        }
    }


}