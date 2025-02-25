package com.example.eilgoal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Locale

class MatchFavorisActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MatchAdapter
    private val matchList = mutableListOf<FixtureResponse>()

    // Liste statique d'IDs d'équipes favorites (ex. PSG=85, Real=541, etc.)
    private var favoriteTeamIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_favoris)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MatchAdapter(matchList)
        recyclerView.adapter = adapter

        fetchTeamIdsFromFirebase()

        // 2) Méthode pour récupérer les données depuis l'API
        fetchFixturesFromApi()
    }

    /**
     * (Optionnel) Ancienne méthode : lire df.json localement
     */
    /****
     * Récupère la liste d'IDs dans la Realtime Database
     * Structure attendue :
     *
     *  "favoriteTeams": {
     *    "ids": {
     *       "649": "649",
     *       "3874": "3874"
     *    }
     *
     *  }
     ****/
    private fun fetchTeamIdsFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("favoriteTeams/ids")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dynamicIds = mutableListOf<Int>()

                // Récupérer chaque ID dans la base
                for (child in snapshot.children) {
                    val stringValue = child.getValue(String::class.java)
                    val intValue = stringValue?.toIntOrNull()
                    if (intValue != null) {
                        dynamicIds.add(intValue)
                    }
                }

                // Vider la liste existante et ajouter les IDs récupérés
                favoriteTeamIds.clear()
                favoriteTeamIds.addAll(dynamicIds)

                // Puis lancer la récupération API
                fetchFixturesFromApi()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MatchFavorisActivity,
                    "Erreur DB: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchFixturesFromApi() {
        // Créer une instance Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://v3.football.api-sports.io/") // Base URL de l'API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Créer le service
        val apiService = retrofit.create(FootballApiService::class.java)

        // Pour simplifier, on va faire un appel par équipe favorite,
        // puis combiner les résultats dans matchList.
        // (Vous pouvez aussi faire un seul appel si l'API supporte "team=85-529", etc.)
        matchList.clear()

        for (teamId in favoriteTeamIds) {
            // Exemple : on récupère les fixtures d'une équipe pour la saison 2024
            val call = apiService.getFixturesByTeam(
                apiKey = "b858f319bbe18daaa9c6da52158f7a16",   // Remplacez par votre clé API
                teamId = teamId,
                season = 2024
            )

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!
                        // On ajoute les matchs à matchList
                        matchList.addAll(apiResponse.response)
                        matchList.sortBy {
                            it.fixture.timestamp
                        }

                        //On notifie l'adapter
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MatchFavorisActivity, "Erreur API: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@MatchFavorisActivity, "Erreur réseau: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * Interface Retrofit pour l'API
     */
    interface FootballApiService {
        // GET /fixtures?team={teamId}&season={season}
        @GET("fixtures")
        fun getFixturesByTeam(
            @Header("x-apisports-key") apiKey: String, // Clé API
            @Query("team") teamId: Int,
            @Query("season") season: Int
        ): Call<ApiResponse>
    }

    // Classes de données (inchangées)
    data class ApiResponse(val response: List<FixtureResponse>)
    data class FixtureResponse(val fixture: Fixture, val teams: Teams, val goals: Goals, val score: Score)
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

    /**
     * Adapter pour afficher les matchs
     */
    inner class MatchAdapter(private val matches: List<FixtureResponse>) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.match_item, parent, false)
            return MatchViewHolder(view)
        }

        override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
            val match = matches[position]

            // Nom des équipes
            holder.teamAName.text = match.teams.home.name
            holder.teamBName.text = match.teams.away.name

            // Récupération des buts (goals)
            val homeGoals = match.goals.home ?: 0 // Valeur par défaut si null
            val awayGoals = match.goals.away ?: 0

            // Afficher le score "2 - 1"
            holder.date.text = "$homeGoals - $awayGoals"

            // Charger logos via Glide
            Glide.with(holder.itemView.context)
                .load(match.teams.home.logo)
                .placeholder(R.drawable.team_placeholder)
                .error(R.drawable.team_placeholder)
                .into(holder.teamAImage)

            Glide.with(holder.itemView.context)
                .load(match.teams.away.logo)
                .placeholder(R.drawable.team_placeholder)
                .error(R.drawable.team_placeholder)
                .into(holder.teamBImage)
        }

        override fun getItemCount(): Int = matches.size

        inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val teamAName: TextView = itemView.findViewById(R.id.equipe)
            val teamBName: TextView = itemView.findViewById(R.id.equipe2)
            val date: TextView = itemView.findViewById(R.id.date)
            val teamAImage: ImageView = itemView.findViewById(R.id.equipeImage)
            val teamBImage: ImageView = itemView.findViewById(R.id.equipe2Image)
        }
    }
}
