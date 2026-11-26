package com.nuvio.app.features.updater

actual object AppUpdaterPlatform {
    actual val isSupported: Boolean = false
    actual val isDebugBuild: Boolean = false
    actual fun getSupportedAbis(): List<String> = listOf("x86_64")
    actual fun getIgnoredTag(): String? = null
    actual fun setIgnoredTag(tag: String?) {}
    actual fun getUpdateChannel(): String? = "stable"
    actual fun setUpdateChannel(channel: String) {}
    actual fun deleteDownloadedApk(path: String) {}
    actual suspend fun downloadApk(assetUrl: String, assetName: String, onProgress: (Long, Long?) -> Unit): Result<String> = Result.failure(UnsupportedOperationException())
    actual fun canRequestPackageInstalls(): Boolean = false
    actual fun openUnknownSourcesSettings() {}
    actual fun installDownloadedApk(path: String): Result<Unit> = Result.failure(UnsupportedOperationException())
}
