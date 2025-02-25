package com.example.eilgoal

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.InputStream

class MatchDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.match_details)

        // Initialize Firebase Realtime Database reference
        val database: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("favoriteTeams")

        // Retrieve fixture data from JSON asset
        val fixtureId = intent.getStringExtra("FIXTURE_ID")
        val jsonData = loadJSONFromAsset("dyalk.json")

        if (jsonData != null) {
            val jsonObject = JSONObject(jsonData)
            val fixture = jsonObject.getJSONArray("response").getJSONObject(0)
            val teams = fixture.getJSONObject("teams")
            val homeTeam = teams.getJSONObject("home")
            val awayTeam = teams.getJSONObject("away")
            val goals = fixture.getJSONObject("goals")
            val matchStatus = fixture.getJSONObject("fixture")
                .getJSONObject("status").getString("long")

            // Find views from layout
            val scoreTextView = findViewById<TextView>(R.id.tvScore)
            val homeLogoImageView = findViewById<ImageView>(R.id.ivTeamA)
            val awayLogoImageView = findViewById<ImageView>(R.id.ivTeamB)
            val matchStatusTextView = findViewById<TextView>(R.id.tvMatchStatus)
            val statisticsListView = findViewById<ListView>(R.id.lvStatistics)
            val scorersLeftTextView = findViewById<TextView>(R.id.tvScorersLeft)
            val scorersRightTextView = findViewById<TextView>(R.id.tvScorersRight)

            // Heart icons for favorite teams
            val heartHome = findViewById<ImageView>(R.id.ivHeartHome)
            val heartAway = findViewById<ImageView>(R.id.ivHeartAway)

            // Set initial UI values
            scoreTextView.text = "${goals.getInt("home")} - ${goals.getInt("away")}"
            matchStatusTextView.text = matchStatus
            Glide.with(this).load(homeTeam.getString("logo")).into(homeLogoImageView)
            Glide.with(this).load(awayTeam.getString("logo")).into(awayLogoImageView)

            // Set initial heart icons to empty
            heartHome.setImageResource(R.drawable.ic_heart_empty)
            heartAway.setImageResource(R.drawable.ic_heart_empty)

            var isHomeFavorite = false
            var isAwayFavorite = false

            // Check Firebase for home team favorite status
            database.child(homeTeam.getInt("id").toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isHomeFavorite = snapshot.exists()
                        if (isHomeFavorite) {
                            heartHome.setImageResource(R.drawable.ic_heart_filled)
                        } else {
                            heartHome.setImageResource(R.drawable.ic_heart_empty)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) { }
                })

            // Check Firebase for away team favorite status
            database.child(awayTeam.getInt("id").toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isAwayFavorite = snapshot.exists()
                        if (isAwayFavorite) {
                            heartAway.setImageResource(R.drawable.ic_heart_filled)
                        } else {
                            heartAway.setImageResource(R.drawable.ic_heart_empty)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) { }
                })

            // Toggle for home team favorite with deferred UI update
            heartHome.setOnClickListener {
                val teamId = homeTeam.getInt("id").toString()
                val newState = !isHomeFavorite
                if (newState) {
                    // Attempt to add favorite
                    database.child(teamId).setValue(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isHomeFavorite = true
                                heartHome.setImageResource(R.drawable.ic_heart_filled)
                                Toast.makeText(this, "Data inserted", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                } else {
                    // Attempt to remove favorite
                    database.child(teamId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isHomeFavorite = false
                                heartHome.setImageResource(R.drawable.ic_heart_empty)
                                Toast.makeText(this, "Favorite removed", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                }
            }

            // Toggle for away team favorite with deferred UI update
            heartAway.setOnClickListener {
                val teamId = awayTeam.getInt("id").toString()
                val newState = !isAwayFavorite
                if (newState) {
                    database.child(teamId).setValue(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isAwayFavorite = true
                                heartAway.setImageResource(R.drawable.ic_heart_filled)
                                Toast.makeText(this, "Data inserted", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                } else {
                    database.child(teamId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isAwayFavorite = false
                                heartAway.setImageResource(R.drawable.ic_heart_empty)
                                Toast.makeText(this, "Favorite removed", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                }
            }

            // Process match events
            val events = fixture.optJSONArray("events")
            val eventList = mutableListOf<Pair<SpannableString, Boolean>>()
            var scorersLeft = ""
            var scorersRight = ""

            events?.let {
                val sortedEvents = (0 until it.length()).map { i -> it.getJSONObject(i) }
                    .sortedByDescending { event -> event.getJSONObject("time").getInt("elapsed") }

                for (event in sortedEvents) {
                    val elapsedTime = "${event.getJSONObject("time").getInt("elapsed")}'"
                    val playerName = event.getJSONObject("player").getString("name")
                    val eventType = event.getString("type")
                    val teamId = event.getJSONObject("team").getInt("id")
                    val assist = event.optJSONObject("assist")?.optString("name") ?: ""
                    val isHomeTeam = teamId == homeTeam.getInt("id")

                    if (eventType == "Goal") {
                        if (isHomeTeam) scorersLeft += "$playerName $elapsedTime\n"
                        else scorersRight += "$playerName $elapsedTime\n"
                    }

                    val eventText = when (eventType) {
                        "Goal" -> {
                            val text = "$elapsedTime ⚽ $playerName"
                            val spannable = SpannableString(text + if (assist.isNotEmpty()) "\nAssist: $assist" else "")
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                text.indexOf(playerName),
                                text.indexOf(playerName) + playerName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),
                                text.indexOf(playerName),
                                text.indexOf(playerName) + playerName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                elapsedTime.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),
                                0,
                                elapsedTime.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            if (assist.isNotEmpty()) {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.GRAY),
                                    spannable.indexOf("Assist"),
                                    spannable.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            spannable
                        }
                        "Card" -> {
                            val cardIcon = if (event.getString("detail") == "Yellow Card") "🟨" else "🟥"
                            val text = "$elapsedTime $cardIcon $playerName"
                            val spannable = SpannableString(text)
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                text.indexOf(playerName),
                                text.indexOf(playerName) + playerName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),
                                text.indexOf(playerName),
                                text.indexOf(playerName) + playerName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                elapsedTime.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),
                                0,
                                elapsedTime.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable
                        }
                        "subst" -> {
                            val text = "$elapsedTime 🔄 $playerName"
                            val spannable = SpannableString(text + if (assist.isNotEmpty()) "\nSubstituted: $assist" else "")
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                text.indexOf(playerName),
                                text.indexOf(playerName) + playerName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),
                                text.indexOf(playerName),
                                text.indexOf(playerName) + playerName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                elapsedTime.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),
                                0,
                                elapsedTime.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            if (assist.isNotEmpty()) {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.GRAY),
                                    spannable.indexOf("Substituted"),
                                    spannable.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            spannable
                        }
                        else -> SpannableString("")
                    }
                    if (eventText.isNotEmpty()) eventList.add(Pair(eventText, isHomeTeam))
                }
            }

            scorersLeftTextView.text = scorersLeft.trim()
            scorersRightTextView.text = scorersRight.trim()

            val adapter = object : BaseAdapter() {
                override fun getCount(): Int = eventList.size
                override fun getItem(position: Int): Any = eventList[position]
                override fun getItemId(position: Int): Long = position.toLong()
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val (text, isHomeTeam) = eventList[position]
                    val textView = TextView(this@MatchDetailsActivity)
                    textView.text = text
                    textView.textSize = 16f
                    textView.setPadding(16, 8, 16, 8)
                    textView.gravity = if (isHomeTeam) Gravity.START else Gravity.END
                    return textView
                }
            }
            statisticsListView.adapter = adapter
        }
    }

    // Helper function to load JSON from assets
    private fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val inputStream: InputStream = assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}
