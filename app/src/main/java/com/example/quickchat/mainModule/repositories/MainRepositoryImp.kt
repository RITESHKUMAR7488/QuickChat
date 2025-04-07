package com.example.quickchat.mainModule.repositories

import android.content.Context
import android.util.Log
import androidx.collection.emptyIntList
import androidx.lifecycle.MutableLiveData
import com.example.quickchat.constants.Constant
import com.example.quickchat.constants.Constant.COMMENTS
import com.example.quickchat.constants.Constant.POSTS
import com.example.quickchat.mainModule.inteface.ImageUploadApi
import com.example.quickchat.mainModule.inteface.VideoGetApi
import com.example.quickchat.mainModule.inteface.VideoUploadApi
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.CommentModel
import com.example.quickchat.mainModule.models.ImageUploadResponse
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.models.VideoGetResponse
import com.example.quickchat.mainModule.models.VideoUploadResponse
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

class MainRepositoryImp(
    private val database: FirebaseFirestore,
    private val imageUploadApi: ImageUploadApi,
    private val videoUploadApi: VideoUploadApi,
    private val videoGetApi: VideoGetApi,

    ) : RepositoryMain {
    private lateinit var userId: String

    @Inject
    lateinit var preferenceManager: PreferenceManager


    override fun addPost(
        communityId: String,
        model: PostModel,
        result: (UiState<PostModel>) -> Unit
    ) {
        database.collection(Constant.COMMUNITIES)
            .document(communityId).collection(Constant.MY_POST).add(model)
            .addOnSuccessListener { documentReference ->

                val postId = documentReference.id
                model.postId = postId

                database.collection(Constant.POSTS)
                    .document(postId)
                    .set(model)
                    .addOnSuccessListener {
                        result.invoke(
                            UiState.Success(model)
                        )
                    }
                    .addOnFailureListener { e ->
                        result.invoke(
                            UiState.Failure(e.message ?: "An error occurred")
                        )
                    }

            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(it.message ?: "An error occurred")
                )
            }

    }

    override fun getDetails(
        userId: String,
        result: (UiState<UserModel?>) -> Unit
    ) {
        val usersCollection = database.collection(Constant.USERS)
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userModel: UserModel? = document.toObject(UserModel::class.java)
                    Log.d("nkss", userModel.toString())
                    result.invoke(UiState.Success(userModel))
                } else {
                    Log.d("Firestore", "No such user found!")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user details", e)
                result.invoke(UiState.Failure(e.message ?: "An error occurred"))

            }
    }

    override fun getAllCommunities(
        userId: String,
        result: (UiState<List<AllCommunityModel>>) -> Unit
    ) {
        val allCommunityCollection = database.collection(Constant.COMMUNITIES)

        allCommunityCollection
            .whereEqualTo("userId", userId) // Adjust the field name as per your Firestore structure
            .get()
            .addOnSuccessListener { querySnapshot ->
                val communities = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(AllCommunityModel::class.java)
                }
                result(UiState.Success(communities))
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(it.message ?: "An error occurred")
                )
            }

    }

    override fun getAllPost(result: (UiState<List<MainPostModel>>) -> Unit) {
        val postCollection = database.collection(Constant.POSTS)
        val communityCollection = database.collection(Constant.COMMUNITIES)

        val postTask = postCollection.get()
        val communityTask = communityCollection.get()

        Tasks.whenAllSuccess<QuerySnapshot>(postTask, communityTask)
            .addOnSuccessListener { snapshots ->
                try {
                    val postSnapshot = snapshots[0] as QuerySnapshot
                    val communitySnapshot = snapshots[1] as QuerySnapshot

                    val posts = mutableListOf<MainPostModel>()
                    val communities = mutableListOf<AllCommunityModel>()

                    // Convert POSTS collection to MainPostModel.TypeOneItem
                    for (doc in postSnapshot.documents) {
                        val post = doc.toObject(PostModel::class.java)
                        post?.let { posts.add(MainPostModel.TypeOneItem(it)) }
                    }

                    // Convert COMMUNITIES collection to a list
                    for (doc in communitySnapshot.documents) {
                        val community = doc.toObject(AllCommunityModel::class.java)
                        community?.let { communities.add(it) }
                    }

                    // Prepare final list by inserting Community chunks after a random number of posts
                    val finalList = mutableListOf<MainPostModel>()
                    var index = 0

                    while (index < posts.size) {
                        // Generate a random chunk size between 6 and 10
                        val chunkSize = Random.nextInt(6, 11)

                        // Add posts in the random chunk size
                        finalList.addAll(posts.subList(index, minOf(index + chunkSize, posts.size)))
                        index += chunkSize

                        // Add a chunk of communities (if available)
                        if (communities.isNotEmpty()) {
                            val communityChunk = communities.take(2) // Take 2 communities at a time
                            finalList.add(MainPostModel.CommunityChunk(communityChunk))
                            communities.subList(0, minOf(2, communities.size))
                                .clear() // Remove used items
                        }
                    }

                    result(UiState.Success(finalList))
                } catch (e: Exception) {
                    result(UiState.Failure(e.localizedMessage ?: "Error fetching data"))
                }
            }.addOnFailureListener { exception ->
                result(UiState.Failure(exception.localizedMessage ?: "Error fetching data"))
            }
    }

    override fun uploadImage(
        imageFile: File,  // File to upload
        apiKey: String,   // API key
        data: MutableLiveData<ImageUploadResponse>,
        error: MutableLiveData<Throwable>
    ) {
        // Create a RequestBody for the image file
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("source", imageFile.name, requestBody)

        // Make the API call
        imageUploadApi.uploadImage(apiKey, action = "upload", image = imagePart)
            .enqueue(object : Callback<ImageUploadResponse> {
                override fun onResponse(
                    call: Call<ImageUploadResponse>,
                    response: Response<ImageUploadResponse>
                ) {
                    Log.d("responsess", "Image uploaded successfully: ${response.body()}")
                    if (response.isSuccessful && response.body() != null) {
                        // Successfully received response
                        data.value = response.body()
                        Log.d(
                            "responsess",
                            "Image uploaded successfully: ${response.body()?.image?.url}"
                        )
                    } else {
                        // Handle unsuccessful response
                        Log.d("responsess", "Failed: ${response.message()}")
                        data.value = null
                    }
                }

                override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                    // Handle failure (e.g., network error)
                    error.value = t
                    Log.d("responsess", "Failed to upload image: ${t.message}")
                }
            })
    }

    override fun getVideo(
        data: MutableLiveData<VideoGetResponse>,
        error: MutableLiveData<Throwable>
    ) {

        videoGetApi.getVideo().enqueue(object : Callback<VideoGetResponse?> {
            override fun onResponse(
                p0: Call<VideoGetResponse?>,
                response: Response<VideoGetResponse?>
            ) {
                Log.d("VideoResponse1", "Failed: $response")
                if (response.isSuccessful && response.body() != null) {
                    data.value = response.body()

                } else {
                    data.value = null
                    Log.d("VideoResponse", "Failed: ${response.message()}")
                }
            }

            override fun onFailure(p0: Call<VideoGetResponse?>, p1: Throwable) {
                error.value = p1
                Log.e("VideoResponse", "Error fetching videos: ${p1.message}")
            }


        })
    }

    override fun updateUser(userModel: UserModel, result: (UiState<UserModel>) -> Unit) {
        val userId = userModel.uid ?: return result.invoke(UiState.Failure("User ID is null"))

        database.collection(Constant.USERS)
            .document(userId)
            .set(userModel)
            .addOnSuccessListener {
                result.invoke(UiState.Success(userModel))
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.message ?: "Failed to update user"))
            }
    }

    override fun likePost(postId: String, userId: String, result: (UiState<PostModel>) -> Unit) {
        val postRef = database.collection(Constant.POSTS).document(postId)

        database.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val post = snapshot.toObject(PostModel::class.java)
                ?: throw Exception("Post not found")

            // Add the user's ID to the likes list
            val updatedLikes = post.likes?.toMutableList() ?: mutableListOf()
            if (!updatedLikes.contains(userId)) {
                updatedLikes.add(userId)
            }

            // Update the post with the new likes list
            transaction.update(postRef, "likes", updatedLikes)

            // Return the updated post
            post.copy(likes = updatedLikes)
        }.addOnSuccessListener { updatedPost ->
            result.invoke(UiState.Success(updatedPost))
        }.addOnFailureListener { exception ->
            result.invoke(UiState.Failure(exception.message ?: "Failed to like post"))
        }
    }

    override fun unlikePost(postId: String, userId: String, result: (UiState<PostModel>) -> Unit) {
        val postRef = database.collection(Constant.POSTS).document(postId)

        database.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val post = snapshot.toObject(PostModel::class.java)
                ?: throw Exception("Post not found")

            // Remove the user's ID from the likes list
            val updatedLikes = post.likes?.toMutableList() ?: mutableListOf()
            updatedLikes.remove(userId)

            // Update the post with the new likes list
            transaction.update(postRef, "likes", updatedLikes)

            // Return the updated post
            post.copy(likes = updatedLikes)
        }.addOnSuccessListener { updatedPost ->
            result.invoke(UiState.Success(updatedPost))
        }.addOnFailureListener { exception ->
            result.invoke(UiState.Failure(exception.message ?: "Failed to unlike post"))
        }
    }


    override fun uploadVideo(
        videoFile: File,
        apiKey: String,
        data: MutableLiveData<VideoUploadResponse>,
        error: MutableLiveData<Throwable>
    ) {
        // Create a RequestBody for the video file
        val requestBody = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, requestBody)

        // Make the API call
        videoUploadApi.uploadVideo(apiKey, action = "upload", video = videoPart)
            .enqueue(object : Callback<VideoUploadResponse> {
                override fun onResponse(
                    call: Call<VideoUploadResponse>,
                    response: Response<VideoUploadResponse>
                ) {
                    Log.d("VideoUpload", "Video uploaded successfully: ${response.body()}")
                    if (response.isSuccessful && response.body() != null) {
                        // Successfully received response
                        data.value = response.body()
                    } else {
                        // Handle unsuccessful response
                        Log.d("VideoUpload", "Failed: ${response.message()}")
                        data.value = null
                    }
                }

                override fun onFailure(call: Call<VideoUploadResponse>, t: Throwable) {
                    // Handle failure (e.g., network error)
                    error.value = t
                    Log.e("VideoUpload", "Failed to upload video: ${t.message}")
                }
            })
    }

    override fun getAllUser(userId: String, result: (UiState<List<UserModel>>) -> Unit) {
        val usersCollection = database.collection(Constant.USERS)
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(UserModel::class.java)
                }
                result(UiState.Success(users))
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user details", e)
                result.invoke(UiState.Failure(e.message ?: "An error occurred"))


            }



    }

    override fun addComment(
        postId: String,
        comment: CommentModel,
        result: (UiState<CommentModel>) -> Unit
    ) {
       val commentRef=database.collection(Constant.POSTS).document(postId).collection(Constant.COMMENTS).document()
        comment.commentId=commentRef.id
        comment.timestamp=System.currentTimeMillis()
        comment.likes= 0

        commentRef.set(comment).addOnSuccessListener {
                result.invoke(UiState.Success(comment))
        }.addOnFailureListener{ e ->
            result.invoke(UiState.Failure(e.message ?: "An error occurred"))

        }


    }

    override fun getComments(postId: String, result: (UiState<List<CommentModel>>) -> Unit) {
        database.collection(Constant.POSTS)
            .document(postId)
            .collection(Constant.COMMENTS)
            .orderBy("timestamp", Query.Direction.DESCENDING) // Newest first
            .get()
            .addOnSuccessListener { querySnapshot ->
                val comments = mutableListOf<CommentModel>()
                for (document in querySnapshot.documents) {
                    val comment = document.toObject(CommentModel::class.java)?.apply {
                        commentId = document.id // Ensure commentId is set
                    }
                    comment?.let { comments.add(it) }
                }
                result.invoke(UiState.Success(comments))
            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.message ?: "Failed to load comments"
                    )
                )
            }
    }

    override fun likeComment(
        postId: String,
        commentId: String,
        userId: String,
        result: (UiState<CommentModel>) -> Unit
    ) {
        val commentRef = database.collection(Constant.POSTS)
            .document(postId)
            .collection(Constant.COMMENTS)
            .document(commentId)

        commentRef.update("likes", FieldValue.increment(1))
            .addOnSuccessListener {
                // Fetch the updated comment to return
                commentRef.get().addOnSuccessListener { snapshot ->
                    val updatedComment = snapshot.toObject(CommentModel::class.java)
                    updatedComment?.let {
                        result.invoke(UiState.Success(it))
                    } ?: result.invoke(UiState.Failure("Comment not found"))
                }
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.message ?: "Failed to like comment"))
            }
    }

    override fun unlikeComment(
        postId: String,
        commentId: String,
        userId: String,
        result: (UiState<CommentModel>) -> Unit
    ) {
        val commentRef = database.collection(Constant.POSTS)
            .document(postId)
            .collection(Constant.COMMENTS)
            .document(commentId)

        commentRef.update("likes", FieldValue.increment(-1))
            .addOnSuccessListener {
                // Fetch the updated comment to return
                commentRef.get().addOnSuccessListener { snapshot ->
                    val updatedComment = snapshot.toObject(CommentModel::class.java)
                    updatedComment?.let {
                        result.invoke(UiState.Success(it))
                    } ?: result.invoke(UiState.Failure("Comment not found"))
                }
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.message ?: "Failed to unlike comment"))
            }
    }

}

