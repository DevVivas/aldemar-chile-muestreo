package com.aldemar.aquamuestra.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints del backend AquaSample. Retrofit genera la implementación a partir de esta interfaz.
 * Las rutas son relativas a la URL base (BuildConfig.API_BASE_URL), por eso no empiezan con "/".
 */
interface AquaSampleApi {

    /**
     * Quién soy. En el login se pasa [credencial] a mano (todavía no hay sesión guardada);
     * en el resto de llamadas la pone AuthInterceptor.
     */
    @GET("api/auth/me")
    suspend fun me(@Header("Authorization") credencial: String? = null): UsuarioDto

    @GET("api/muestras")
    suspend fun listarMuestras(
        @Query("centro") centro: String? = null,
        @Query("tren") tren: String? = null,
        @Query("linea") linea: String? = null,
        @Query("estado") estado: String? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null
    ): List<MuestraDto>

    @GET("api/muestras/{id}")
    suspend fun obtenerMuestra(@Path("id") id: String): MuestraDto

    /** Crea o actualiza (upsert): es el que usará la sincronización para no duplicar. */
    @PUT("api/muestras/{id}")
    suspend fun guardarMuestra(@Path("id") id: String, @Body muestra: MuestraDto): MuestraDto

    @PATCH("api/muestras/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: String, @Body cambio: CambioEstadoDto): MuestraDto

    /** Sube la foto como formulario multipart con el campo "archivo". */
    @Multipart
    @POST("api/muestras/{id}/foto")
    suspend fun subirFoto(@Path("id") id: String, @Part archivo: MultipartBody.Part): FotoSubidaDto
}
