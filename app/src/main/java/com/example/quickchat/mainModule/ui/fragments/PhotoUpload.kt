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

    private var _binding: FragmentPhotoUploadBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    private val postViewModel: PostViewModel by viewModels()
    private var communityId: String? = null
    private var filePath: Uri? = null

    companion object {
        private const val ARG_COMMUNITY_ID = "communityId"

        fun newInstance(communityId: String): PhotoUpload {
            return PhotoUpload().apply {
                arguments = Bundle().apply {
                    putString(ARG_COMMUNITY_ID, communityId)
                }
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null && isAdded) {
            filePath = result.data?.data
            Log.d("FilePathss", filePath.toString())

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)
                with(binding) {
                    mainLayout.visibility = View.GONE
                    cropImageView.visibility = View.VISIBLE
                    titleText.text = "Crop"
                    rotate.visibility = View.VISIBLE
                    done.visibility = View.VISIBLE
                    cropImageView.setImageUriAsync(filePath)
                }
            } catch (e: IOException) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted && isAdded) {
            selectImage()
        } else if (isAdded) {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        communityId = arguments?.getString(ARG_COMMUNITY_ID)

        if (communityId.isNullOrEmpty()) {
            if (isAdded) {
                Toast.makeText(requireContext(), "Invalid community selected.", Toast.LENGTH_SHORT).show()
            }
            parentFragmentManager.popBackStack()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        with(binding) {
            btnPost.setOnClickListener { validateAndCreatePost() }
            postImage.setOnClickListener { checkAndRequestPermissions() }

            back.setOnClickListener {
                if (isAdded) {
                    parentFragmentManager.beginTransaction()
                        .remove(this@PhotoUpload)
                        .commit()
                }
            }

            rotate.setOnClickListener {
                if (isAdded) {
                    cropImageView.rotateImage(90)
                }
            }

            done.setOnClickListener {
                if (isAdded) {
                    with(binding) {
                        mainLayout.visibility = View.VISIBLE
                        cropImageView.visibility = View.GONE
                        rotate.visibility = View.GONE
                        done.visibility = View.GONE
                        titleText.text = "Profile"
                        val cropped: Bitmap? = cropImageView.croppedImage
                        filePath = cropped?.let { getUriFromBitmap(it) }
                        postImage.setImageBitmap(cropped)
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (!isAdded) return

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            selectImage()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun validateAndCreatePost() {
        if (!isAdded) return

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
                    if (!isAdded) return@observe

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
                                        .observe(viewLifecycleOwner) { state ->
                                            if (!isAdded) return@observe

                                            when (state) {
                                                is UiState.Loading -> {
                                                    Log.d("TAG", "Creating community: Loading...")
                                                }
                                                is UiState.Success -> {
                                                    Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()
                                                    parentFragmentManager.popBackStack()
                                                }
                                                is UiState.Failure -> {
                                                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
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
            } else {
                // Handle case where no image is selected but still want to post
                preferenceManager.userId?.let { userId ->
                    communityId?.let { communityId ->
                        postViewModel.addPost(communityId, post)
                            .observe(viewLifecycleOwner) { state ->
                                if (!isAdded) return@observe

                                when (state) {
                                    is UiState.Loading -> { /* Handle loading */ }
                                    is UiState.Success -> {
                                        Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()
                                        parentFragmentManager.popBackStack()
                                    }
                                    is UiState.Failure -> {
                                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun selectImage() {
        if (!isAdded) return

        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Image"))
    }

    private fun getUriFromBitmap(bitmap: Bitmap): Uri? {
        if (!isAdded) return null

        return try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bytes)
            val path = MediaStore.Images.Media.insertImage(
                requireActivity().contentResolver,
                bitmap, System.currentTimeMillis().toString(), null
            )
            Uri.parse(path)
        } catch (e: Exception) {
            if (isAdded) {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    private fun uriToFile(uri: Uri): File? {
        if (!isAdded) return null

        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val file = File(requireActivity().cacheDir, "community_image.jpg")
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            if (isAdded) {
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}