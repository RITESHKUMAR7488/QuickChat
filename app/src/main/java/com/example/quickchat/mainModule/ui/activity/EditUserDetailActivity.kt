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
    import com.bumptech.glide.Glide
    import com.example.quickchat.R
    import com.example.quickchat.databinding.ActivityEditUserDetailBinding
    import com.example.quickchat.mainModule.viewmodels.PostViewModel
    import com.example.quickchat.onboardingModule.models.UserModel
    import com.example.quickchat.utility.BaseActivity
    import com.example.quickchat.utility.PreferenceManager
    import com.example.quickchat.utility.UiState
    import dagger.hilt.android.AndroidEntryPoint
    import java.io.ByteArrayOutputStream
    import java.io.File
    import java.io.IOException

    @AndroidEntryPoint
    class EditUserDetailActivity : BaseActivity() {
        private lateinit var binding: ActivityEditUserDetailBinding
        private val postViewModel: PostViewModel by viewModels()
        private var filePath: Uri? = null
        lateinit var userId: String
        lateinit var model: UserModel

        companion object {
            private const val CAMERA_PERMISSION_CODE = 100
            const val STORAGE_PERMISSION_CODE = 101
            const val PICK_IMAGE_REQUEST = 22
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_user_detail)
            with(binding) {
                 preferenceManager = PreferenceManager(this@EditUserDetailActivity)
                userId = preferenceManager.userModel?.uid.toString()
                back.setOnClickListener{
                    onBackPressedDispatcher.onBackPressed()
                }


                binding.profileImage.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        checkPermission(Manifest.permission.READ_MEDIA_IMAGES, STORAGE_PERMISSION_CODE)
                    } else {
                        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
                    }
                }
            }
        }
        private fun fetchUserData() {
            model= preferenceManager.userModel!!
            val name = preferenceManager.userModel?.firstName
            val email = preferenceManager.userModel?.email
            val address = preferenceManager.userModel?.address
            val mobile = preferenceManager.userModel?.mobileNumber
            val profileImageUrl = preferenceManager.userModel?.imageUrl
            binding.nameInput.setText(name)
            binding.emailInput.setText(email)
            binding.addressInput.setText(address)
            binding.mobileInput.setText(mobile)

            profileImageUrl?.let { url ->
                loadProfilePicture(url)
            }

        }

        private fun updateUSer() {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
    //        val password = binding.passwordInput.text.toString()
            val address = binding.addressInput.text.toString()
            val mobile = binding.mobileInput.text.toString()

            if (name.isBlank()) {
                binding.nameInput.error = "Please enter name"
            } else if (email.isBlank()) {
                binding.emailInput.error = "Please enter email"
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailInput.error = "Invalid email"
    //        } else if (password.length < 8) {
    //            binding.passwordInput.error = "Password must be at least 8 characters"
    //        }
            } else if (mobile.length < 10) {
                binding.mobileInput.error = "Mobile number must be at least 10 characters"
            } else if (address.isBlank()) {
                binding.addressInput.error = "Please enter address"
            } else {
               model.apply {
                   this.firstName = name
                   this.email = email
    //               this.password = password
                   this.address = address
                   this.mobileNumber = mobile
               }

                val file = filePath?.let { uriToFile(it) }
                val apiKey = "6d207e02198a847aa98d0a2a901485a5"

                if (file != null) {
                    postViewModel.uploadImage(file, apiKey).observe(this) { state ->
                        Log.d("fohjfiwj", "State: $state")
                        when (state) {

                            is UiState.Loading -> {
                                Toast.makeText(this, "Image is Uploading...", Toast.LENGTH_SHORT).show()
                            }
                            is UiState.Success -> {
                                Log.d("statesawefewstwe", state.data.toString())
                                val response = state.data
                                model.imageUrl = response.image?.url

                                postViewModel.updateUser(model).observe(this) {
                                    Log.d("fohjfiwergej", "State: $it")
                                    when (it) {

                                        is UiState.Loading -> {
                                            Log.d("statess", "Loading")
                                        }
                                        is UiState.Failure -> {
                                            Log.d("states", it.error.toString())
                                        }
                                        is UiState.Success -> {
                                            preferenceManager.userModel = model
                                            onBackPressedDispatcher.onBackPressed()
                                            finish()
                                        }
                                    }
                                }
                            }
                            is UiState.Failure -> {
                                Toast.makeText(this, "Upload Failed: ${state.error}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    postViewModel.updateUser(model).observe(this) {
                        when (it) {
                            is UiState.Loading -> {
                                Log.d("statess", "Loading")
                            }
                            is UiState.Failure -> {
                                Log.d("states", it.error.toString())
                            }
                            is UiState.Success -> {
                                Log.d("states", it.data.toString())
                                preferenceManager.userModel = model
                                onBackPressedDispatcher.onBackPressed()
                                finish()
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
            val file = File(cacheDir, "user_image.jpg")
            file.outputStream().use { outputStream -> inputStream?.copyTo(outputStream) }
            return file
        }
        private fun loadProfilePicture(imageUrl: String) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.profileImage)
        }

        override fun onResume() {
            super.onResume()
            binding.UpdateProfileButton.setOnClickListener {
                updateUSer()
            }
            fetchUserData()

        }

    }