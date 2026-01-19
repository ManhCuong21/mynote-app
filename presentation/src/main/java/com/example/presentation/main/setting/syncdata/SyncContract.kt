package com.example.presentation.main.setting.syncdata

data class SyncUiState(
    val isLoading: Boolean = false,
    val foundDevices: List<Pair<String, String>> = emptyList(),
    val syncProgress: Int = 0,
    val isConnected: Boolean = false, // Thêm biến này
    val connectedDeviceName: String? = null,
    val connectedEndpointId: String? = null,
){
    companion object {
        val INITIAL = SyncUiState()
    }
}

// Action gửi từ UI
sealed interface SyncAction {
    data class StartBroadcasting(val deviceName: String) : SyncAction
    object StartSearching : SyncAction
    data class ConnectToDevice(val endpointId: String) : SyncAction
    data class SendData(val endpointId: String) : SyncAction
    object StopSync : SyncAction
}

// Event bắn ngược lại UI (Toast, Dialog, Nav)
sealed interface SyncSingleEvent {
    object SyncSuccess : SyncSingleEvent
    data class SyncFailed(val error: Throwable?) : SyncSingleEvent
    data class StatusChanged(val status: String) : SyncSingleEvent
}