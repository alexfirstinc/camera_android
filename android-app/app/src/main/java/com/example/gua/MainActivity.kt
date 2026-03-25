package com.example.gua

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private var rotationSensor: Sensor? = null

    private var trueNorthCorrection = 0f

    private lateinit var baguaView: BaguaView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val genderSpinner: Spinner = findViewById(R.id.genderSpinner)
        val daySpinner: Spinner = findViewById(R.id.daySpinner)
        val monthSpinner: Spinner = findViewById(R.id.monthSpinner)
        val yearSpinner: Spinner = findViewById(R.id.yearSpinner)
        val calcButton: Button = findViewById(R.id.calcButton)
        baguaView = findViewById(R.id.baguaView)
        resultText = findViewById(R.id.resultText)

        genderSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Мужской", "Женский"))
        daySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (1..31).map { it.toString() })
        monthSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (1..12).map { it.toString() })
        yearSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (1940..2025).reversed().map { it.toString() })

        calcButton.setOnClickListener {
            val gender = genderSpinner.selectedItem.toString()
            val year = yearSpinner.selectedItem.toString().toInt()
            val gua = GuaCalculator.calculate(gender, year)
            resultText.text = "Ваше число Гуа: $gua"
            baguaView.setUserGua(gua)
        }

        requestLocationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        subscribeLocation()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rot = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rot, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rot, orientation)
        val magneticAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val normalized = (magneticAzimuth + trueNorthCorrection + 360f) % 360f

        baguaView.setHeading(normalized)
        title = "Азимут: ${normalized.roundToInt()}°"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onLocationChanged(location: Location) {
        val field = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        trueNorthCorrection = field.declination
    }

    @SuppressLint("MissingPermission")
    private fun subscribeLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, this)
    }

    private fun requestLocationPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
    }
}
