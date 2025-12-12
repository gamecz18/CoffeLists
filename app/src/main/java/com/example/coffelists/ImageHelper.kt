package cz.g18.coffeelists

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageHelper {

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "photos")
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }

            val fileName = "coffee_${UUID.randomUUID()}.jpg"
            val destinationFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getImageUri(imagePath: String?): Uri? {
        return if (imagePath != null && File(imagePath).exists()) {
            Uri.fromFile(File(imagePath))
        } else {
            null
        }
    }
}