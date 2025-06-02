plugins {
    id("com.android.application")
    // Jika kode aplikasi Anda Java murni dan tidak ada Kotlin sama sekali, plugin kotlin("android") bisa opsional.
    // Namun, karena file gradle Anda .kts, seringkali lebih aman untuk menyertakannya.
    // Jika Anda yakin tidak perlu, Anda bisa coba menghapusnya, tapi jika ada error terkait Kotlin, tambahkan kembali.
    kotlin("android")

    // Plugin Safe Args. Karena kode aplikasi Anda Java, gunakan versi non-Kotlin.
    id("androidx.navigation.safeargs")
    // Jika Anda memutuskan menggunakan fitur KTX untuk Navigation dan argumen Kotlin-friendly,
    // gunakan id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.catatanku" // PASTIKAN INI SESUAI DENGAN PACKAGE NAME ANDA
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.catatanku" // PASTIKAN INI SESUAI
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // Tambahkan kotlinOptions jika Anda menyertakan plugin kotlin("android")
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1") // core-ktx baik untuk interoperabilitas Java/Kotlin
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Room Database (asumsi kode Room Anda Java)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version") // Untuk Java

    // ViewModel and LiveData (versi Java)
    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    val fragment_version = "1.6.2"
    implementation("androidx.fragment:fragment:$fragment_version")       // ← Tambahkan ini
    implementation("androidx.fragment:fragment-ktx:$fragment_version")   // Sudah ada, ini tetap

    // --- Navigation Component (versi Java) ---
    val nav_version = "2.7.7" // PASTIKAN VERSI INI SAMA DENGAN DI PROJECT LEVEL
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.bumptech.glide:glide:4.16.0") // Versi terbaru Glide

}