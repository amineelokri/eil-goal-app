package com.example.eilgoal

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suivimatch)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val team1: TextView = findViewById(R.id.team1)
        val team2: TextView = findViewById(R.id.team2)
        val teamImage1: ImageView = findViewById(R.id.imageteam1)
        val teamImage2: ImageView = findViewById(R.id.imageteam2)
        val date: TextView = findViewById(R.id.hotTime)
        val goals: TextView = findViewById(R.id.goals)
        val victime: ImageView = findViewById(R.id.vicitime)
        val victimeText: TextView = findViewById(R.id.victimeText)

        // 🔹 Charger les données de l'API
        loadLiveMatch(team1, team2, teamImage1, teamImage2, date, goals)
        loadInjury(victime, victimeText)
        loadNextMatches(recyclerView)
    }

    // 📌 Charger les matchs en direct
    private fun loadLiveMatch(
        team1: TextView, team2: TextView, teamImage1: ImageView, teamImage2: ImageView,
        date: TextView, goals: TextView
    ) {
        RetrofitClient.instance.getLiveMatches(season = 2024)
            .enqueue(object : Callback<MatchResponse> {
                override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                    if (response.isSuccessful) {
                        val liveMatches = response.body()?.response
                        println("Live Matches: ${liveMatches?.size}")

                        if (!liveMatches.isNullOrEmpty()) {
                            val match = liveMatches[0]  // Prendre le premier match en live
                            team1.text = match.teams.home.name
                            team2.text = match.teams.away.name
                            date.text = "${match.fixture.status.elapsed ?: 0}'"
                            goals.text = "${match.goals.home ?: 0} : ${match.goals.away ?: 0}"

                            // Charger les images des équipes avec Glide
                            Glide.with(this@HomePageActivity).load(match.teams.home.logo).into(teamImage1)
                            Glide.with(this@HomePageActivity).load(match.teams.away.logo).into(teamImage2)
                        }
                    }
                }

                override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                    date.text = "Erreur de chargement"
                }
            })
    }

    // 📌 Charger les blessures des joueurs
    private fun loadInjury(victime: ImageView, victimeText: TextView) {
        RetrofitClient.instance.getInjuries(league = 2, season = 2024)
            .enqueue(object : Callback<InjuriesResponse> {
                override fun onResponse(call: Call<InjuriesResponse>, response: Response<InjuriesResponse>) {
                    if (response.isSuccessful) {
                        val injuries = response.body()?.response
                        println("Injuries: ${injuries?.size}")

                        if (!injuries.isNullOrEmpty()) {
                            val firstInjury = injuries[0]
                            val injuryItem = InjuryItem(
                                playerName = firstInjury.player.name,
                                playerPhoto = firstInjury.player.photo,
                                type = firstInjury.player.type,
                                reason = firstInjury.player.reason,
                                teamName = firstInjury.team.name,
                                teamLogo = firstInjury.team.logo
                            )

                            Glide.with(this@HomePageActivity).load(injuryItem.playerPhoto).into(victime)
                            val vic = "Le joueur ${injuryItem.playerName} (${injuryItem.teamName}) blessé: ${injuryItem.type}"
                            victimeText.text = vic
                        }
                    }
                }

                override fun onFailure(call: Call<InjuriesResponse>, t: Throwable) {
                    victimeText.text = "Erreur de chargement"
                }
            })
    }
    // 📌 Charger les prochains matchs
    private fun loadNextMatches(recyclerView: RecyclerView) {
        RetrofitClient.instance.getNextMatches(season = 2024)
            .enqueue(object : Callback<MatchResponse> {
                override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                    if (response.isSuccessful) {
                        val matches = response.body()?.response
                        println("Nombre de matchs reçus: ${matches?.size}")

                        if (!matches.isNullOrEmpty()) {
                            val matchList = mutableListOf<DataClass>()

                            for (match in matches) {
                                println("Match ajouté: ${match.teams.home.name} vs ${match.teams.away.name}")
                                matchList.add(
                                    DataClass(
                                        equipe2Image = match.teams.home.logo,
                                        equipe2 = match.teams.home.name,
                                        equipeImage = match.teams.away.logo,
                                        equipe = match.teams.away.name,
                                        date = match.fixture.date.substring(0, 10),
                                        homeGoals = match.goals.home ?: 0,
                                        awayGoals = match.goals.away ?: 0,
                                        elapsed = match.fixture.status.elapsed ?: 0
                                    )
                                )
                            }

                            recyclerView.layoutManager = LinearLayoutManager(this@HomePageActivity)
                            recyclerView.adapter = ItemAdapter(matchList)
                            recyclerView.adapter?.notifyDataSetChanged()
                        } else {
                            println("⚠ Aucune donnée reçue de l'API")
                        }
                    }
                }

                override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                    println("Erreur de chargement des matchs : ${t.message}")
                }
            })
    }
    private fun loadPlayedMatches(recyclerView: RecyclerView) {
        val today = "2025-02-25" // Date fixe, à remplacer par une date dynamique si nécessaire

        RetrofitClient.instance.getPlayedMatches(date = today)
            .enqueue(object : Callback<MatchResponse> {
                override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                    if (response.isSuccessful) {
                        val matches = response.body()?.response
                        println("Matchs joués reçus: ${matches?.size}") // Vérifier combien de matchs sont reçus

                        if (!matches.isNullOrEmpty()) {
                            val matchList = matches.map { match ->
                                DataClass(
                                    equipe2Image = match.teams.home.logo,
                                    equipe2 = match.teams.home.name,
                                    equipeImage = match.teams.away.logo,
                                    equipe = match.teams.away.name,
                                    date = "${match.goals.home ?: 0} - ${match.goals.away ?: 0}", // Score final
                                    homeGoals = match.goals.home ?: 0,
                                    awayGoals = match.goals.away ?: 0,
                                    elapsed = match.fixture.status.elapsed ?: 90 // Durée du match
                                )
                            }

                            recyclerView.layoutManager = LinearLayoutManager(this@HomePageActivity, LinearLayoutManager.HORIZONTAL, false)
                            recyclerView.adapter = ItemAdapter(matchList)
                            recyclerView.adapter?.notifyDataSetChanged()
                        } else {
                            println("⚠ Aucun match joué trouvé pour la date : $today")
                        }
                    } else {
                        println("❌ Erreur API: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                    println("🚨 Erreur de requête : ${t.message}")
                }
            })
    }

    fun fetchPlayedMatches(date: String, callback: (List<DataClass>) -> Unit) {
        RetrofitClient.instance.getPlayedMatches(date = date)
            .enqueue(object : Callback<MatchResponse> {
                override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                    if (response.isSuccessful) {
                        val matches = response.body()?.response
                        println("Matchs joués reçus: ${matches?.size}") // Vérification du nombre de matchs reçus

                        if (!matches.isNullOrEmpty()) {
                            val matchList = matches.map { match ->
                                DataClass(
                                    equipeImage = match.teams.home.logo,
                                    equipe = match.teams.home.name,
                                    equipe2Image = match.teams.away.logo,
                                    equipe2 = match.teams.away.name,
                                    date = "${match.goals.home ?: 0} - ${match.goals.away ?: 0}", // Afficher le score final
                                    homeGoals = match.goals.home ?: 0,
                                    awayGoals = match.goals.away ?: 0,
                                    elapsed = match.fixture.status.elapsed ?: 90 // Temps total du match
                                )
                            }
                            callback(matchList) // Retourne la liste des matchs à l'appelant
                        } else {
                            println("⚠ Aucun match joué trouvé pour la date : $date")
                            callback(emptyList()) // Retourne une liste vide si aucun match n'est trouvé
                        }
                    } else {
                        println("❌ Erreur API: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                    println("🚨 Erreur de requête : ${t.message}")
                    callback(emptyList())
                }
            })
    }

}
