# Task: Background Step Monitoring

**Requirement:** FR1.1 - The app must run a foreground service on the watch to subscribe to `DataType.DAILY_STEPS` using the `PassiveMonitoringClient` from Health Services.

---

## Work Done

*   Created the `StepTrackingService.kt` file in `com.stepblocks.wear.services`.
*   The service uses `HealthServices.getClient()` to get a `PassiveMonitoringClient`.
*   A `PassiveListenerCallback` is implemented to handle incoming step data.
*   In `onStartCommand`, the service registers the callback for `DataType.STEPS_DAILY`.
*   Updated the `AndroidManifest.xml` to include the `StepTrackingService`.
*   Added the `ACTIVITY_RECOGNITION` and `FOREGROUND_SERVICE` permissions to the manifest.
*   The project builds successfully with these changes.

### `StepTrackingService.kt`
```kotlin
package com.stepblocks.wear.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import java.util.concurrent.Executor

class StepTrackingService : Service() {

    private val healthServicesClient by lazy { HealthServices.getClient(this) }
    private val passiveMonitoringClient by lazy { healthServicesClient.passiveMonitoringClient }
    private lateinit var mainExecutor: Executor

    private val passiveListenerCallback = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            dataPoints.getData(DataType.STEPS_DAILY).forEach { dataPoint ->
                val steps = dataPoint.value
                // TODO: Handle step count
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mainExecutor = ContextCompat.getMainExecutor(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.STEPS_DAILY))
            .build()

        passiveMonitoringClient.setPassiveListenerCallback(
            config,
            passiveListenerCallback
        )
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
```

### `stepblockswear/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.stepblocks.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.stepblocks.wear"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.health.services.client)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.guava)
}
```

### `gradle/libs.versions.toml`
```toml
[versions]
# ...
healthServices = "1.0.0"
# ...
```
