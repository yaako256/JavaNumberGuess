# apkファイルを出力
ガチで大変だった。
結果だけ示す。


## DockerfileにSDKのやつを入れる
```dockerfile
# Dockerfile

# ========================================
# base image
# ========================================
FROM gradle:8.14.3-jdk21 AS base

WORKDIR /workspace

COPY . .

# wrapperがなければ init 用に使う
RUN chmod +x gradlew 2>/dev/null || true

# ========================================
# dev target（init用）
# ========================================
FROM base AS init

# initだけ実行する環境
CMD ["gradle", "init"]


# ========================================
# build target（ビルド＋実行準備）
# ========================================
FROM base AS build

RUN ./gradlew build

# デバッグ（重要）
RUN ls -R /workspace/app/build/libs

# ========================================
# runtime target（最終実行）
# ========================================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /workspace

# ビルド成果物を移動する
COPY --from=build /workspace/app/build/libs/app.jar app.jar

# docker run時に実行する内容
# アプリを実行する
ENTRYPOINT ["java", "-jar", "app.jar"]



# ========================================
# アンドロイド用apk作成
# ========================================
FROM gradle:8.14.3-jdk21 AS android


# ----------- SDK -----------
# 必須ツール
RUN apt-get update && apt-get install -y wget unzip curl

# Android SDK root
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# SDKダウンロード
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    cd /opt && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip commandlinetools-linux-11076708_latest.zip && \
    mv cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest

# SDK components install
RUN yes | sdkmanager --licenses && \
    sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0"


WORKDIR /workspace

COPY . .

RUN chmod +x gradlew

CMD ["./gradlew", "assembleDebug"]
```


## Androidビルド用のcomposeにする
androidのapk出力用のコンテナを用意する。
appはもういらないが、一旦残しておく。(確認でき次第phase5で消す)
compose.yaml
```yaml
# compose.yaml

name: JavaNumberGuess

services:
  # Gradleの初回設定用
  gradle-init:
    container_name: java_numver_guess_init

    build:
      context: .
      dockerfile: Dockerfile
      target: init

    working_dir: /workspace
    volumes:
      - .:/workspace

    stdin_open: true
    tty: true

  # ビルド/実行をする用
  app:
    container_name: java_numver_guess_build

    build:
      context: .
      dockerfile: Dockerfile
      target: runtime

    working_dir: /workspace

  # アンドロイドのapkを作る用
  android:
    build:
      context: .
      dockerfile: Dockerfile
      target: android
    volumes:
      - .:/workspace

```

# settings.gradle.kts
pluginManagementとかを増やさなきゃだったので増やした。
```kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JavaNumberGuess"
include("app")
```

# build.gradle.kts
コンパイルのバージョンとかを増やした。
kotlinとjavaでそろえなきゃいけないらしい。
最新は21とかだが、17が安定？
```
plugins {
    id("com.android.application") version "8.3.2"
    id("org.jetbrains.kotlin.android") version "2.0.21"
}

android {
    namespace = "com.javanumberguess"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.javanumberguess"
        minSdk = 24
        targetSdk = 34
    }

    compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
```


## gradle.properties
何かわからないが編集が必要
```
android.useAndroidX=true
android.enableJetifier=true
```

## local.properties
何かわからないが編集が必要？
ほんとにこれだけ
```
local.properties
```

## App.kt
アンドロイドのアプリとなるため、main()ではなくなった。
このようになった
```
// app/src/main/kotlin/App.kt
package com.javanumberguess

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("APP", "Hello Android from Logcat")

        val message = MessageProvider.getMessage()
        Log.d("APP", message)
    }
}
```

## 実行
```
docker compose run --rm android
```
これをする。`BUILD SUCCESSFUL`なら
```
app/build/outputs/apk/debug/app-debug.apk
```
にapkが出力される