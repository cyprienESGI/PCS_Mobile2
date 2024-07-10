package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.auth0.android.jwt.JWT
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Profile : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupDrawer()

        // Load user data from API
        fetchUserInfo()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.profile)
        navView = findViewById(R.id.nav_view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val btnMenu = findViewById<ImageView>(R.id.burger_menu)
        btnMenu.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.openDrawer(navView)
            } else {
                drawerLayout.closeDrawer(navView)
            }
        }
    }

    private fun setupDrawer() {
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@Profile, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Already in Profile, do nothing
                    true
                }
                R.id.nav_logout -> {
                    clearPreferencesAndLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun clearPreferencesAndLogout() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        val intent = Intent(this@Profile, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchUserInfo() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)

                if (token.isNullOrEmpty()) {
                    throw IllegalStateException("Token is null or empty")
                }

                val decodedToken = JWT(token)
                userId = decodedToken.getClaim("userId").asInt()

                val url = URL("${MyApp.URL_API}/user/$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")

                val responseCode = connection.responseCode
                val responseData = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }

                connection.disconnect()

                if (responseData.isNullOrEmpty()) {
                    throw Exception("Empty response")
                }

                val userData = JSONObject(responseData)
                val user = userData.getJSONObject("user")

                // Save user data to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("email", user.getString("email"))
                    putString("last_name", user.getString("last_name"))
                    putString("first_name", user.getString("first_name"))
                    putString("userPfp", user.getString("userPfp"))
                    apply()
                }

                // Load user data into UI
                loadUserData()

            } catch (e: Exception) {
                Log.e("Profile", "Error fetching user info: ${e.message}", e)
                Toast.makeText(this@Profile, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)

        val userEmail = sharedPreferences.getString("email", "Email not set")
        val userLastName = sharedPreferences.getString("last_name", "Last name not set")
        val userFirstName = sharedPreferences.getString("first_name", "First name not set")
        val userPfp = sharedPreferences.getString("userPfp", null)

        val headerView = navView.getHeaderView(0)

        val userEmailTextView: TextView = headerView.findViewById(R.id.user_address)
        userEmailTextView.text = userEmail

        val userNameTextView: TextView = headerView.findViewById(R.id.user_name)
        userNameTextView.text = "$userLastName $userFirstName"

        val userUrlCircleImageView: CircleImageView = headerView.findViewById(R.id.user_img)
        userPfp?.let {
            Picasso.get().load(it).into(userUrlCircleImageView)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
