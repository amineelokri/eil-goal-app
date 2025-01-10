package com.example.eilgoal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eilgoal.ui.theme.EilGoalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.suivimatch)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val items=listOf<DataClass>(
            DataClass(R.drawable.fcb,"FC Barcelona",R.drawable.mcity,"Manchester City"),
            DataClass(R.drawable.arsenal,"Arsenal",R.drawable.mcity,"Manchester City"),
            DataClass(R.drawable.arsenal,"Arsenal",R.drawable.fcb,"FC Barcelona")
            )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ItemAdapter(items)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EilGoalTheme {
        Greeting("Android")
    }
}