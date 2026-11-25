package com.example.recordatoriosapp.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

enum class ModoServidorSimulado {
    NORMAL,
    ERROR_500,
    ERROR_400,
    TIMEOUT,
    SIN_CONEXION
}

// Simula las respuestas del servidor para probar la app y sus errores
class SimulatedServerInterceptor(
    private val moshi: Moshi
) : Interceptor {

    @Volatile
    var modo: ModoServidorSimulado = ModoServidorSimulado.NORMAL

    private val almacenamientoRemoto = ConcurrentHashMap<String, RecordatorioDto>()
    private val dtoAdapter = moshi.adapter(RecordatorioDto::class.java)
    private val listAdapter = moshi.adapter<List<RecordatorioDto>>(
        Types.newParameterizedType(List::class.java, RecordatorioDto::class.java)
    )
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Simular fallos de red o servidor segun el modo activo
        when (modo) {
            ModoServidorSimulado.SIN_CONEXION -> throw IOException("Sin conexion a internet")
            ModoServidorSimulado.TIMEOUT -> throw SocketTimeoutException("Tiempo de espera agotado")
            ModoServidorSimulado.ERROR_500 -> return construirRespuesta(
                request, 500, "Internal Server Error", """{"error":"Error interno del servidor"}"""
            )
            ModoServidorSimulado.ERROR_400 -> return construirRespuesta(
                request, 400, "Bad Request", """{"error":"Solicitud invalida"}"""
            )
            ModoServidorSimulado.NORMAL -> Unit
        }

        val path = request.url.encodedPath
        val method = request.method

        return when {
            method == "GET" && path.endsWith("/recordatorios") -> {
                val json = listAdapter.toJson(almacenamientoRemoto.values.toList())
                construirRespuesta(request, 200, "OK", json)
            }

            method == "PUT" && path.contains("/recordatorios/") -> {
                val buffer = Buffer()
                request.body?.writeTo(buffer)
                val bodyString = buffer.readUtf8()
                val dto = dtoAdapter.fromJson(bodyString)

                if (dto != null) {
                    almacenamientoRemoto[dto.id] = dto
                    construirRespuesta(request, 200, "OK", dtoAdapter.toJson(dto))
                } else {
                    construirRespuesta(request, 400, "Bad Request", """{"error":"JSON invalido"}""")
                }
            }

            method == "DELETE" && path.contains("/recordatorios/") -> {
                val id = path.substringAfterLast("/")
                almacenamientoRemoto.remove(id)
                construirRespuesta(request, 204, "No Content", "")
            }

            else -> construirRespuesta(request, 404, "Not Found", """{"error":"No encontrado"}""")
        }
    }

    private fun construirRespuesta(
        request: okhttp3.Request,
        code: Int,
        message: String,
        body: String
    ): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(body.toResponseBody(jsonMediaType))
            .build()
    }
}