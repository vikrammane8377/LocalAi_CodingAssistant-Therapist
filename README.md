# Local AI Coding Assistant & Therapist (AI Edge Gallery)

AI Edge Gallery is a collection of on-device AI demos and Android apps showcasing local LLMs and multimodal inference. It includes sample projects (ProgrammingAiLocal, Gallery Android), model assets, and a lightweight Python bot. Build with Gradle, run offline, and explore privacy-preserving AI on mobile.

## Projects
- `gallery/Android` — reference Android app and samples
- `ProgrammingAiLocal/` — Android app using local LLMs
- `Programming__AiLocal/` — alternate Android app variant
- `mistral_model/` — model assets/experiments
- `ProgrammingAiLocal/qwen25-therapy-bot/` — minimal Python demo

## Quick Start
1) Open in Android Studio: ProgrammingAiLocal/ or Programming__AiLocal/ (or gallery/Android/src/app)
2) Let Gradle sync, then build & run on a device/emulator

Command line:
./gradlew :ProgrammingAiLocal:app:assembleDebug
./gradlew :Programming__AiLocal:app:assembleDebug

License: Apache 2.0 (see LICENSE)
