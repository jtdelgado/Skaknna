package com.skaknna.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.skaknna.BuildConfig
import com.skaknna.vision.ChessVisionManager
import com.skaknna.vision.VisionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onValidationComplete: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionRequested by remember { mutableStateOf(false) }

    var isFromGallery by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            permissionRequested = true
            if (!granted) {
                onNavigateBack() // Go back if permission denied
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera setup
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedPhotoFile by remember { mutableStateOf<File?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            capturedImageUri = uri
            capturedPhotoFile = null
            isFromGallery = true
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // AI Vision Setup
    val visionManager = remember { ChessVisionManager(BuildConfig.GEMINI_API_KEY) }
    var visionState by remember { mutableStateOf<VisionState>(VisionState.Idle) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    // Auto-reset captured image after 5 seconds IF not analyzing
    LaunchedEffect(capturedImageUri, visionState) {
        if (capturedImageUri != null && visionState == VisionState.Idle) {
            delay(5000)
            capturedImageUri = null
            capturedPhotoFile = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Tablero", color = com.skaknna.ui.theme.GoldenYellow, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = com.skaknna.ui.theme.GoldenYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.skaknna.ui.theme.TransparentColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasCameraPermission) {
                if (visionState is VisionState.Analyzing) {
                    CircularProgressIndicator(color = com.skaknna.ui.theme.GoldenYellow)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analizando pieza por pieza con IA...", color = com.skaknna.ui.theme.WarmWhite, fontWeight = FontWeight.Bold)
                } else if (capturedImageUri != null) {
                    // Muestra la imagen capturada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .aspectRatio(1f)
                            .border(2.dp, com.skaknna.ui.theme.GoldenYellow)
                    ) {
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Foto capturada",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    if (visionState is VisionState.Error) {
                        Text(
                            text = (visionState as VisionState.Error).message, 
                            color = MaterialTheme.colorScheme.error, 
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { 
                                capturedImageUri = null 
                                capturedPhotoFile = null
                                isFromGallery = false
                                visionState = VisionState.Idle 
                            },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = "Reintentar") },
                            text = { Text("Reintentar", fontWeight = FontWeight.Bold) },
                            containerColor = com.skaknna.ui.theme.WoodDark,
                            contentColor = com.skaknna.ui.theme.GoldenYellow
                        )

                        ExtendedFloatingActionButton(
                            onClick = { 
                                if (capturedPhotoFile != null || isFromGallery) {
                                    visionState = VisionState.Analyzing
                                    coroutineScope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            if (isFromGallery) {
                                                val inputStream = context.contentResolver.openInputStream(capturedImageUri!!)
                                                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                                                inputStream?.close()
                                                visionManager.analyzeBoard(originalBitmap)
                                            } else {
                                                val originalBitmap = BitmapFactory.decodeFile(capturedPhotoFile!!.absolutePath)
                                                // Recortar la imagen en un cuadrado basándose en el centro, 
                                                // coincidiendo así exactamente con el encuadre 1:1 que vio el usuario
                                                val width = originalBitmap.width
                                                val height = originalBitmap.height
                                                val size = minOf(width, height)
                                                val x = (width - size) / 2
                                                val y = (height - size) / 2
                                                
                                                val croppedBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size)
                                                visionManager.analyzeBoard(croppedBitmap)
                                            }
                                        }
                                        visionState = result
                                        
                                        if (result is VisionState.Success) {
                                            onValidationComplete(result.fen)
                                        }
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.Check, contentDescription = "Analizar") },
                            text = { Text("Analizar Imagen", fontWeight = FontWeight.Bold) },
                            containerColor = com.skaknna.ui.theme.WoodMedium,
                            contentColor = com.skaknna.ui.theme.WarmWhite
                        )
                    }
                } else {
                    // Interfaz de Cámara
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .aspectRatio(1f)
                            .border(2.dp, com.skaknna.ui.theme.GoldenYellow)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                imageCapture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (exc: Exception) {
                                    Log.e("CameraPreview", "Fallo al inicializar la cámara", exc)
                                }

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Grid 8x8 Overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val squareSize = size.width / 8f
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            // Dibujar las celdas del ajedrez (cuadros alternos) muy sutiles
                            for (row in 0 until 8) {
                                for (col in 0 until 8) {
                                    if ((row + col) % 2 == 1) {
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.05f),
                                            topLeft = Offset(col * squareSize, row * squareSize),
                                            size = Size(squareSize, squareSize)
                                        )
                                    }
                                }
                            }

                            // Dibujar las líneas del grid muy finas y blancas
                            for (i in 1..7) {
                                // Líneas verticales
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(i * squareSize, 0f),
                                    end = Offset(i * squareSize, canvasHeight),
                                    strokeWidth = 1.dp.toPx()
                                )
                                // Líneas horizontales
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(0f, i * squareSize),
                                    end = Offset(canvasWidth, i * squareSize),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                val photoFile = File(context.cacheDir, "chessboard_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                imageCapture?.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            capturedImageUri = Uri.fromFile(photoFile)
                                            capturedPhotoFile = photoFile
                                            isFromGallery = false
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("CameraPreview", "Error capturando la foto: ${exception.message}", exception)
                                        }
                                    }
                                )
                            },
                            icon = { Icon(Icons.Default.Camera, contentDescription = "Capturar") },
                            text = { Text("Capturar", fontWeight = FontWeight.Bold) },
                            containerColor = com.skaknna.ui.theme.WoodMedium,
                            contentColor = com.skaknna.ui.theme.GoldenYellow,
                            modifier = Modifier
                                .weight(1f)
                                .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                        )

                        FilledIconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(56.dp)
                                .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = com.skaknna.ui.theme.WoodDark, contentColor = com.skaknna.ui.theme.GoldenYellow)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería")
                        }
                    }
                }
            } else if (permissionRequested) {
                // Should not reach here usually since ungranted returns back
                Text("Se requiere permiso de cámara para escanear el tablero.")
            } else {
                CircularProgressIndicator(color = com.skaknna.ui.theme.GoldenYellow)
            }
        }
    }
}
