package com.example.presentation.note.image

import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.domain.usecase.file.ImageFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageNoteViewModel @Inject constructor(
    private val imageFileUseCase: ImageFileUseCase,
) : BaseViewModel() {
    private val _actionSharedFlow = MutableSharedFlow<ImageNoteAction>(extraBufferCapacity = 64)
    private inline fun <reified T : ImageNoteAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<ImageNoteSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<ImageNoteSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: ImageNoteAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        getListImage()
        deleteImage()
        saveImageNote()
    }

    private fun getListImage() {
        action<ImageNoteAction.GetListImageNote>()
            .onEach {
                val listImage = imageFileUseCase.readImage(it.context)
                _singleEventChannel.send(ImageNoteSingleEvent.GetListImage(listImage))
            }.launchIn(viewModelScope)
    }

    private fun deleteImage() {
        action<ImageNoteAction.DeleteImageNote>()
            .onEach {
                imageFileUseCase.deleteImage(it.pathImage)
                dispatch(ImageNoteAction.GetListImageNote(it.context))
            }
            .launchIn(viewModelScope)
    }

    private fun saveImageNote() {
        action<ImageNoteAction.SaveImageNote>()
            .onEach {
                imageFileUseCase.saveImageToTemp(it.context, it.bitmap)
                dispatch(ImageNoteAction.GetListImageNote(it.context))
            }.launchIn(viewModelScope)
    }
}