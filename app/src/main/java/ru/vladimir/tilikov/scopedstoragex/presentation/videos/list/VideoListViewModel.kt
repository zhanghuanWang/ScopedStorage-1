package ru.vladimir.tilikov.scopedstoragex.presentation.videos.list

import android.app.Application
import android.app.RecoverableSecurityException
import android.app.RemoteAction
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import ru.vladimir.tilikov.scopedstoragex.R
import ru.vladimir.tilikov.scopedstoragex.data.Video
import ru.vladimir.tilikov.scopedstoragex.data.repositories.VideosRepository
import ru.vladimir.tilikov.scopedstoragex.utils.SingleLiveEvent
import ru.vladimir.tilikov.scopedstoragex.utils.haveQ
import timber.log.Timber

class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    private val videoRepository = VideosRepository(application)
    private val errorHandler = CoroutineExceptionHandler { _, t ->
        Timber.e(t, "CoroutineExceptionHandler call")
    }

    private var isObservingStarted = false
    private var pendingDeleteId: Long? = null

    private val _videos = MutableLiveData<List<Video>>()
    private val _toastLiveEvent = SingleLiveEvent<Int>()
    private val _permissionsGranted = MutableLiveData<Boolean>()
    private val _recoverableAction = MutableLiveData<RemoteAction>()

    val videos: LiveData<List<Video>>
        get() = _videos
    val toastLiveEvent: LiveData<Int>
        get() = _toastLiveEvent
    val permissionsGranted: LiveData<Boolean>
        get() = _permissionsGranted
    val recoverableAction: LiveData<RemoteAction>
        get() = _recoverableAction

    override fun onCleared() {
        super.onCleared()
        videoRepository.unregisterVideoObserver()
    }

    private fun loadAllVideos() {
        viewModelScope.launch(errorHandler) {
            try {
                _videos.postValue(videoRepository.getAllVideos())
            } catch (t: Throwable) {
                Timber.e(t, "Error loading videos")
                _toastLiveEvent.postValue(R.string.error_loading_videos)
                _videos.value = emptyList()
            }
        }
    }

    fun permissionGranted() {
        loadAllVideos()
        if (isObservingStarted.not()) {
            videoRepository.observeVideos { loadAllVideos() }
            isObservingStarted = true
        }
        _permissionsGranted.postValue(true)
    }

    fun permissionDenied() {
        _permissionsGranted.postValue(false)
        _toastLiveEvent.postValue(R.string.need_permission_access)
    }

    fun updatePermissionState(isGranted: Boolean) {
        if (isGranted) {
            permissionGranted()
        } else {
            permissionDenied()
        }
    }

    fun deleteVideo(id: Long) {
        viewModelScope.launch(errorHandler) {
            try {
                videoRepository.deleteVideoById(id)
                pendingDeleteId = null
            } catch (t: Throwable) {
                if (haveQ() && t is RecoverableSecurityException) {
                    pendingDeleteId = id
                    _recoverableAction.postValue(t.userAction)
                } else {
                    _toastLiveEvent.postValue(R.string.deleting_error)
                }
            }
        }
    }

    fun confirmDelete() {
        pendingDeleteId?.let {
            deleteVideo(it)
        }
    }

    fun declineDelete() {
        pendingDeleteId = null
    }
}