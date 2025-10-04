package com.qrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : ComponentActivity() {

    // Launcher for QR scanning
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { scannedData ->
            if (scannedData.startsWith("http://") || scannedData.startsWith("https://")) {
                val browserIntent = Intent(Intent.ACTION_VIEW, scannedData.toUri())
                startActivity(browserIntent)
            }
        }
    }

    // Launcher for requesting camera permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startScanning()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                QRScannerScreen { checkCameraPermissionAndScan() }
            }
        }
    }

    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> startScanning()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanning() {
        val options = ScanOptions().apply {
            setPrompt("Scan a QR Code")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(CaptureAct::class.java)
        }
        barcodeLauncher.launch(options)
    }
}

// Composable UI for the QR Scanner
@Composable
fun QRScannerScreen(onScanClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onScanClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Scan QR Code")
        }
    }
}
