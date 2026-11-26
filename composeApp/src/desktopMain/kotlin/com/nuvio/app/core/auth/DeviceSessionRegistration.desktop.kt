package com.nuvio.app.core.auth

internal actual fun currentDeviceClientMetadata(): DeviceClientMetadata =
    DeviceClientMetadata(
        deviceName = System.getenv("COMPUTERNAME") ?: "Windows PC",
        platform = "windows"
    )
