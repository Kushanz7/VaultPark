package com.kushan.vaultpark.ui.components

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.view.PreviewView
import com.kushan.vaultpark.util.BarcodeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onQRCodeDetected: (String, Rect) -> Unit,
    isFlashEnabled: Boolean,
    onFlashToggle: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    DisposableEffect(lifecycleOwner) {
        scope.launch {
            try {
                val provider = withContext(Dispatchers.Default) {
                    ProcessCameraProvider.getInstance(context).get()
                }
                
                previewView?.let { preview ->
                    provider.unbindAll()
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    val previewUseCase = Preview.Builder().build().also {
                        it.setSurfaceProvider(preview.surfaceProvider)
                    }
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                BarcodeAnalyzer { qrCode, rect ->
                                    onQRCodeDetected(qrCode, rect)
                                }
                            )
                        }
                    
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        previewUseCase,
                        imageAnalysis
                    )
                    
                    try {
                        if (isFlashEnabled && camera.cameraInfo.hasFlashUnit()) {
                            camera.cameraControl.enableTorch(true)
                        } else {
                            camera.cameraControl.enableTorch(false)
                        }
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Error toggling flash", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error initializing camera", e)
            }
        }
        
        onDispose {
            // Cleanup if needed
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Scanning frame overlay
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .border(
                    width = 2.dp,
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(40.dp)
        )
        
        // Flash button
        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isFlashEnabled) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Toggle Flash",
                tint = Color.White
            )
        }
    }
}
