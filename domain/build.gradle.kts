plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.radiothing.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

}

dependencies {
    implementation(libs.coroutines.core)
    implementation("javax.inject:javax.inject:1")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
}

kotlin {
    jvmToolchain(17)
}
