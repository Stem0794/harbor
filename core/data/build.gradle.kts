plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.theodorekonikowski.harbor.core.data"
    compileSdk = 36
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:topology"))
    implementation(project(":core:policy"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
}
