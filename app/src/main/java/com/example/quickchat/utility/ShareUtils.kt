package com.example.quickchat.utility


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.quickchat.mainModule.models.PostModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareUtils {

    /**
     * Shares a post with its image, username, and description
     * @param context Context
     * @param post The post to share
     */
    suspend fun sharePost(context: Context, post: PostModel) {
        try {
            // Create share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"

                // Add text components (username and description)
                val shareText = buildShareText(post)
                putExtra(Intent.EXTRA_TEXT, shareText)

                // Handle image if available
                if (!post.imageUrl.isNullOrEmpty()) {
                    val imageUri = getImageUriForSharing(context, post.imageUrl!!)
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            // Start activity to share
            context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
        } catch (e: Exception) {
            Log.e("ShareUtils", "Error sharing post: ${e.message}")
        }
    }

    /**
     * Builds the text content for sharing
     */
    private fun buildShareText(post: PostModel): String {
        val username = post.detailModel?.firstname ?: "User"
        val title = post.title ?: ""
        val description = post.description ?: ""

        return """
            From: $username
            ${if (title.isNotEmpty()) "$title\n" else ""}
            $description
            
            Shared via QuickChat App
        """.trimIndent()
    }

    /**
     * Downloads and processes the image for sharing
     */
    private suspend fun getImageUriForSharing(context: Context, imageUrl: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // Download image using Glide
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()

            // Create a file to store the image
            val imagesFolder = File(context.cacheDir, "shared_images")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs()
            }

            val imageFile = File(imagesFolder, "shared_post_${System.currentTimeMillis()}.jpg")

            // Save bitmap to file
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            // Get content URI using FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
        } catch (e: IOException) {
            Log.e("ShareUtils", "Error processing image for sharing: ${e.message}")
            null
        }
    }
}