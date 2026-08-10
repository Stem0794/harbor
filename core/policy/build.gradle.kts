plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.monstera.harbor.core.policy"
    compileSdk = 36
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:topology"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
}
