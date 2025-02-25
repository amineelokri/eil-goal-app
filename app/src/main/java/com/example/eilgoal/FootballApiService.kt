import com.example.eilgoal.InjuriesResponse
import com.example.eilgoal.MatchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface FootballApiService {
    @Headers("x-apisports-key: 9510d4263e33d16ebf91e6edade56b12") // Remplace par ta clé API
    @GET("fixtures")
    fun getNextMatches(
        @Query("season") season: Int,
        @Query("date") date:String ="2025-02-25",
        @Query("status") status:String="FT"
    ): Call<MatchResponse>

    @Headers("x-apisports-key: 9510d4263e33d16ebf91e6edade56b12")
    @GET("fixtures")
    fun getLiveMatches(
        @Query("season") season: Int,
        @Query("live") live: String = "all"
    ): Call<MatchResponse>

    @Headers("x-apisports-key: 9510d4263e33d16ebf91e6edade56b12")
    @GET("injuries")
    fun getInjuries(
        @Query("league") league: Int,
        @Query("season") season: Int
    ): Call<InjuriesResponse>

    @Headers("x-apisports-key: 9510d4263e33d16ebf91e6edade56b12") // Remplace par ta clé API
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
