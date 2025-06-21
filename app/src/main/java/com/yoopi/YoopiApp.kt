package com.yoopi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp          // ← boots Hilt once for the whole app
class YoopiApp : Application()
