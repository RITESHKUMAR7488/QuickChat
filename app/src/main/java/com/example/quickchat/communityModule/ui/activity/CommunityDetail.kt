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
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.activity.CreateCommunity.Companion
import com.example.quickchat.communityModule.ui.adapters.ViewPagerAdapter
import com.example.quickchat.communityModule.ui.fragments.Posts
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.ActivityCommunityDetailBinding
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class CommunityDetail : BaseActivity() {
    lateinit var communityId: String
    private lateinit var binding: ActivityCommunityDetailBinding
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
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_community_detail)
        communityId = intent.getStringExtra("communityId").toString()
        setUpViewPager()


    }

    private fun setupThreeDotMenu() {
        // Find the three-dot menu ImageView
        val threeDotMenu = binding.root.findViewById<ImageView>(R.id.three_dot_menu)

        // Set click listener for the three-dot menu
        threeDotMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        // Initialize PopupMenu
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.nav_menu, popupMenu.menu)

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    val intent= Intent(this,UpdateCommunity::class.java)
                    intent.putExtra("COMMUNITY_ID", communityId)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // Show the PopupMenu
        popupMenu.show()
    }

    private fun setCommunityDetails(){
        communityViewModel.getCommunityDetails(communityId).observe(this){
            Log.d("taggggggggg", "setCommunityDetails: ${it.toString()}")
            when(it){
                is UiState.Loading->{}
                is UiState.Success->{

                    binding.communityDescription.text=it.data.communityDescription
                    binding.address.text=it.data.address
                    binding.profileName.text=it.data.communityName
                    binding.mobileNumber.text=it.data.mobileNumber
                    if(it.data.imageUrl!=null){
                        Glide.with(this).load(it.data.imageUrl).into(binding.profileImage)

                    }

                }
                is UiState.Failure->{}
            }
        }
    }



    private fun setUpViewPager() {
        Log.d("TAGefergerg", "setUpViewPager: $communityId")
        val adapter = ViewPagerAdapter(this@CommunityDetail)
        adapter.addFragment(Posts.newInstance(communityId), "Posts")
        adapter.addFragment(Posts.newInstance(communityId), "Reels")
        adapter.addFragment(Posts.newInstance(communityId), "Songs")
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = false

        // Bind the viewPager with the TabLayout using TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        setupThreeDotMenu()
        setCommunityDetails()
        binding.profileImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermission(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    CreateCommunity.STORAGE_PERMISSION_CODE
                )
            } else {
                checkPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    CreateCommunity.STORAGE_PERMISSION_CODE
                )
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
        if (requestCode == CreateCommunity.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data

            Log.d("FilePathss", filePath.toString())

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)

                binding.cropImageView.visibility = View.VISIBLE
                binding.titleText.text = "Crop"
                binding.rotate.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
                binding.cropImageView.setImageUriAsync(filePath)

                binding.rotate.setOnClickListener {
                    binding.cropImageView.rotateImage(90)
                }
                binding.done.setOnClickListener {

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
        if (requestCode == CreateCommunity.STORAGE_PERMISSION_CODE) {
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