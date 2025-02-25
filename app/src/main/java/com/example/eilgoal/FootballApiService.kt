import com.example.eilgoal.service.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface FootballApiService {
    @Headers("x-apisports-key: b858f319bbe18daaa9c6da52158f7a16") // Remplace par ta clé API
    @GET("fixtures")
    fun getNextMatches(
        @Query("season") season: Int,
        @Query("status") status:String="FT",
        @Query("league") league:String="2"
    ): Call<MatchResponse>

    @Headers("x-apisports-key: b858f319bbe18daaa9c6da52158f7a16")
    @GET("fixtures")
    fun getLiveMatches(
        @Query("season") season: Int,
        @Query("live") live: String = "all"
    ): Call<MatchResponse>

    @Headers("x-apisports-key: b858f319bbe18daaa9c6da52158f7a16")
    @GET("injuries")
    fun getInjuries(
        @Query("league") league: Int,
        @Query("season") season: Int
    ): Call<InjuriesResponse>

    @Headers("x-apisports-key: b858f319bbe18daaa9c6da52158f7a16") // Remplace par ta clé API
    @GET("fixtures")
    fun getFinishedMatches(
        @Query("status") status: String = "FT",
        @Query("date") date: String // Format YYYY-MM-DD
    ): Call<MatchResponse>

    @GET("fixtures")
    fun getPlayedMatches(
        @Query("date") date: String,  // Format: YYYY-MM-DD
        @Query("status") status: String = "FT"
    ): Call<MatchResponse>


}
