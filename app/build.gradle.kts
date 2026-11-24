import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// URL del backend: se lee de local.properties (API_BASE_URL=...), que NO se sube a git.
// Por defecto 10.0.2.2, que en el emulador apunta al localhost del PC.
val localProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val apiBaseUrl: String = localProperties.getProperty("API_BASE_URL") ?: "http://10.0.2.2:8080/"

// API externa Open-Meteo Marine. Se puede cambiar con -PMARINE_BASE_URL=... (solo para pruebas).
val marineBaseUrl: String = (project.findProperty("MARINE_BASE_URL") as String?)
    ?: localProperties.getProperty("MARINE_BASE_URL")
    ?: "https://marine-api.open-meteo.com/"

android {
    namespace = "com.aldemar.aquamuestra"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.aldemar.aquamuestra"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "MARINE_BASE_URL", "\"$marineBaseUrl\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room (base de datos local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Red: Retrofit + Gson + log de peticiones
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Imágenes: Coil (carga) + Telephoto (zoom con pellizco y doble toque)
    implementation(libs.coil.compose)
    implementation(libs.telephoto.zoomable.image.coil)

    // Cámara: CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Sesión y preferencias
    implementation(libs.androidx.datastore.preferences)

    // Sincronización en segundo plano
    implementation(libs.androidx.work.runtime.ktx)

    // Corrutinas
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}