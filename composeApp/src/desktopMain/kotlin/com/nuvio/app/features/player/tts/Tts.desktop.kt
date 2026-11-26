package com.nuvio.app.features.player.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object PiperModelManager {
    actual val presets: List<PiperPreset> = emptyList()
    actual val downloadProgress: StateFlow<Map<String, Float>> = MutableStateFlow(emptyMap())
    actual val isEspeakDownloading: StateFlow<Boolean> = MutableStateFlow(false)
    actual val espeakDownloadProgress: StateFlow<Float> = MutableStateFlow(0f)
    actual fun isModelInstalled(modelId: String): Boolean = false
    actual fun getInstalledModel(modelId: String): InstalledPiperModel? = null
    actual fun downloadPreset(preset: PiperPreset, onComplete: (InstalledPiperModel) -> Unit, onError: (String) -> Unit) = onError("Unsupported on Desktop")
    actual fun downloadCustomModel(id: String, name: String, onnxUrl: String, jsonUrl: String, onComplete: (InstalledPiperModel) -> Unit, onError: (String) -> Unit) = onError("Unsupported on Desktop")
    actual fun deleteModel(modelId: String) {}
    actual fun getEspeakDir(): String = ""
}

actual object PiperOnnxEngine {
    actual val isSupported: Boolean = false
    actual val isModelLoaded: Boolean = false
    actual val currentLoadedModelId: String? = null
    actual fun loadModel(model: InstalledPiperModel): Boolean = false
    actual fun generatePcm(text: String, speed: Float): FloatArray? = null
    actual fun release() {}
}

internal actual class SubtitleTtsPlayer actual constructor() {
    actual fun playPcm(samples: FloatArray, sampleRate: Int, volume: Float, tailMs: Int, onCompleted: (() -> Unit)?) { onCompleted?.invoke() }
    actual fun playMp3(mp3Bytes: ByteArray, volume: Float, speed: Float) {}
    actual fun setVolume(volume: Float) {}
    actual fun stop() {}
    actual fun release() {}
}

internal actual object SubtitleTtsStorage {
    private var cached = SubtitleTtsSettings()
    actual fun loadSettings(): SubtitleTtsSettings = cached
    actual fun saveSettings(settings: SubtitleTtsSettings) { cached = settings }
}

actual object SystemTtsEngine {
    actual val isInitialized: Boolean = false
    actual val availableEngines: StateFlow<List<SystemTtsEngineInfo>> = MutableStateFlow(emptyList())
    actual val availableVoices: StateFlow<List<SystemVoiceInfo>> = MutableStateFlow(emptyList())
    actual fun initialize(preferredEnginePackage: String?) {}
    actual fun setEngine(enginePackage: String) {}
    actual fun speak(text: String, voiceName: String?, pitch: Float, speed: Float, volume: Float, onCompleted: (() -> Unit)?) { onCompleted?.invoke() }
    actual fun stop() {}
    actual fun release() {}
    actual fun openSystemTtsSettings() {}
}


