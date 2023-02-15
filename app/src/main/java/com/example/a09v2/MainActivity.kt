package com.example.a09v2

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.a09v2.databinding.ActivityMainBinding
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        mediaController.setMediaPlayer(binding.videoView)
        binding.videoView.setMediaController(mediaController)


        // En clicar el botó, s'executa el següent codi
        binding.button.setOnClickListener {
            createVideoFile()

            // Obtenim l'URI de la imatge que es crearà
            val videoUri: Uri = FileProvider.getUriForFile(
                this, "com.example.a09v2.fileprovider", file
            )

            // Creem l'intent per obrir la càmera i li afegim l'URI de la imatge que es crearà
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            }

            // Iniciem l'activitat de la càmera i esperem el resultat
            startForResult.launch(intent)
        }
    }

    // Funció que s'executa després d'obtenir el resultat de l'activitat de la càmera
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Si l'activitat de la càmera va tornar una imatge, la mostrem
                /*
                result.data?.let { intent ->
                    intent.extras?.get("data")?.let {
                        // Si l'activitat de la càmera no va tornar una imatge, mostrem l'imatge que hem creat abans
                 */
                binding.videoView.setVideoURI(
                    insertVideoIntoMediaStore(file)
                )
                binding.videoView.start();
                Toast.makeText(this@MainActivity, "Video saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Something went wrong!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    // Funció que crea un fitxer temporal per guardar la imatge que es crearà
    private fun createVideoFile() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)

        file = File.createTempFile("eduardomorell_${Date().time}", ".mp4", dir)
    }

    private fun insertVideoIntoMediaStore(videoFile: File): Uri {
        // Crea un objecte ContentValues i afegeix valors per a la imatge que es vol inserir al MediaStore
        val values = ContentValues().apply {
            put(
                MediaStore.Video.Media.DISPLAY_NAME, "eduardomorell_${Date().time}.mp4"
            ) // Nom del fitxer
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4") // Tipus MIME
            put(
                MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES
            ) // Ruta on es guardarà la imatge
        }

        // Obté el ContentResolver de l'aplicació
        val resolver = applicationContext.contentResolver

        // Insereix la imatge al MediaStore i obté la Uri associada
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)!!
        //val uri = file.toUri()
        // Obté un OutputStream per escriure la imatge al MediaStore
        resolver.openOutputStream(uri)?.use { outputStream ->
            videoFile.inputStream().copyTo(outputStream) // Copia la imatge a l'OutputStream
        }

        // Retorna la Uri
        return uri
    }
}

