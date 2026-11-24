package com.aldemar.aquamuestra.data.remote.marine

import com.aldemar.aquamuestra.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit SEPARADO para Open-Meteo Marine.
 *
 * A propósito NO usa el OkHttp de RetrofitClient: ese tiene AuthInterceptor, que agrega
 * "Authorization: Basic ..." con el usuario y la contraseña de nuestro backend. Enviar esas
 * credenciales a un servidor externo sería filtrarlas. Este cliente no tiene ningún interceptor
 * de autenticación.
 */
object MarineClient {

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()
    }

    val api: MarineApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.MARINE_BASE_URL) // https://marine-api.open-meteo.com/
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarineApi::class.java)
    }
}
