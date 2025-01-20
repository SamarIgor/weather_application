package com.example.weatherapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapplication.ui.theme.WeatherApplicationTheme
import com.example.weatherapplication.viewmodel.WeatherViewModel
import com.example.weatherapplication.data.SearchEvent
import java.text.SimpleDateFormat
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.LocationManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        // Initialize viewModel and fusedLocationClient
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set the content view using Jetpack compose
        setContent {
            WeatherApplicationTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController, startDestination = "weather", modifier = Modifier.padding(innerPadding)) {
                        composable("weather") {
                            WeatherScreen(
                                mainActivity = this@MainActivity,
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                mainActivity = this@MainActivity,
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        // Fetch the latest weather data when the app comes to the foreground
        viewModel.fetchWeatherData(viewModel.currentLocationName.value)
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
        // Save the current state or perform cleanup operations
        val sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("currentLocation", viewModel.currentLocationName.value)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        // Clear the permissions state
        val sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("locationPermissionGranted")
        editor.apply()
    }

    // Function to get last devices location
    fun getLastLocation(onGpsNotEnabled: () -> Unit, onLocationRetrieved: (Boolean) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isGpsEnabled) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("MainActivity", "User's location: lat=$lat, lon=$lon")
                    viewModel.fetchWeatherData(lat, lon)
                    onLocationRetrieved(true)
                } else {
                    onLocationRetrieved(false)
                }
            }
        } else {
            onGpsNotEnabled()
            onLocationRetrieved(false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation(
                    onGpsNotEnabled = {
                        // Handle the case where GPS is not enabled
                        Log.d("MainActivity", "GPS is not enabled")
                    },
                    onLocationRetrieved = { isLocationRetrieved ->
                        if (isLocationRetrieved) {
                            Log.d("MainActivity", "Location retrieved successfully")
                        } else {
                            Log.d("MainActivity", "Failed to retrieve location")
                        }
                    }
                )
            } else {
                // Handle the case where permissions are not granted
                Log.d("MainActivity", "Location permissions not granted")
            }
            return
        }
    }
}

@Composable
fun WeatherScreen(mainActivity: MainActivity, viewModel: WeatherViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val weatherData = viewModel.weatherData.observeAsState().value
    val forecastData = viewModel.forecastData.observeAsState().value
    val currentLocationName = viewModel.currentLocationName.collectAsState().value
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showGpsReminder by remember { mutableStateOf(false) }

    if (weatherData != null && forecastData != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.background), // Set the background image to the "background.jpeg" image
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(36.dp),
                            tint = Color.White)
                    }
                }
                Text(
                    text = "$currentLocationName",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${kotlin.math.floor(weatherData.main.temp).toInt()}°C",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherIcon(weatherData.weather[0].description)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${weatherData.weather[0].description}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.temperature),
                            contentDescription = "Real Feel Temperature Icon",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Real Feel: ${kotlin.math.floor(weatherData.main.feels_like).toInt()}°C",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.sunrise),
                            contentDescription = "Sunrise Icon",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Sunrise: ${formatTime(weatherData.sys.sunrise)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.sunset),
                            contentDescription = "Sunset Icon",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Sunset: ${formatTime(weatherData.sys.sunset)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.humidity),
                            contentDescription = "Humidity Icon",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Humidity: ${weatherData.main.humidity}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.wind),
                            contentDescription = "Wind Speed Icon",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Wind Speed: ${weatherData.wind.speed} m/s",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.pressure),
                            contentDescription = "Pressure Icon",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Pressure: ${weatherData.main.pressure}hPa",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Daily Forecast",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TemperatureGraph(forecastData.list.take(6))
                }
            }
        }

        GpsReminderSnackbar(showGpsReminder, onDismiss = { showGpsReminder = false })

        if (showSnackbar) {
            Snackbar(
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text(text = "Dismiss")
                    }
                }
            ) {
                Text(text = snackbarMessage)
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SettingsScreen(mainActivity: MainActivity, viewModel: WeatherViewModel, navController: NavController, modifier: Modifier = Modifier) {
    var textFieldValue by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showGpsReminder by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background), // Set the background image to the "background.jpeg" image
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { navController.navigate("weather") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(36.dp),
                        tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Search Location
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                    },
                    placeholder = { Text(text = "Enter location") },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        if (textFieldValue.isNotEmpty()) {
                            viewModel.onSearchEvent(SearchEvent.Search(textFieldValue))
                            textFieldValue = ""
                            if (viewModel.currentLocationName.value == "Location not found") {
                                snackbarMessage = "Location not found"
                                showSnackbar = true
                                coroutineScope.launch {
                                    delay(2000)
                                    showSnackbar = false
                                }
                            } else {
                                navController.navigate("weather")
                            }
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "Search Icon",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Get Current Location
            Button(
                onClick = {
                    mainActivity.getLastLocation(
                        onGpsNotEnabled = {
                            showGpsReminder = true
                        },
                        onLocationRetrieved = { isLocationRetrieved ->
                            if (isLocationRetrieved) {
                                navController.navigate("weather")
                            }
                        }
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.location),
                    contentDescription = "My Location Icon",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "My Location")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Set Default Location
            if (viewModel.currentLocationName.value.isNotEmpty()) {
                Button(
                    onClick = {
                        if (viewModel.currentLocationName.value == "My Location") {
                            snackbarMessage = "You cannot set your location as default"
                        } else if (viewModel.currentLocationName.value == "Location not found") {
                            snackbarMessage = "You cannot set location as default"
                        } else {
                            viewModel.updateDefaultLocation(viewModel.currentLocationName.value)
                            snackbarMessage = "Default location set to ${viewModel.currentLocationName.value}"
                        }
                        showSnackbar = true
                        coroutineScope.launch {
                            delay(2000)
                            showSnackbar = false
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.default_location),
                        contentDescription = "Set as Default Icon",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Set as Default")
                }
            }

            GpsReminderSnackbar(showGpsReminder, onDismiss = { showGpsReminder = false })

            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text(text = "Dismiss")
                        }
                    }
                ) {
                    Text(text = snackbarMessage)
                }
            }
        }
    }
}

@Composable
fun GpsReminderSnackbar(showGpsReminder: Boolean, onDismiss: () -> Unit) {
    if (showGpsReminder) {
        LaunchedEffect(key1 = showGpsReminder) {
            delay(5000)
            onDismiss()
        }
        Snackbar(
            action = {
                TextButton(onClick = onDismiss) {
                    Text(text = "Dismiss")
                }
            }
        ) {
            Text(text = "Please turn on your GPS to use this feature.")
        }
    }
}

private fun formatTime(time: Long): String {
    val formatter = SimpleDateFormat("HH:mm")
    return formatter.format(time * 1000)
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherApplicationTheme {
        val navController = rememberNavController()
        WeatherScreen(mainActivity = MainActivity(), viewModel = WeatherViewModel(), navController = navController)
    }
}
