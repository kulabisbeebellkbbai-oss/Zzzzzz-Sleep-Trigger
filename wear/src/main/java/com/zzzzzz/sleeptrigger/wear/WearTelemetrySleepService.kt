package com.zzzzzz.sleeptrigger.wear

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class WearTelemetrySleepService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var store: WearPassiveTriggerStateStore
    private lateinit var classifier: WearTelemetrySleepClassifier
    private var latestHeartRate: Float? = null
    private var latestHeartRateAtMillis: Long = 0L
    private var offBody: Boolean? = null
    private var lastEvaluationAtMillis: Long = 0L

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SensorManager::class.java)
        store = WearPassiveTriggerStateStore(this)
        classifier = WearTelemetrySleepClassifier()
        startForeground(NOTIFICATION_ID, notification())
        registerSensors()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        store.writeTelemetryStatus("Telemetry running")
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        store.writeTelemetryStatus("Telemetry stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                classifier.addMotionSample(now, event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_HEART_RATE -> {
                if (event.values.isNotEmpty() && event.values[0] > 0f) {
                    latestHeartRate = event.values[0]
                    latestHeartRateAtMillis = now
                }
            }
            Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT -> {
                if (event.values.isNotEmpty()) {
                    offBody = event.values[0] <= 0f
                }
            }
        }
        if (now - lastEvaluationAtMillis >= EVALUATION_INTERVAL_MILLIS) {
            lastEvaluationAtMillis = now
            evaluate(now)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun evaluate(now: Long) {
        val latestHeartRateIsRecent = now - latestHeartRateAtMillis <= HEART_RATE_RECENCY_MILLIS
        val wornEvidence = offBody != true && (offBody == false || latestHeartRateIsRecent)
        val result = classifier.evaluate(
            nowMillis = now,
            latestHeartRate = latestHeartRate?.takeIf { latestHeartRateIsRecent },
            wornEvidence = wornEvidence
        )
        store.writeTelemetryStatus(
            "Telemetry ${result.state}: confidence=${"%.2f".format(result.confidence)} " +
                "quiet=${result.quietMinutes}m hr=${latestHeartRate?.toInt() ?: 0}"
        )
        if (result.state != WearTelemetrySleepClassifier.State.PROBABLE_SLEEP) return
        if (!store.shouldSendTelemetrySleep(now)) {
            store.appendHistory("telemetryDebounced", now)
            return
        }
        store.appendHistory(
            kind = "telemetryTrigger",
            atMillis = now,
            values = mapOf(
                "confidence" to result.confidence.toString(),
                "quietMinutes" to result.quietMinutes.toString(),
                "heartRate" to (latestHeartRate?.toString() ?: ""),
                "offBody" to (offBody?.toString() ?: "")
            )
        )
        WearHealthTriggerSender(this).send(
            triggerType = "ASLEEP_DETECTED",
            confidence = result.confidence,
            source = "WEAR_TELEMETRY",
            surface = "watch-telemetry",
            metadata = mapOf(
                "telemetryState" to result.state.name,
                "quietMinutes" to result.quietMinutes.toString(),
                "heartRate" to (latestHeartRate?.toString() ?: ""),
                "offBody" to (offBody?.toString() ?: "")
            )
        )
    }

    private fun registerSensors() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            store.writeTelemetryStatus("Telemetry unavailable: no accelerometer")
            stopSelf()
            return
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)?.let { heartRate ->
                sensorManager.registerListener(this, heartRate, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT)?.let { offBodySensor ->
            sensorManager.registerListener(this, offBodySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun notification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Sleep telemetry",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Zzzzzz telemetry")
            .setContentText("Watching for sleep telemetry")
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.zzzzzz.sleeptrigger.wear.START_TELEMETRY"
        private const val CHANNEL_ID = "sleep-telemetry"
        private const val NOTIFICATION_ID = 42
        private const val EVALUATION_INTERVAL_MILLIS = 60_000L
        private const val HEART_RATE_RECENCY_MILLIS = 15 * 60 * 1000L
    }
}

class WearTelemetrySleepClassifier {
    private val buckets = ArrayDeque<MotionBucket>()
    private var currentBucket: MotionBucket? = null

    fun addMotionSample(nowMillis: Long, x: Float, y: Float, z: Float) {
        val bucketStart = nowMillis - (nowMillis % BUCKET_MILLIS)
        val bucket = currentBucket?.takeIf { it.startedAtMillis == bucketStart }
            ?: MotionBucket(bucketStart).also { newBucket ->
                currentBucket?.let { buckets.addLast(it) }
                currentBucket = newBucket
                while (buckets.size > MAX_BUCKETS) buckets.removeFirst()
            }
        val magnitude = sqrt(x * x + y * y + z * z)
        bucket.add(magnitude)
    }

    fun evaluate(nowMillis: Long, latestHeartRate: Float?, wornEvidence: Boolean): Result {
        val recentBuckets = (buckets + listOfNotNull(currentBucket))
            .filter { nowMillis - it.startedAtMillis <= WINDOW_MILLIS }
        val quietBuckets = recentBuckets.count { it.isQuiet() }
        val quietMinutes = quietBuckets
        val heartRateLooksAsleep = latestHeartRate != null && latestHeartRate in 38f..90f
        val probable = wornEvidence &&
            quietMinutes >= REQUIRED_QUIET_MINUTES &&
            heartRateLooksAsleep
        val confidence = when {
            probable -> minOf(0.95f, 0.72f + ((quietMinutes - REQUIRED_QUIET_MINUTES) * 0.02f))
            wornEvidence && quietMinutes >= REQUIRED_QUIET_MINUTES -> 0.62f
            else -> 0.2f + minOf(0.35f, quietMinutes * 0.02f)
        }
        return Result(
            state = if (probable) State.PROBABLE_SLEEP else State.AWAKE_OR_UNKNOWN,
            confidence = confidence,
            quietMinutes = quietMinutes
        )
    }

    data class Result(
        val state: State,
        val confidence: Float,
        val quietMinutes: Int
    )

    enum class State {
        AWAKE_OR_UNKNOWN,
        PROBABLE_SLEEP
    }

    private class MotionBucket(val startedAtMillis: Long) {
        private var count = 0
        private var mean = 0f
        private var maxDelta = 0f

        fun add(magnitude: Float) {
            if (count > 0) {
                val delta = kotlin.math.abs(magnitude - mean)
                if (delta > maxDelta) maxDelta = delta
            }
            count += 1
            mean += (magnitude - mean) / count
        }

        fun isQuiet(): Boolean = count >= MIN_SAMPLES_PER_BUCKET && maxDelta <= QUIET_MAX_DELTA
    }

    companion object {
        private const val BUCKET_MILLIS = 60_000L
        private const val WINDOW_MILLIS = 20 * 60 * 1000L
        private const val REQUIRED_QUIET_MINUTES = 15
        private const val MAX_BUCKETS = 25
        private const val MIN_SAMPLES_PER_BUCKET = 4
        private const val QUIET_MAX_DELTA = 0.35f
    }
}

class WearStartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                WearPassiveMonitoringRegistrar(context).register()
                val serviceIntent = Intent(context, WearTelemetrySleepService::class.java)
                    .setAction(WearTelemetrySleepService.ACTION_START)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
