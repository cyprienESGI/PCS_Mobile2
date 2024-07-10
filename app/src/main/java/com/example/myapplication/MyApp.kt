package com.example.myapplication

import android.app.Application

class MyApp : Application() {
    // Use 'const' for compile-time constants
    companion object {
        const val URL_API: String = "http://195.200.14.164:3000"
        const val URL_WEB: String = "http://195.200.14.164:5173/"
    }
}
