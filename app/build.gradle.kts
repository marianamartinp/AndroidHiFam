plugins {
    alias(libs.plugins.androidApplication)
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.mariana.androidhifam"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mariana.androidhifam"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        dataBinding = true
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // FragmentContainerView
    val fragVersion = "1.6.2"
    implementation("androidx.fragment:fragment-ktx:$fragVersion")

    // NavigationComponent
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    implementation("com.google.android.material:material:1.11.0")

    // Actualización, proporciona nuevos métodos
    implementation("androidx.appcompat:appcompat:1.6.0-alpha04")

    // Refrescar la pantalla
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Integración Google Drive
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation("com.google.http-client:google-http-client-gson:1.44.1")
    implementation("com.google.api-client:google-api-client-android:1.26.0") {
        exclude(group= "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0") {
        exclude(group= "org.apache.httpcomponents")
    }

}

configurations {
    all {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}