package com.example.hotel_pere_maria_app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.hotel_pere_maria_app.ui.Navegation.NavegationMain

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavegationMain()
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun GreetingPreview(){
    NavegationMain()
}