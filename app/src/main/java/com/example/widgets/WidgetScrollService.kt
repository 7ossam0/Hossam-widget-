package com.example.widgets

import android.content.Intent
import android.widget.RemoteViewsService

class WidgetScrollService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetViewsFactory(this.applicationContext, intent)
    }
}
