package com.example.presentation.main.setting.syncdata

import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.domain.usecase.sync.SyncDomainEvent
import com.example.domain.usecase.sync.SyncUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncUseCase: SyncUseCase
) : BaseViewModel() {

    private val _mutableStateFlow = MutableStateFlow(SyncUiState.INITIAL)
    val stateFlow: StateFlow<SyncUiState> = _mutableStateFlow.asStateFlow()

    private val _actionSharedFlow = MutableSharedFlow<SyncAction>(extraBufferCapacity = 64)
    private inline fun <reified T : SyncAction> action() = _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<SyncSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<SyncSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: SyncAction) = viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        observeSyncEvents()
        handleSyncActions()
        handleSendData()
    }

    private fun observeSyncEvents() {
        syncUseCase.observeSyncEvents()
            .onEach { event ->
                when (event) {
                    is SyncDomainEvent.DeviceFound -> _mutableStateFlow.update { state ->
                        val newList = state.foundDevices.toMutableList()
                        if (newList.none { it.first == event.id }) newList.add(event.id to event.name)
                        state.copy(foundDevices = newList)
                    }
                    is SyncDomainEvent.DataSavedSuccess -> {
                        // Khi máy nhận báo DataSavedSuccess, máy gửi cũng có thể coi là hoàn tất
                        updateLoading(false)
                        _singleEventChannel.send(SyncSingleEvent.SyncSuccess)
                    }
                    is SyncDomainEvent.StatusChanged -> {
                        val isConnected = event.status == "Connected"

                        // Logic tắt loading khi có thông báo hoàn tất hoặc lỗi
                        if (event.status.contains("hoàn tất") || event.status.contains("thành công")) {
                            updateLoading(false)
                        }

                        _mutableStateFlow.update { state ->
                            state.copy(
                                isConnected = isConnected,
                                // Nếu mất kết nối thì tắt loading
                                isLoading = if (!isConnected && state.isLoading) false else state.isLoading
                            )
                        }
                        _singleEventChannel.send(SyncSingleEvent.StatusChanged(event.status))
                    }
                    is SyncDomainEvent.Progress -> {
                        _mutableStateFlow.update { it.copy(
                            syncProgress = event.value,
                            isLoading = true
                        ) }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun handleSyncActions() {
        // 1. Handle Broadcasting
        action<SyncAction.StartBroadcasting>()
            .onEach { action ->
                syncUseCase.startBroadcasting(action.deviceName).fold(
                    success = { _singleEventChannel.send(SyncSingleEvent.StatusChanged("Sending a signal...")) },
                    failure = { _singleEventChannel.send(SyncSingleEvent.SyncFailed(it)) }
                )
            }.launchIn(viewModelScope)

        // 2. Handle Searching
        action<SyncAction.StartSearching>()
            .onEach {
                _mutableStateFlow.update { it.copy(foundDevices = emptyList()) }
                syncUseCase.startSearching().fold(
                    success = { _singleEventChannel.send(SyncSingleEvent.StatusChanged("Looking for equipment...")) },
                    failure = { _singleEventChannel.send(SyncSingleEvent.SyncFailed(it)) }
                )
            }.launchIn(viewModelScope)

        // 3. Handle Connect
        action<SyncAction.ConnectToDevice>()
            .onEach { action ->
                updateLoading(true)
                syncUseCase.connect(action.endpointId).fold(
                    success = { _singleEventChannel.send(SyncSingleEvent.StatusChanged("Connecting...")) },
                    failure = {
                        updateLoading(false)
                        _singleEventChannel.send(SyncSingleEvent.SyncFailed(it))
                    }
                )
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleSendData() {
        action<SyncAction.SendData>()
            .onEach { action ->
                // Bắt đầu quá trình đồng bộ toàn diện
                updateLoading(true)
                _singleEventChannel.send(SyncSingleEvent.StatusChanged("Đang đóng gói hình ảnh và ghi âm..."))

                // Gọi hàm sendFullSync thay vì sendAllData
                syncUseCase.sendFullSync(action.endpointId).fold(
                    success = {
                        // Lưu ý: SUCCESS ở đây có nghĩa là đã bắt đầu gửi thành công
                        // Tiến trình gửi thực tế sẽ được cập nhật qua observeSyncEvents (SyncDomainEvent.Progress)
                        _singleEventChannel.send(SyncSingleEvent.StatusChanged("Đang truyền tải dữ liệu đa phương tiện..."))
                    },
                    failure = { error ->
                        updateLoading(false)
                        _singleEventChannel.send(SyncSingleEvent.SyncFailed(error))
                    }
                )
                // Không nên updateLoading(false) ngay tại đây vì quá trình truyền file ZIP
                // có thể kéo dài. Ta sẽ tắt loading khi nhận được sự kiện hoàn tất từ observeSyncEvents.
            }.launchIn(viewModelScope)
    }

    fun setConnectingDevice(endpointId: String) {
        _mutableStateFlow.update { it.copy(connectedEndpointId = endpointId) }
    }

    private fun updateLoading(isLoading: Boolean) {
        _mutableStateFlow.update { it.copy(isLoading = isLoading) }
    }

    override fun onCleared() {
        syncUseCase.stopSync()
        super.onCleared()
    }
}