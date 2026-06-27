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