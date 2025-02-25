package com.example.eilgoal

data class DataClass(var equipeImage: String, var equipe: String, var equipe2Image: String, var equipe2:String,var date:String, var homeGoals: Int?,       // Nombre de buts de l’équipe domicile
                     var awayGoals: Int?,       // Nombre de buts de l’équipe extérieure
                     var elapsed: Int? )
// MatchResponse.kt
data class MatchResponse(
    val get: String,
    val parameters: Parameters,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<Match>
)

data class Parameters(
    val league: String,
    val next: String
)

data class Paging(
    val current: Int,
    val total: Int
)

data class Match(
    val fixture: Fixture,
    val league: League,
    val teams: Teams,
    val goals: Goals,
    val score: Score
)

data class Fixture(
    val id: Long,
    val referee: String?,
    val timezone: String,
    val date: String, // au format ISO, vous pouvez le formatter ensuite
    val timestamp: Long,
    val periods: Periods,
    val venue: Venue,
    val status: Status
)

data class Periods(
    val first: Int?,
    val second: Int?
)

data class Venue(
    val id: Int?,
    val name: String,
    val city: String
)

data class Status(
    val long: String,
    val short: String,
    val elapsed: Int?,
    val extra: Any?
)

data class League(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String,
    val season: Int,
    val round: String,
    val standings: Boolean
)

data class Teams(
    val home: Team,
    val away: Team
)

data class Team(
    val id: Int,
    val name: String,
    val logo: String,
    val winner: Boolean?
)

data class Goals(
    val home: Int?,
    val away: Int?
)

data class Score(
    val halftime: ScoreDetail,
    val fulltime: ScoreDetail,
    val extratime: ScoreDetail,
    val penalty: ScoreDetail
)

data class ScoreDetail(
    val home: Int?,
    val away: Int?
)

data class InjuriesResponse(
    val get: String,
    val parameters: InjuriesParameters,
    val errors: List<String>,
    val results: Int,
    val paging: InjuriesPaging,
    val response: List<Injury>
)

data class InjuriesParameters(
    val league: String,
    val season: String
)

data class InjuriesPaging(
    val current: Int,
    val total: Int
)

data class Injury(
    val player: InjuredPlayer,
    val team: InjuredTeam,
    val fixture: InjuredFixture,
    val league: InjuredLeague
)

data class InjuredPlayer(
    val id: Int,
    val name: String,
    val photo: String,
    val type: String,
    val reason: String
)

data class InjuredTeam(
    val id: Int,
    val name: String,
    val logo: String
)

data class InjuredFixture(
    val id: Int,
    val timezone: String,
    val date: String,
    val timestamp: Long
)

data class InjuredLeague(
    val id: Int,
    val season: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?
)
data class InjuryItem(
    val playerName: String,
    val playerPhoto: String,
    val type: String,
    val reason: String,
    val teamName: String,
    val teamLogo: String
)




