// app/src/main/kotlin/App.kt
package com.javanumberguess

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("APP", "Hello Android from Logcat")

        val message = MessageProvider.getMessage()
        Log.d("APP", message)
    }
}