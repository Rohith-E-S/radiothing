package com.radiothing.data.api

import com.radiothing.data.api.dto.CountryDto
import com.radiothing.data.api.dto.LanguageDto
import com.radiothing.data.api.dto.StationDto
import com.radiothing.data.api.dto.TagDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioBrowserApi {
    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("tag") tag: String? = null,
        @Query("country") country: String? = null,
        @Query("language") language: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true
    ): List<StationDto>

    @GET("json/stations/topvote")
    suspend fun getTopStations(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<StationDto>

    @GET("json/stations/bycountryexact/{country}")
    suspend fun getStationsByCountry(
        @Path("country") country: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<StationDto>

    @GET("json/stations/bytagexact/{tag}")
    suspend fun getStationsByGenre(
        @Path("tag") tag: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<StationDto>

    @GET("json/stations/bylanguageexact/{language}")
    suspend fun getStationsByLanguage(
        @Path("language") language: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<StationDto>

    @GET("json/countries")
    suspend fun getCountries(): List<CountryDto>

    @GET("json/tags")
    suspend fun getTags(
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<TagDto>

    @GET("json/languages")
    suspend fun getLanguages(): List<LanguageDto>

    @POST("json/url/{stationuuid}")
    suspend fun clickStation(
        @Path("stationuuid") stationUuid: String
    )
}
