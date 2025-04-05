package com.example.quickchat.communityModule.ui.activity

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
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.constants.Constant
import com.example.quickchat.databinding.ActivityCreateCommunityBinding
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class CreateCommunity : BaseActivity() {

    private lateinit var binding: ActivityCreateCommunityBinding
    private val communityViewModel: CommunityViewModel by viewModels()
    private val mainViewModel: PostViewModel by viewModels()
    private var filePath: Uri? = null


    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        const val STORAGE_PERMISSION_CODE = 101
        const val PICK_IMAGE_REQUEST = 22
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_community)
        preferenceManager = PreferenceManager(this)

        with(binding) {
            createProfileButton.setOnClickListener {
                validateCommunity()
                back.setOnClickListener{
                    onBackPressedDispatcher.onBackPressed()
                }


            }

            profileImage.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPermission(Manifest.permission.READ_MEDIA_IMAGES, STORAGE_PERMISSION_CODE)
                } else {
                    checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
                }
            }
        }
    }

    private fun validateCommunity() {
        with(binding) {


            val communityName = nameInput.text.toString().trim()
            val communityDescription = descriptionInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val mobile = mobileInput.text.toString().trim()
            val address = addressInput.text.toString().trim()

            if (communityName.isEmpty()) {
                nameInput.error = "Please enter community name"
                commonUtil.showToast("Please enter community name")
                return
            }
            if (communityDescription.isEmpty()) {
                descriptionInput.error = "Please enter community description"
                commonUtil.showToast("Please enter community description")
                return
            }
            if (email.isEmpty()) {
                emailInput.error = "Please enter email"
                commonUtil.showToast("Please enter email")
                return
            }
            if (mobile.isEmpty() || mobile.length != 10) {
                mobileInput.error = "Please enter a valid mobile number"
                commonUtil.showToast("Please enter a valid mobile number")
                return
            }
            if (address.isEmpty()) {
                addressInput.error = "Please enter address"
                commonUtil.showToast("Please enter address")
                return
            }

            val community = CommunityModels(
                communityName = communityName,
                communityDescription = communityDescription,
                email = email,
                mobileNumber = mobile,
                address = address,
                userId = preferenceManager.userId
            )


            val file = filePath?.let { uriToFile(it) }
            val apiKey = "6d207e02198a847aa98d0a2a901485a5"

            if (file != null) {
                mainViewModel.uploadImage(file, apiKey).observe(this@CreateCommunity) { state ->

                    Log.d("TAG", "validateCommunityss: $state")
                    when (state) {
                        is UiState.Loading -> {
                            Toast.makeText(this@CreateCommunity, "Image is Uploading...", Toast.LENGTH_SHORT).show()
                        }

                        is UiState.Success -> {
                            val response = state.data
                            community.imageUrl = response.image?.url

                            Log.d("UserIDDD", "validateCommunity: ${preferenceManager.userId}")

                            preferenceManager.userId?.let {
                                communityViewModel.addCommunity(it, community, Constant.ADMIN)
                                    .observe(this@CreateCommunity) { it2 ->
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

                        is UiState.Failure -> {
                            Toast.makeText(this@CreateCommunity, "Upload Failed: ${state.error}", Toast.LENGTH_SHORT).show()
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
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data

            Log.d("FilePathss", filePath.toString())

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.scroll.visibility = View.GONE
                binding.cropImageView.visibility = View.VISIBLE
                binding.titleText.text = "Crop"
                binding.rotate.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
                binding.cropImageView.setImageUriAsync(filePath)

                binding.rotate.setOnClickListener {
                    binding.cropImageView.rotateImage(90)
                }
                binding.done.setOnClickListener {
                    binding.scroll.visibility = View.VISIBLE
                    binding.cropImageView.visibility = View.GONE
                    binding.rotate.visibility = View.GONE
                    binding.done.visibility = View.GONE
                    binding.titleText.text = "Profile"
                    val cropped: Bitmap? = binding.cropImageView.croppedImage
                    filePath = cropped?.let { getUriFromBitmap(it) }
                    binding.profileImage.setImageBitmap(cropped)
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
        if (requestCode == STORAGE_PERMISSION_CODE) {
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
