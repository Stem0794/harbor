plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.monstera.harbor.core.topology"
    compileSdk = 36
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    testImplementation(libs.junit)
}
