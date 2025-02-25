package com.example.eilgoal

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Retrofit API service interface for Football API
interface FootballApiService {
    // Changed query parameter to "id" to match the expected link
    @GET("fixtures")
    fun getFixtureById(
        @Header("x-apisports-key") apiKey: String,
        @Query("id") fixtureId: Long
    ): Call<ApiResponseFixture>
}

// Data classes for the API response
data class ApiResponseFixture(val response: List<FixtureResponse>)

data class FixtureResponse(
    val fixture: Fixture,
    val teams: Teams,
    val goals: Goals,
    val events: List<Event>?,
    val score: Score
)

data class Fixture(
    val id: Long,
    val referee: String?,
    val timezone: String,
    val date: String,
    val timestamp: Long,
    val periods: Periods,
    val venue: Venue,
    val status: Status
)

data class Periods(val first: Long?, val second: Long?)
data class Venue(val id: Long?, val name: String, val city: String?)
data class Status(val long: String, val short: String, val elapsed: Int?, val extra: Any?)
data class Teams(val home: Team, val away: Team)
data class Team(val id: Long, val name: String, val logo: String, val winner: Boolean?)
data class Goals(val home: Int?, val away: Int?)
data class Score(val halftime: ScoreTime, val fulltime: ScoreTime, val extratime: ScoreTime?, val penalty: ScoreTime?)
data class ScoreTime(val home: Int?, val away: Int?)
data class Event(
    val time: EventTime,
    val team: EventTeam,
    val player: EventPlayer,
    val assist: EventAssist?,
    val type: String,
    val detail: String
)
data class EventTime(val elapsed: Int)
data class EventTeam(val id: Int, val name: String)
data class EventPlayer(val id: Int, val name: String)
data class EventAssist(val id: Int?, val name: String?)

class MatchDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.match_details)

        // Initialize Firebase Realtime Database reference
        val database: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("favoriteTeams")

// Get the fixture id from the intent extras and convert to Long
        val fixtureIdString = intent.getStringExtra("FIXTURE_ID")
        val fixtureId = fixtureIdString?.toLongOrNull()
        if (fixtureId == null) {
            Toast.makeText(this, "Invalid fixture id", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("https://v3.football.api-sports.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(FootballApiService::class.java)

        // Fetch fixture details from the API
        val call = apiService.getFixtureById(
            apiKey = "b858f319bbe18daaa9c6da52158f7a16", // Replace with your API key
            fixtureId = fixtureId
        )

        call.enqueue(object : Callback<ApiResponseFixture> {
            override fun onResponse(
                call: Call<ApiResponseFixture>,
                response: Response<ApiResponseFixture>
            ) {
                // Log the HTTP status code for debugging
                Log.d("MatchDetailsActivity", "Response code: ${response.code()}")
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    // Log the raw API response so you can inspect the JSON
                    Log.d("MatchDetailsActivity", "API Response: $apiResponse")
                    if (apiResponse.response.isNotEmpty()) {
                        val fixtureData = apiResponse.response[0]
                        updateUI(fixtureData, database)
                    } else {
                        Toast.makeText(
                            this@MatchDetailsActivity,
                            "No fixture data found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.e("MatchDetailsActivity", "API Error: ${response.message()}")
                    Toast.makeText(
                        this@MatchDetailsActivity,
                        "API Error: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponseFixture>, t: Throwable) {
                Log.e("MatchDetailsActivity", "Network Error: ${t.message}", t)
                Toast.makeText(
                    this@MatchDetailsActivity,
                    "Network Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // Update UI using the data retrieved from the API
    private fun updateUI(fixture: FixtureResponse, database: DatabaseReference) {
        val teams = fixture.teams
        val homeTeam = teams.home
        val awayTeam = teams.away
        val goals = fixture.goals
        val matchStatus = fixture.fixture.status.long

        // Find views from the layout
        val scoreTextView = findViewById<TextView>(R.id.tvScore)
        val homeLogoImageView = findViewById<ImageView>(R.id.ivTeamA)
        val awayLogoImageView = findViewById<ImageView>(R.id.ivTeamB)
        val matchStatusTextView = findViewById<TextView>(R.id.tvMatchStatus)
        val statisticsListView = findViewById<ListView>(R.id.lvStatistics)
        val scorersLeftTextView = findViewById<TextView>(R.id.tvScorersLeft)
        val scorersRightTextView = findViewById<TextView>(R.id.tvScorersRight)
        val heartHome = findViewById<ImageView>(R.id.ivHeartHome)
        val heartAway = findViewById<ImageView>(R.id.ivHeartAway)

        // Set UI values
        scoreTextView.text = "${goals.home ?: 0} - ${goals.away ?: 0}"
        matchStatusTextView.text = matchStatus
        Glide.with(this).load(homeTeam.logo).into(homeLogoImageView)
        Glide.with(this).load(awayTeam.logo).into(awayLogoImageView)

        // Initialize heart icons to empty
        heartHome.setImageResource(R.drawable.ic_heart_empty)
        heartAway.setImageResource(R.drawable.ic_heart_empty)

        var isHomeFavorite = false
        var isAwayFavorite = false

        // Check favorite status for the home team in Firebase
        database.child("ids")
            .child(homeTeam.id.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isHomeFavorite = snapshot.exists()
                    heartHome.setImageResource(
                        if (isHomeFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_empty
                    )
                }
                override fun onCancelled(error: DatabaseError) { }
            })

        // Check favorite status for the away team in Firebase
        database.child("ids")
            .child(awayTeam.id.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isAwayFavorite = snapshot.exists()
                    heartAway.setImageResource(
                        if (isAwayFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_empty
                    )
                }
                override fun onCancelled(error: DatabaseError) { }
            })

        // Toggle favorite status for the home team
        heartHome.setOnClickListener {
            val teamId = homeTeam.id.toString()
            val newState = !isHomeFavorite
            if (newState) {
                database.child("ids").child(teamId).setValue(teamId)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isHomeFavorite = true
                            heartHome.setImageResource(R.drawable.ic_heart_filled)
                            Toast.makeText(this, "Data inserted", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                database.child("ids").child(teamId).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isHomeFavorite = false
                            heartHome.setImageResource(R.drawable.ic_heart_empty)
                            Toast.makeText(this, "Favorite removed", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        // Toggle favorite status for the away team
        heartAway.setOnClickListener {
            val teamId = awayTeam.id.toString()
            val newState = !isAwayFavorite
            if (newState) {
                database.child("ids").child(teamId).setValue(teamId)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isAwayFavorite = true
                            heartAway.setImageResource(R.drawable.ic_heart_filled)
                            Toast.makeText(this, "Data inserted", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                database.child("ids").child(teamId).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isAwayFavorite = false
                            heartAway.setImageResource(R.drawable.ic_heart_empty)
                            Toast.makeText(this, "Favorite removed", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        // Process match events to display statistics
        val events = fixture.events
        val eventList = mutableListOf<Pair<SpannableString, Boolean>>()
        var scorersLeft = ""
        var scorersRight = ""

        events?.let {
            // Sort events by elapsed time in descending order
            val sortedEvents = it.sortedByDescending { event -> event.time.elapsed }
            for (event in sortedEvents) {
                val elapsedTime = "${event.time.elapsed}'"
                val playerName = event.player.name
                val eventType = event.type
                val teamId = event.team.id
                val assist = event.assist?.name ?: ""
                val isHomeTeam = teamId == homeTeam.id.toInt()

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
                        val cardIcon = if (event.detail == "Yellow Card") "🟨" else "🟥"
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

        // Set up a simple adapter for the list of events
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
