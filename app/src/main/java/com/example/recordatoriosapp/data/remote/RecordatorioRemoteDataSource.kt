package com.example.recordatoriosapp.data.remote

import com.example.recordatoriosapp.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class RecordatorioRemoteDataSource {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val interceptor = SimulatedServerInterceptor(moshi)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(interceptor)
        .build()

    private val api: RecordatorioApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(RecordatorioApiService::class.java)

    suspend fun getRecordatorios(): RemoteResult<List<RecordatorioDto>> {
        return ejecutarLlamada {
            val response = api.getRecordatorios()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    RemoteResult.Success(body)
                } else {
                    RemoteResult.InvalidData(IllegalStateException("Respuesta vacia"))
                }
            } else {
                RemoteResult.HttpError(response.code(), response.message())
            }
        }
    }

    suspend fun saveRecordatorio(dto: RecordatorioDto): RemoteResult<RecordatorioDto> {
        return ejecutarLlamada {
            val response = api.saveRecordatorio(dto.id, dto)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    RemoteResult.Success(body)
                } else {
                    RemoteResult.InvalidData(IllegalStateException("Respuesta vacia"))
                }
            } else {
                RemoteResult.HttpError(response.code(), response.message())
            }
        }
    }

    suspend fun deleteRecordatorio(id: String): RemoteResult<Unit> {
        return ejecutarLlamada {
            val response = api.deleteRecordatorio(id)
            if (response.isSuccessful) {
                RemoteResult.Success(Unit)
            } else {
                RemoteResult.HttpError(response.code(), response.message())
            }
        }
    }

    private suspend fun <T> ejecutarLlamada(
        bloque: suspend () -> RemoteResult<T>
    ): RemoteResult<T> {
        return try {
            bloque()
        } catch (e: SocketTimeoutException) {
            RemoteResult.Timeout
        } catch (e: IOException) {
            RemoteResult.NoConnection
        } catch (e: Throwable) {
            RemoteResult.InvalidData(e)
        }
    }
}