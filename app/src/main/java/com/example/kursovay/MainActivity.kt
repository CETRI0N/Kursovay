@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.kursovay

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kursovay.ui.them.CameraX
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    // запись объекта с камеры
    private var recording: Recording? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermiss()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0) // запрос разрешений, если их нет
        }

        setContent {
            CameraX {
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState() // состояние нижнего листа
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE) // варианты использования: захват изображения или захват видео
                    }
                }
                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmaps.collectAsState()

                BottomSheetScaffold(scaffoldState = scaffoldState, sheetPeekHeight = 0.dp, sheetContent = {// нижний лист; содержимое листа,
                        PhotoBottomSheetContent(bitmaps = bitmaps, modifier = Modifier.fillMaxWidth())
                    })
                { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) // блок для нижнего листа
                    {
                        CameraPreview(controller = controller, modifier = Modifier.fillMaxSize()) // предварительный просмотр

                        IconButton(onClick = {
                                controller.cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    }
                                    else CameraSelector.DEFAULT_BACK_CAMERA
                            },
                            modifier = Modifier.offset(16.dp, 16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Переключить камеру")
                        }

                        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) // кнопка сделеть фото
                        {
                            IconButton(onClick = { // галерея
                                    scope.launch {  // открытие нижнего листа
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                })
                            {
                                Icon(imageVector = Icons.Default.Photo, contentDescription = "Галерея")
                            }
                            IconButton(onClick = { // фото
                                    takePhoto(controller = controller, onPhotoTaken = viewModel::onTakePhoto)
                                })
                            {
                                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Сделать фото")
                            }
                            IconButton(onClick = { // видео
                                    recordVideo(controller = controller,)
                                })
                            {
                                Icon(imageVector = Icons.Default.Videocam, contentDescription = "Сделать видео")
                            }
                        }
                    }
                }
            }
        }
    }


    private fun takePhoto(controller: LifecycleCameraController, onPhotoTaken: (Bitmap) -> Unit) { // делаем фото
        if(!hasPermiss()){
            return
        }
        controller.takePicture(ContextCompat.getMainExecutor(applicationContext), object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image) //события на успешно сделанное фото

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                        postScale(-1f, 1f)
                    }
                    val rotatedBitmap = Bitmap.createBitmap(image.toBitmap(), 0, 0, image.width, image.height, matrix, true)

                    onPhotoTaken(rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Фото не сделано: ", exception)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun recordVideo(controller: LifecycleCameraController) {
        if (recording != null) {
            recording?.stop()
            recording = null
            return
        }
        if(!hasPermiss()){
            return
        }
        val outputFile = File(filesDir, "Филин.mp4")
        recording = controller.startRecording( //параметры вывода файла. Конструктор берет файл, который нужно сохранить
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(applicationContext),

        ) { event ->
            when (event) {// событие когда видос заканчивается
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        recording?.close()
                        recording = null

                        Toast.makeText(applicationContext, "Видео не записано", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(applicationContext, "Молодец, справился)", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }
    private fun hasPermiss(): Boolean { // проверка, есть ли разрешения
        return CAMERAX_PERMISSIONS.all { ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED // проверка отдального разрешения
        }
    }

    companion object { // запрос на разрешения
        private val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,)
    }
}