<div align="center">
  <picture>
    <img width="120px" alt="Inspektor logo" src="https://raw.githubusercontent.com/shohrabpilot/inspektor/latest/images/inspektor.png">
  </picture>
</div>

# Inspektor 🕵️‍♂️
![main status](https://github.com/shohrabpilot/inspektor/actions/workflows/publish.yaml/badge.svg?branch=latest)
[![kotlin](https://img.shields.io/badge/Kotlin-2.3.21-8949FB.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![ktor](https://img.shields.io/badge/ktor-3.5.0-8949FB.svg?style=flat&logo=kotlin)](https://github.com/ktorio/ktor)
[![latest version](https://img.shields.io/maven-central/v/pro.cashkeeper.inspektor/inspektor?color=blue&label=Version)](https://central.sonatype.com/artifact/pro.cashkeeper.inspektor/inspektor)

> [!CAUTION]
> This library is **not stable**, and the API may change. It is not advised to use it in
> production projects.

Inspektor is an HTTP inspection library for Ktor. It allows you to view HTTP requests and responses,
including basic information, headers, and bodies directly on your device.

![Screenshots](images/screenshots.png)

## Installation

Add the following dependency to your `build.gradle.kts` file:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("pro.cashkeeper.inspektor:inspektor:0.1.0")
}
```

## Usage

To use Inspektor, install the plugin in your `HttpClient` configuration:

```kotlin
import pro.cashkeeper.inspektor.Inspektor

val client = HttpClient {
    install(Inspektor)
}

suspend fun apiCall() {
    client.get("https://example.com")
}
```

### Viewing the logs

To open the inspection UI, call the `openInspektor()` function from your UI (e.g., from a developer menu or a long-press gesture):

```kotlin
import pro.cashkeeper.inspektor.openInspektor

// Trigger this from your UI
openInspektor()
```

- **Android**: Opens a new activity. You can also open it via the system notification.
- **iOS**: Opens as a bottom sheet.
- **Desktop**: Opens in a new window.

### Platform Specifics

#### iOS
Add `-lsqlite3` to **Other Linker Flags** in your Xcode Build Settings.

#### Desktop
Call `setApplicationId` before using Inspektor to define where the database should be stored:

```kotlin
import pro.cashkeeper.inspektor.setApplicationId

fun main() {
    setApplicationId("pro.cashkeeper.sample")
    // ...
}
```

## Configuration

You can customize Inspektor using the `InspektorConfig` block:

```kotlin
install(Inspektor) {
    level = LogLevel.BODY
    maxContentLength = 250_000
    showNotifications = true
    retentionDuration = 30.days
    
    // Filter specific requests
    filter { request -> request.url.host.contains("api.example.com") }
    
    // Sanitize sensitive headers
    sanitizeHeader { header -> header == "Authorization" }
}
```

## Features 🚀

- [x] **Request & Response Logging**: View full details of all Ktor network traffic.
- [x] **Overriding**: Mock or override request/response bodies and headers for testing.
- [x] **HAR Export**: Export logs in HTTP Archive (HAR) format for analysis in Chrome/Fiddler.
- [x] **Multiplatform**: Supports Android, iOS, and Desktop (JVM).

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Inspiration

Inspired by [Chucker](https://github.com/ChuckerTeam/chucker) - An HTTP inspector for Android & OkHttp.
