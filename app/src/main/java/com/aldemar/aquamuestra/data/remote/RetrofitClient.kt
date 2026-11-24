package com.aldemar.aquamuestra.data.remote

import com.aldemar.aquamuestra.BuildConfig
import com.aldemar.aquamuestra.ui.theme.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP único de la app (singleton): se crea la primera vez que se usa.
 *
 * - Timeouts cortos (10 s): en terreno es mejor avisar rápido "sin conexión" que dejar esperando.
 * - AuthInterceptor: agrega las credenciales de la sesión a cada petición.
 * - Logging: muestra peticiones y respuestas en Logcat, SOLO en debug (nunca en la app publicada).
 */
object RetrofitClient {

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor { SessionManager.credencial })
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        // BASIC: método, URL, código y tiempo. No imprime cabeceras (ni la contraseña).
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }
            .build()
    }

    val api: AquaSampleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AquaSampleApi::class.java)
    }
}
