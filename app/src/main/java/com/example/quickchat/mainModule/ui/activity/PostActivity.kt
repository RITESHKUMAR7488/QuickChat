package com.example.quickchat.mainModule.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.activity.CreateCommunity
import com.example.quickchat.communityModule.ui.activity.CreateCommunity.Companion
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import com.example.quickchat.databinding.ActivityPostBinding
import com.example.quickchat.mainModule.models.DetailModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class PostActivity : BaseActivity() {

    private lateinit var binding: ActivityPostBinding
    private val postViewModel: PostViewModel by viewModels()
    private var communityId: String? = null
    private var filePath: Uri? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        private const val PICK_IMAGE_REQUEST = 22
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post)
        preferenceManager = PreferenceManager(this)
        communityId = intent.getStringExtra("COMMUNITY_ID")

        if (communityId.isNullOrEmpty()) {
            commonUtil.showToast("Invalid community selected.")
            finish()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        with(binding) {
            btnPost.setOnClickListener { validateAndCreatePost() }
            postImage.setOnClickListener { checkAndRequestPermissions() }
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        checkPermission(permission, STORAGE_PERMISSION_CODE)
    }

    private fun validateAndCreatePost() {
        with(binding) {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Please enter a post title"
                commonUtil.showToast("Please enter a post title")
                return
            }

            if (description.isEmpty()) {
                etDescription.error = "Please enter a post description"
                commonUtil.showToast("Please enter a post description")
                return
            }

            val detailModel = DetailModel(
                firstname = preferenceManager.userModel?.firstName,
                lastName = preferenceManager.userModel?.lastName,
                email = preferenceManager.userModel?.email
            )

            val post = PostModel(
                title = title,
                description = description,
                userId = preferenceManager.userId,
                detailModel = detailModel,
                communityId = communityId
            )

            val file = filePath?.let { uriToFile(it) }
            val apiKey = "6d207e02198a847aa98d0a2a901485a5"


            if (file != null) {
                postViewModel.uploadImage(file, apiKey).observe(this@PostActivity) { state ->
                    when (state) {
                        is UiState.Loading -> {
                            commonUtil.showToast("Image is Uploading...")
                        }

                        is UiState.Success -> {
                            val response = state.data
                            post.imageUrl = response.image?.url

                            preferenceManager.userId?.let { userId ->
                                communityId?.let { communityId ->
                                    postViewModel.addPost(communityId, post)
                                        .observe(this@PostActivity) { it2 ->
                                        when (it2) {
                                            is UiState.Loading -> {
                                                Log.d("TAG", "Creating community: Loading...")
                                            }

                                            is UiState.Success -> {
                                                commonUtil.showToast("Community created successfully")

                                            }

                                            is UiState.Failure -> {
                                                commonUtil.showToast(it2.error)
                                            }

                                            else -> {
                                                commonUtil.showToast("Something went wrong")
                                            }
                                        }
                                    }
                                }
                            }


                        }
                        is UiState.Failure -> {
                            Toast.makeText(this@PostActivity, "Upload Failed: ${state.error}", Toast.LENGTH_SHORT).show()
                        }

                    }
                }


            }
        }


    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."),
            CreateCommunity.PICK_IMAGE_REQUEST
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == com.example.quickchat.communityModule.ui.activity.CreateCommunity.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data

            Log.d("FilePathss", filePath.toString())

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.mainLayout.visibility = View.GONE
                binding.cropImageView.visibility = View.VISIBLE
                binding.titleText.text = "Crop"
                binding.rotate.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
                binding.cropImageView.setImageUriAsync(filePath)

                binding.rotate.setOnClickListener {
                    binding.cropImageView.rotateImage(90)
                }
                binding.done.setOnClickListener {
                    binding.mainLayout.visibility=View.VISIBLE
                    binding.cropImageView.visibility = View.GONE
                    binding.rotate.visibility = View.GONE
                    binding.done.visibility = View.GONE
                    binding.titleText.text = "Profile"
                    val cropped: Bitmap? = binding.cropImageView.croppedImage
                    filePath = cropped?.let { getUriFromBitmap(it) }
                    binding.postImage.setImageBitmap(cropped)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun getUriFromBitmap(bitmap: Bitmap): Uri {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, ByteArrayOutputStream())
        val path = MediaStore.Images.Media.insertImage(
            this.contentResolver,
            bitmap, System.currentTimeMillis().toString(), null
        )
        return Uri.parse(path)
    }
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            selectImage()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == com.example.quickchat.communityModule.ui.activity.CreateCommunity.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                commonUtil.showToast("Storage Permission Denied")
            }
        }
    }
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "community_image.jpg")
        file.outputStream().use { outputStream -> inputStream?.copyTo(outputStream) }
        return file
    }





}