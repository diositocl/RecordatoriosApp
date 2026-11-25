package com.example.recordatoriosapp.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecordatorioApiService {

    @GET("recordatorios")
    suspend fun getRecordatorios(): Response<List<RecordatorioDto>>

    @PUT("recordatorios/{id}")
    suspend fun saveRecordatorio(
        @Path("id") id: String,
        @Body recordatorio: RecordatorioDto
    ): Response<RecordatorioDto>

    @DELETE("recordatorios/{id}")
    suspend fun deleteRecordatorio(
        @Path("id") id: String
    ): Response<Unit>
}