package com.example.m1tmdb_pessione

import android.telecom.Call
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TempoApi {
    interface TempoApi {

        @GET("getNbTempoDays?TypeAlerte=TEMPO")
        fun getTempoData(): retrofit2.Call<TempoData>

        @GET("searchTempoStore?TypeAlerte=TEMPO")
        fun getTempoColor(): retrofit2.Call<TempoColorResponse>

        @GET("historicTEMPOStore")
        fun getHistoricData(@Query("dateBegin") dateBegin: String, @Query("dateEnd") dateEnd: String): retrofit2.Call<List<TempoColorResponse>>

        companion object {
            private const val BASE_URL = "https://particulier.edf.fr/services/rest/referentiel/"

            fun create(): TempoApi {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                return retrofit.create(TempoApi::class.java)
            }
        }
    }
}
data class TempoColorResponse(
    @SerializedName("dateRelevant")
    val date: String,
    @SerializedName("Tempo")
    val color: String
)