package ru.vladimir.tilikov.scopedstoragex.presentation.videos.list

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.vladimir.tilikov.scopedstoragex.data.ItemAction
import ru.vladimir.tilikov.scopedstoragex.databinding.FragmentVideoListBinding
import ru.vladimir.tilikov.scopedstoragex.utils.*

class VideoListFragment :
    ViewBindingFragment<FragmentVideoListBinding>(FragmentVideoListBinding::inflate) {

    private val viewModel by viewModels<VideoListViewModel>()
    private var videoAdapter: VideoAdapter by autoCleared()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var recoverableActionLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var addToFavoriteLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var deleteToTrashLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPermissionResultListener()
        initRecoverableActionListener()
        initAddFavoriteListener()
        initDeleteToTrashListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        bindViewModel()
        setListeners()
        if (hasPermission().not()) {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionState(hasPermission())
    }

    private fun bindViewModel() {
        with(viewModel) {
            videos.observe(viewLifecycleOwner) { list ->
                videoAdapter.items = list
//                binding.rvVideos.scrollToPosition(list.size - 1)
            }
            toastLiveEvent.observe(viewLifecycleOwner, ::toast)
            permissionsGranted.observe(viewLifecycleOwner, ::updatePermissionUi)
            recoverableAction.observe(viewLifecycleOwner, ::handleRecoverableAction)
        }
    }

    private fun setListeners() {
        binding.fabAddVideo.setOnClickListener {
            findNavController().navigate(VideoListFragmentDirections.actionVideoListFragmentToAddVideoDialogFragment())
        }
        binding.btnGrantPermissions.setOnClickListener { requestPermissions() }
    }

    private fun initList() {
        videoAdapter = VideoAdapter { id, action ->
            when (action) {
                ItemAction.ADD_FAVORITE -> handleUpdateVideoFavoriteStatus(id)
                ItemAction.TO_TRASH -> handleDeleteVideoToTrash(id)
                ItemAction.DELETE -> viewModel.deleteVideo(id)
            }
        }

        with(binding.rvVideos) {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun initPermissionResultListener() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
                if (map.values.all { it }) {
                    viewModel.permissionGranted()
                } else {
                    viewModel.permissionDenied()
                }
            }
    }

    private fun initRecoverableActionListener() {
        recoverableActionLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                val isConfirmed = result.resultCode == Activity.RESULT_OK
                if (isConfirmed) {
                    viewModel.confirmDelete()
                } else {
                    viewModel.declineDelete()
                }
            }
    }

    private fun initAddFavoriteListener() {
        addToFavoriteLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                val isConfirmed = result.resultCode == Activity.RESULT_OK
                if (isConfirmed) {
                    toast("Favorite confirmed")
                } else {
                    toast("Favorite declined")
                }
            }
    }

    private fun initDeleteToTrashListener() {
        deleteToTrashLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                val isConfirmed = result.resultCode == Activity.RESULT_OK
                if (isConfirmed) {
                    toast("Deleting to trash confirmed")
                } else {
                    toast("Deleting to trash declined")
                }
            }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(PERMISSIONS.toTypedArray())
    }

    private fun updatePermissionUi(isGranted: Boolean) {
        with(binding) {
            rvVideos.isVisible = isGranted
            fabAddVideo.isVisible = isGranted
            btnGrantPermissions.isVisible = isGranted.not()
        }
    }

    private fun hasPermission(): Boolean {
        return PERMISSIONS.all {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleRecoverableAction(action: RemoteAction) {
        val request = IntentSenderRequest.Builder(action.actionIntent.intentSender).build()
        recoverableActionLauncher.launch(request)
    }

//===============================================================================================
    private fun handleUpdateVideoFavoriteStatus(videoId: Long) {
        var intent: PendingIntent? = null
        val uri =
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)

        if (haveR()) {
            intent = MediaStore.createFavoriteRequest(
                requireContext().contentResolver,
                mutableListOf(uri),
                true
            )
        }

        val request = IntentSenderRequest.Builder(intent!!).build()
        addToFavoriteLauncher.launch(request)
    }

    private fun handleDeleteVideoToTrash(videoId: Long) {
        var intent: PendingIntent? = null
        val uri =
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)

        if (haveR()) {
            intent = MediaStore.createTrashRequest(
                requireContext().contentResolver,
                mutableListOf(uri),
                true
            )
        }

        val request = IntentSenderRequest.Builder(intent!!).build()
        deleteToTrashLauncher.launch(request)
    }
//================================================================================================

    companion object {
        private val PERMISSIONS = listOfNotNull(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE.takeIf { haveQ().not() }
        )
    }
}