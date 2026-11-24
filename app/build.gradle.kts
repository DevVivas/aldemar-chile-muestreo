import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
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

// ---------------------------------------------------------------- Firma de la APK release
// Los datos del keystore NUNCA van en este archivo ni en git. Se leen de local.properties
// (que está en .gitignore) o, si ahí no están, de variables de entorno con el mismo nombre
// (útil en un servidor de CI). Un valor vacío cuenta como "no está".
fun datoFirma(clave: String): String? =
    localProperties.getProperty(clave)?.takeIf { it.isNotBlank() }
        ?: System.getenv(clave)?.takeIf { it.isNotBlank() }

val keystorePath = datoFirma("KEYSTORE_PATH")
val keystorePassword = datoFirma("KEYSTORE_PASSWORD")
val keyAliasFirma = datoFirma("KEY_ALIAS")
val keyPasswordFirma = datoFirma("KEY_PASSWORD")

/** Qué falta para poder firmar (lista vacía = todo listo). */
val faltanParaFirmar: List<String> = buildList {
    if (keystorePath == null) add("KEYSTORE_PATH")
    else if (!file(keystorePath).exists()) add("KEYSTORE_PATH (no existe el archivo $keystorePath)")
    if (keystorePassword == null) add("KEYSTORE_PASSWORD")
    if (keyAliasFirma == null) add("KEY_ALIAS")
    if (keyPasswordFirma == null) add("KEY_PASSWORD")
}

// Versión de la app (una sola fuente: se usa en defaultConfig y en el nombre del archivo).
val appVersionCode = 1
val appVersionName = "1.0.0"

android {
    namespace = "com.aldemar.aquamuestra"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.aldemar.aquamuestra"
        minSdk = 24
        targetSdk = 37
        // versionCode: número interno que debe SUBIR en cada entrega (Android lo usa para saber qué es más nuevo).
        // versionName: lo que ve el usuario ("1.0.0" = mayor.menor.parche).
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "MARINE_BASE_URL", "\"$marineBaseUrl\"")
    }

    signingConfigs {
        create("release") {
            // Solo se rellenan si existen; si falta algo, la tarea verificarFirmaRelease detiene el build.
            if (faltanParaFirmar.isEmpty()) {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                keyAlias = keyAliasFirma
                keyPassword = keyPasswordFirma
            }
        }
    }

    buildTypes {
        release {
            // Se firma SIEMPRE con el keystore propio (nunca con la llave debug).
            signingConfig = signingConfigs.getByName("release")
            // R8 (minify) sigue desactivado: activarlo exige reglas para Gson/Retrofit/Room y probarlo a fondo.
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

// Nombre de la APK: aquamuestra-v1.0.0-release.apk (y aquamuestra-v1.0.0-debug.apk).
// AGP arma el nombre como "<archivesName>-<buildType>.apk".
base {
    archivesName = "aquamuestra-v$appVersionName"
}

// Si faltan datos de firma, cualquier build release falla con un mensaje claro
// (en vez de generar una APK sin firmar o firmada con la llave debug).
val verificarFirmaRelease by tasks.registering {
    description = "Comprueba que estén los datos del keystore para firmar la release."
    val faltan = faltanParaFirmar
    doLast {
        if (faltan.isNotEmpty()) {
            throw GradleException(
                "No se puede firmar la APK release. Falta: ${faltan.joinToString()}. " +
                    "Complétalo en local.properties (ver local.properties.example) " +
                    "o como variables de entorno con el mismo nombre."
            )
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(verificarFirmaRelease) }

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

    // Gráficos del dashboard (Vico 3.2.x: última estable compilada con Kotlin 2.3, legible por Kotlin 2.2)
    implementation(libs.vico.compose.m3)

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

// ---------------------------------------------------------------------------------------------
// Cobertura de pruebas con Kover (variant debug)
//   ./gradlew :app:koverHtmlReportDebug   → app/build/reports/kover/htmlDebug/index.html
//   ./gradlew :app:koverXmlReportDebug    → app/build/reports/kover/reportDebug.xml
//   ./gradlew :app:koverVerifyDebug       → falla si las líneas cubiertas bajan de 90 %
// ---------------------------------------------------------------------------------------------
kover {
    reports {
        filters {
            // Lo que se mide: la lógica que se puede probar en la JVM con JUnit + MockK.
            includes {
                classes(
                    "com.aldemar.aquamuestra.viewmodel.*",                       // ViewModels y lógica pura
                    "com.aldemar.aquamuestra.data.repository.*",                 // Repositorios
                    "com.aldemar.aquamuestra.data.sync.SincronizadorMuestras*",  // lógica de sincronización
                    "com.aldemar.aquamuestra.data.remote.MuestraMapper*",        // Room <-> JSON
                    "com.aldemar.aquamuestra.data.remote.marine.MarineRepository*",
                    "com.aldemar.aquamuestra.data.remote.Resultado*",            // Resultado y llamarApi()
                )
            }
            excludes {
                // Pantallas Compose: se prueban con Compose UI Test en el dispositivo, no con JUnit.
                annotatedBy("androidx.compose.runtime.Composable")
                classes(
                    "com.aldemar.aquamuestra.ui.*",
                    // Código generado (Room *_Impl, BuildConfig): no lo escribimos nosotros.
                    "*_Impl*",
                    "*.BuildConfig",
                    // Necesitan Android real (base de datos, contexto) o solo arman dependencias.
                    "com.aldemar.aquamuestra.data.local.AppDatabase*",
                    "com.aldemar.aquamuestra.viewmodel.AppViewModelFactory*",
                    // Clientes HTTP: solo configuran Retrofit/OkHttp; su uso se prueba con la API mockeada.
                    "com.aldemar.aquamuestra.data.remote.RetrofitClient*",
                    "com.aldemar.aquamuestra.data.remote.marine.MarineClient*",
                    // Dependen de WorkManager; su lógica vive en SincronizadorMuestras, que sí se mide.
                    "com.aldemar.aquamuestra.data.sync.SyncWorker*",
                    "com.aldemar.aquamuestra.data.sync.SyncScheduler*",
                    // Dibuja el PDF con android.graphics.pdf.PdfDocument (Canvas/Paint de Android): no corre en
                    // la JVM de los tests. Lo que va en el PDF (filtrar validadas, filas, totales) está en
                    // ReporteMuestras, que sí se mide; el PDF se revisa a mano en el emulador.
                    "com.aldemar.aquamuestra.data.reporte.GeneradorPdf*",
                    // Clases de solo datos (sin lógica): estados de UI, eventos y resultados.
                    "com.aldemar.aquamuestra.viewmodel.*UiState*",
                    "com.aldemar.aquamuestra.viewmodel.*Evento*",
                    "com.aldemar.aquamuestra.viewmodel.ResultadoConteo*",
                    "com.aldemar.aquamuestra.viewmodel.PuntoEvolucion*",
                    "com.aldemar.aquamuestra.viewmodel.Punto",
                    "com.aldemar.aquamuestra.viewmodel.EstadoMarino*",
                    "com.aldemar.aquamuestra.viewmodel.BannerSync*",
                )
            }
        }
        variant("debug") {
            verify {
                rule("Cobertura mínima de líneas") {
                    minBound(90) // % de líneas cubiertas sobre lo incluido arriba
                }
            }
        }
    }
}

