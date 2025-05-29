// Top-level build file where you can add configuration options common to all sub-projects/modules.

// PASTIKAN ANDA MENGGUNAKAN SALAH SATU DARI DUA FORMAT INI, SESUAI FILE ANDA:

// FORMAT 1: Jika file Anda menggunakan blok 'plugins {}' modern
plugins {
    id("com.android.application") version "8.2.2" apply false // Ganti 8.2.2 dengan versi plugin Android Anda
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Ganti dengan versi plugin Kotlin Anda (jika pakai Kotlin)
    // WAJIB ADA UNTUK NAVIGATION SAFE ARGS:
    id("androidx.navigation.safeargs") version "2.7.7" apply false // Ganti 2.7.7 dengan versi stabil terbaru NavComponent
}

// FORMAT 2: Jika file Anda menggunakan format 'buildscript { ... }' lama
// buildscript {
//     val navVersion = "2.7.7" // Ganti dengan versi stabil terbaru NavComponent
//     repositories {
//         google()
//         mavenCentral()
//     }
//     dependencies {
//         // classpath("com.android.tools.build:gradle:8.2.2") // Ganti dengan versi plugin Android Anda
//         // classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // Ganti dengan versi plugin Kotlin Anda
//         // WAJIB ADA UNTUK NAVIGATION SAFE ARGS:
//         classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
//     }
// }

// tasks.register("clean", Delete::class) {
// delete(rootProject.buildDir)
// }
