package ru.vladimir.tilikov.scopedstoragex.presentation.videos.add

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import ru.vladimir.tilikov.scopedstoragex.databinding.DialogAddVideoBinding
import ru.vladimir.tilikov.scopedstoragex.utils.ViewBindingBottomDialogFragment
import ru.vladimir.tilikov.scopedstoragex.utils.toast
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class AddVideoBottomDialogFragment :
    ViewBindingBottomDialogFragment<DialogAddVideoBinding>(DialogAddVideoBinding::inflate) {

    private val viewModel by viewModels<AddVideoDialogViewModel>()

    private lateinit var createDocumentLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCreateDocumentLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewModel()
        setListeners()
        dialog?.setCancelable(true)
    }

    private fun bindViewModel() {
        with(viewModel) {
            isLoading.observe(viewLifecycleOwner, ::setLoading)
            saveSuccessLiveEvent.observe(viewLifecycleOwner) { dismiss() }
            toastLiveEvent.observe(viewLifecycleOwner, ::toast)
        }
    }

    private fun setListeners() {
        binding.btnSaveVideo.setOnClickListener {
            val name = binding.etVideoName.editText?.text.toString()
            createDocumentLauncher.launch(name)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        with(binding) {
            dialog?.setCanceledOnTouchOutside(isLoading.not())
            contentGroup.isVisible = isLoading.not()
            progressBar.isVisible = isLoading
        }
    }

    private fun initCreateDocumentLauncher() {
        createDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
                handleCreateVideo(uri)
                Timber.d("URI = $uri")
            }
    }

    private fun handleCreateVideo(uri: Uri?) {
        if (uri == null) {
            toast("not selected")
            return
        }

        val url = binding.etVideoUrl.editText?.text.toString()
        saveVideo(uri, url)
    }

    private fun saveVideo(uri: Uri, url: String) {
        viewModel.saveVideo(uri, url)
    }
}