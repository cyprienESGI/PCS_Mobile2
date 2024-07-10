package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ListView // Ensure ListView import
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class ListLodging : AppCompatActivity() {
    private val _loading = MutableLiveData<Boolean>()
    //val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    //val error: LiveData<String?> get() = _error

    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_lodging)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchLodgings()
        //fetchImgLodgings()

        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@ListLodging, ListLodging::class.java)
                    startActivity(intent)
                }
                R.id.nav_profil -> {
                    val intent = Intent(this@ListLodging, Profile::class.java)
                    startActivity(intent)

                }
                R.id.nav_login -> {
                    val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    val intent = Intent(this@ListLodging, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        val btnMenu = findViewById<ImageView>(R.id.burger_menu)
        btnMenu.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.openDrawer(navView)
            } else {
                drawerLayout.closeDrawer(navView)
            }
        }

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("userEmail", null)
        val userLastname = sharedPreferences.getString("userLastname", null)
        val userFirstname = sharedPreferences.getString("userFirstname", null)
        val userPfp = sharedPreferences.getString("userPfp", null)

        val headerView = navView.getHeaderView(0)
        val userEmailTextView: TextView = headerView.findViewById(R.id.user_address)
        userEmailTextView.text = userEmail

        val userNameTextView: TextView = headerView.findViewById(R.id.user_name)
        userNameTextView.text = "$userLastname $userFirstname"

        val userUrlCircleImageView: CircleImageView = headerView.findViewById(R.id.user_img)
        val url = "${MyApp.URL_WEB}$userPfp"
        Picasso.get().load(url).into(userUrlCircleImageView)
    }

//    override fun onResume() {
//        super.onResume()
//        checkUserLoggedIn()
//    }
//
//    private fun checkUserLoggedIn() {
//        val sharedPreferences: SharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
//        val token: String? = sharedPreferences.getString("token", null)
//
//        if (token.isNullOrEmpty()) {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private suspend fun makeApiCall(endpoint: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            var responseCode: Int? = null
            var responseMessage: String? = null
            try {
                val url = URL("${MyApp.URL_API}$endpoint")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")

                responseCode = connection.responseCode
                responseMessage = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }

                Pair(responseCode == HttpURLConnection.HTTP_OK, responseMessage)
            } catch (e: Exception) {
                Log.e("MyLog", "Error: ${e.message}", e)
                Pair(false, e.message)
            } finally {
                Log.d("MyLog", "ResponseCode: $responseCode")
                Log.d("MyLog", "ResponseMessage: $responseMessage")
            }
        }
    }

    private fun fetchLodgings() {
        _loading.value = true
        _error.value = null

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = makeApiCall("/apartments")
                if (!response.first) {
                    throw Exception(response.second ?: "An error occurred. Please try again.")
                }

                val data = JSONArray(response.second)
                val listLodgings = mutableListOf<Lodging>()

                for (i in 0 until data.length()) {
                    val jso = data.getJSONObject(i)

                    // Vérification si le champ "img_lodging_id" est null
//                    val idImg: Int? = if (jso.isNull("img_lodging_id")) {
//                        null
//                    } else {
//                        jso.getInt("img_lodging_id")
//                    }

                    // Extraction de l'URL de l'image si présente
//                    val imgLodging = jso.optJSONObject("img_lodging")
//                    val url: String? = imgLodging?.optString("url", null)

                    // Création d'un objet Lodging
                    val lodging = Lodging(
                        jso.getInt("apartments_id"),
                        address = jso.getInt("number").toString()+" "+jso.getString("street"),
//                        jso.getString("city"),
//                        jso.getString("zip_code_lodging"),
                        jso.getInt("price"),
//                        jso.getString("description"),
                        jso.getString("owner_email"),
//                        jso.getInt("validate"),
                        jso.getInt("capacity"),
                        jso.getInt("surface"),
                        jso.getString("apartment_type"),
                        jso.getString("name"),
//                        jso.getInt("rating"),
//                        idImg,
//                        url
                    )
                    listLodgings.add(lodging)
                }

                // Mise à jour de l'adaptateur avec les données récupérées
                val adapter = LodgingAdapter(this@ListLodging, listLodgings)
                val listView = findViewById<ListView>(R.id.list_lodgings)
                listView.adapter = adapter

            } catch (ex: Exception) {
                _error.value = ex.message
                Toast.makeText(this@ListLodging, "Impossible de charger les données", Toast.LENGTH_SHORT).show()
                Log.e("MyLog", "Error lodgings: ${ex.message}")
            } finally {
                _loading.value = false
            }
        }
    }


//    private fun fetchImgLodgings() {
//        _loading.value = true
//        _error.value = null
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = makeApiCall("/api/imgLodgings")
//                if (!response.first) {
//                    throw Exception(response.second ?: "An error occurred. Please try again.")
//                }
//
//                val data = JSONArray(response.second)
//                val listImgLodgings = mutableListOf<ImgLodging>()
//
//                for (i in 0 until data.length()) {
//                    val jso = data.getJSONObject(i)
//
//                    val idCategory = if (jso.isNull("category_id")) null else jso.getInt("category_id")
//                    val idLodging = if (jso.isNull("lodging_id")) null else jso.getInt("lodging_id")
//
//                    val imgLodging = ImgLodging(
//                        jso.getInt("img_lodging_id"),
//                        jso.getString("url"),
//                        idLodging,
//                        idCategory
//                    )
//                    listImgLodgings.add(imgLodging)
//                }
//                ImgLodgingAdapter(this@ListLodging, listImgLodgings)
//
//            } catch (ex: Exception) {
//                withContext(Dispatchers.Main) {
//                    _error.value = ex.message
//                    Toast.makeText(this@ListLodging, "Failed to load data: ${ex.message}", Toast.LENGTH_SHORT).show()
//                    Log.e("MyLog", "Error lodgings: ${ex.message}", ex)
//                }
//            } finally {
//                withContext(Dispatchers.Main) {
//                    _loading.value = false
//                }
//            }
//        }
//    }


}
