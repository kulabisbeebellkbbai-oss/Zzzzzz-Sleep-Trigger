package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.PassiveListenerConfig
import com.google.common.util.concurrent.MoreExecutors

class WearPassiveMonitoringRegistrar(context: Context) {
    private val appContext = context.applicationContext
    private val store = WearPassiveTriggerStateStore(appContext)

    fun register(onComplete: () -> Unit = {}) {
        val passiveClient = HealthServices.getClient(appContext).passiveMonitoringClient
        val config = PassiveListenerConfig.builder()
            .setShouldUserActivityInfoBeRequested(true)
            .build()
        val future = passiveClient.setPassiveListenerServiceAsync(
            WearPassiveTriggerService::class.java,
            config
        )
        future.addListener(
            {
                try {
                    future.get()
                    store.writeRegistrationStatus("Passive monitoring registered")
                    Log.i(TAG, "Health Services passive monitoring registered")
                } catch (exception: Exception) {
                    store.writeRegistrationStatus("Passive registration failed: ${exception.message}")
                    Log.w(TAG, "Health Services passive monitoring registration failed", exception)
                } finally {
                    onComplete()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun readStatus(): String = store.readRegistrationStatus()

    private companion object {
        const val TAG = "WearPassiveRegistrar"
    }
}
