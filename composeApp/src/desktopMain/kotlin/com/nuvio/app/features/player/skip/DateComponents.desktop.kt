package com.nuvio.app.features.player.skip

import java.util.Calendar

internal actual fun currentDateComponents(): DateComponents {
    val cal = Calendar.getInstance()
    return DateComponents(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
}
