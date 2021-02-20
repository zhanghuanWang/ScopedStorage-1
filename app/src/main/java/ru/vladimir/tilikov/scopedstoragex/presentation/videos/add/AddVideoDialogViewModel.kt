package ru.vladimir.tilikov.scopedstoragex.presentation.videos.add

import android.app.Application
import android.content.ContentUris
import android.content.UriMatcher
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.vladimir.tilikov.scopedstoragex.data.repositories.VideosRepository
import ru.vladimir.tilikov.scopedstoragex.utils.SingleLiveEvent
import timber.log.Timber

class AddVideoDialogViewModel(application: Application) : AndroidViewModel(application) {

    private val videoRepository = VideosRepository(application)
    private val errorHandler = CoroutineExceptionHandler { _, t ->
        Timber.e(t, "CoroutineExceptionHandler call")
    }

    private val _isLoading = MutableLiveData(false)
    private val _saveSuccessLiveEvent = SingleLiveEvent<Unit>()
    private val _toastLiveEvent = SingleLiveEvent<String>()

    val isLoading: LiveData<Boolean>
        get() = _isLoading
    val saveSuccessLiveEvent: LiveData<Unit>
        get() = _saveSuccessLiveEvent
    val toastLiveEvent: LiveData<String>
        get() = _toastLiveEvent

    fun saveVideo(uri: Uri, url: String) {
        viewModelScope.launch(errorHandler) {
            _isLoading.value = true
            try {
                videoRepository.saveVideo(uri, url)
                _saveSuccessLiveEvent.call()
            } catch (t: Throwable) {
                Timber.e(t, "Saving error")
                _toastLiveEvent.postValue("Saving error")

//========================================================================================
                uri.lastPathSegment?.toLongOrNull()?.let { id ->
                    videoRepository.deleteVideoById(id)
                }
//============================================================================================
            } finally {
                _isLoading.value = false
            }
        }
    }
}