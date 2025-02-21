package com.example.eilgoal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login_success)
        setContentView(R.layout.suivimatch)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val items=listOf<DataClass>(
            DataClass(R.drawable.fcb,"FC Barcelona",R.drawable.mcity,"Manchester City"),
            DataClass(R.drawable.arsenal,"Arsenal",R.drawable.mcity,"Manchester City"),
            DataClass(R.drawable.arsenal,"Arsenal",R.drawable.fcb,"FC Barcelona"),

        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ItemAdapter(items)
    }
}