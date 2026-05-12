plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.hotel_pere_maria_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hotel_pere_maria_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Emulador Android → API en el PC: 10.0.2.2. Dispositivo físico: IP LAN de tu máquina (ej. http://192.168.1.x:3000/)
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildToolsVersion = "36.1.0"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //Dependencias para MVVM
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //Dependencias para Conexión a API
    implementation("com.squareup.retrofit2:retrofit:3.0.0") // Retrofit: El motor para las peticiones HTTP
    implementation("com.squareup.retrofit2:converter-gson:3.0.0") // Convierte JSON a objetos Kotlin
    // OkHttp Logging Interceptor: (Opcional pero recomendado)
    // Permite ver en el Logcat qué está enviando y recibiendo la API.
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    //Dependencias para carga de imágenes
    implementation("io.coil-kt:coil-compose:2.7.0")
}