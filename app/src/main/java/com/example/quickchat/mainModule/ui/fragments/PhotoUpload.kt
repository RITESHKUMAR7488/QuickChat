package com.example.quickchat.mainModule.ui.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.fragments.Posts
import com.example.quickchat.databinding.FragmentPhotoUploadBinding
import com.example.quickchat.mainModule.models.DetailModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class PhotoUpload : BaseFragment() {

    private lateinit var binding: FragmentPhotoUploadBinding
    private val postViewModel: PostViewModel by viewModels()
    private var communityId: String? = null
    private var filePath: Uri? = null

    companion object {
        private const val ARG_COMMUNITY_ID = "communityId"

        fun newInstance(communityId: String): PhotoUpload {
            val fragment = PhotoUpload()
            val args = Bundle()
            args.putString(ARG_COMMUNITY_ID, communityId)
            fragment.arguments = args
            return fragment
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            filePath = result.data?.data
            Log.d("FilePathss", filePath.toString())

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)
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
                    binding.mainLayout.visibility = View.VISIBLE
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            selectImage()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhotoUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        communityId = arguments?.getString(ARG_COMMUNITY_ID)

        if (communityId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid community selected.", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
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
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(permission)
        } else {
            selectImage()
        }
    }

    private fun validateAndCreatePost() {
        with(binding) {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Please enter a post title"
                Toast.makeText(requireContext(), "Please enter a post title", Toast.LENGTH_SHORT).show()
                return
            }

            if (description.isEmpty()) {
                etDescription.error = "Please enter a post description"
                Toast.makeText(requireContext(), "Please enter a post description", Toast.LENGTH_SHORT).show()
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
                postViewModel.uploadImage(file, apiKey).observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is UiState.Loading -> {
                            Toast.makeText(requireContext(), "Image is Uploading...", Toast.LENGTH_SHORT).show()
                        }

                        is UiState.Success -> {
                            val response = state.data
                            post.imageUrl = response.image?.url

                            preferenceManager.userId?.let { userId ->
                                communityId?.let { communityId ->
                                    postViewModel.addPost(communityId, post)
                                        .observe(viewLifecycleOwner) { it2 ->
                                            when (it2) {
                                                is UiState.Loading -> {
                                                    Log.d("TAG", "Creating community: Loading...")
                                                }

                                                is UiState.Success -> {
                                                    Toast.makeText(requireContext(), "Community created successfully", Toast.LENGTH_SHORT).show()
                                                }

                                                is UiState.Failure -> {
                                                    Toast.makeText(requireContext(), it2.error, Toast.LENGTH_SHORT).show()
                                                }

                                                else -> {
                                                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                }
                            }
                        }
                        is UiState.Failure -> {
                            Toast.makeText(requireContext(), "Upload Failed: ${state.error}", Toast.LENGTH_SHORT).show()
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
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Image from here..."))
    }

    private fun getUriFromBitmap(bitmap: Bitmap): Uri {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, ByteArrayOutputStream())
        val path = MediaStore.Images.Media.insertImage(
            requireActivity().contentResolver,
            bitmap, System.currentTimeMillis().toString(), null
        )
        return Uri.parse(path)
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val file = File(requireActivity().cacheDir, "community_image.jpg")
        file.outputStream().use { outputStream -> inputStream?.copyTo(outputStream) }
        return file
    }
}