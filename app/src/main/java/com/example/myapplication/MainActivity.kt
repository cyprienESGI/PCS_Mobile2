package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configure window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnConnection = findViewById<Button>(R.id.btn_connection)
        btnConnection.setOnClickListener {
            val emailInput = findViewById<EditText>(R.id.emailInput)
            val email = emailInput.text.toString().trim()

            val passwordInput = findViewById<EditText>(R.id.passwordInput)
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@MainActivity, "Champs obligatoire", Toast.LENGTH_SHORT).show()
            } else {
                handleSubmit(email, password)
            }
        }

        val btnCreateAccount = findViewById<TextView>(R.id.createAccount)
        btnCreateAccount.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateAccount::class.java)
            startActivity(intent)
        }
    }

    private suspend fun makeApiCall(endpoint: String, body: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            var responseCode: Int? = null
            var responseMessage: String? = null
            var token: String? = null
            try {
                val url = URL("${MyApp.URL_API}$endpoint")
                connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // Write request body if provided
                if (body.isNotEmpty()) {
                    OutputStreamWriter(connection.outputStream).use { outputStream ->
                        outputStream.write(body)
                        outputStream.flush()
                    }
                }

                responseCode = connection.responseCode
                responseMessage = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }

                // Extract token from headers
                token = connection.getHeaderField("Authorization")?.replace("Bearer ", "")

                Log.d("MyLog", "Response Code: $responseCode")
                Log.d("MyLog", "Response Message: $responseMessage")
                Log.d("MyLog", "Token retrieved: $token")

                Pair(responseCode == HttpURLConnection.HTTP_OK, responseMessage)
            } catch (e: Exception) {
                Log.e("MyLog", "Error: ${e.message}", e)
                Pair(false, e.message)
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun handleSubmit(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _error.value = "Please fill in all fields."
            return
        }

        _loading.value = true
        _error.value = null

        Log.d("MyLog", "Attempting login with email: $email")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val formData = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                Log.d("MyLog", "JSON sent: $formData")

                val (success, response) = makeApiCall("/auth/login", formData.toString())
                if (!success || response.isNullOrEmpty()) {
                    throw Exception("An error occurred. Please try again.")
                }

                val token = extractTokenFromHeader(response)
                if (token.isNullOrEmpty()) {
                    throw Exception("Token is null or empty")
                }

                saveTokenToSharedPreferences(token)

                // Proceed to the next activity
                Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, ListLodging::class.java)
                startActivity(intent)

            } catch (ex: Exception) {
                _error.value = ex.message
                Log.e("MyLog", "Error signin: ${ex.message}")
                if ((ex.message?.contains("Failed to connect to") == true) || (ex.message?.contains("Cleartext HTTP traffic to") == true)) {
                    Toast.makeText(this@MainActivity, "Pas de connexion", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Identifiant invalide", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _loading.value = false
            }
        }
    }

    private fun extractTokenFromHeader(response: String): String? {
        val tokenPrefix = "Bearer "
        return response.substringAfter(tokenPrefix).takeIf { it.isNotBlank() }
    }

    private fun saveTokenToSharedPreferences(token: String) {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token", token)
            apply()
        }
    }
}
