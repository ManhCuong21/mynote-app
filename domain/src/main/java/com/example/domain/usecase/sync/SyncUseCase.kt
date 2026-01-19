package com.example.domain.usecase.sync

import android.content.Context
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.NoteModel
import com.example.data.datalocal.repository.CategoryRepository
import com.example.data.datalocal.repository.NoteRepository
import com.example.data.file.file.FileRepository
import com.example.data.syncdata.manager.SyncEventListener
import com.example.data.syncdata.manager.SyncManager
import com.example.data.syncdata.model.SyncPackage
import com.example.domain.mapper.toNoteModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.runCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val fileRepository: FileRepository,
    private val syncManager: SyncManager
) {
    fun observeSyncEvents(): Flow<SyncDomainEvent> = callbackFlow {
        syncManager.setListener(object : SyncEventListener {

            override fun onSyncPackageReceived(syncPackage: SyncPackage) {
                // Dữ liệu đã được lưu ở tầng Data rồi
                // Ở đây ta chỉ map sang Model để báo lên UI cập nhật
                val noteModels = syncPackage.notes.map { it.toNoteModel() }
                trySend(SyncDomainEvent.DataSavedSuccess(noteModels))
                trySend(SyncDomainEvent.StatusChanged("Đồng bộ hoàn tất: ${noteModels.size} ghi chú"))
            }

            override fun onStatusChanged(status: String) {
                trySend(SyncDomainEvent.StatusChanged(status))
            }

            override fun onProgressUpdate(progress: Int) {
                trySend(SyncDomainEvent.Progress(progress))
            }

            override fun onDeviceFound(id: String, name: String) {
                trySend(SyncDomainEvent.DeviceFound(id, name))
            }
        })

        awaitClose { syncManager.stopAll() }
    }

    // SyncUseCase.kt
    suspend fun sendAllData(endpointId: String): Result<Unit, Throwable> = withContext(appCoroutineDispatchers.io) {
        runCatching {
            val categories = categoryRepository.getAllCategory().getOrThrow()
            val notes = noteRepository.readAllNote().getOrThrow()

            val syncPackage = SyncPackage(categories, notes)
            syncManager.sendData(endpointId, syncPackage)
        }
    }

    suspend fun sendFullSync(endpointId: String): Result<Unit, Throwable> = withContext(appCoroutineDispatchers.io) {
        runCatching {
            // 1. Gửi Metadata (Categories & Notes)
            // Việc gửi JSON trước giúp máy nhận có ID và FolderName để chờ khớp file.
            val categories = categoryRepository.getAllCategory().getOrThrow()
            val notes = noteRepository.readAllNote().getOrThrow()

            val syncPackage = SyncPackage(categories, notes)
            syncManager.sendData(endpointId, syncPackage)

            // 2. Chuẩn bị file ZIP cho thư mục Pictures
            // Thông báo trạng thái đang đóng gói để UI không bị "đứng" cảm giác
            // Bạn có thể emit Status thông qua một Flow khác hoặc Listener nếu cần.

            // Tạo file zip tạm trong cache của App
            val tempZip = File(context.cacheDir, "sync_media_${System.currentTimeMillis()}.zip")

            // Hàm này (trong FileRepository) phải lọc chỉ lấy MediaNote* và bỏ qua Temp
            fileRepository.zipPicturesDirectory(tempZip)

            // 3. Gửi file ZIP duy nhất chứa tất cả Media
            if (tempZip.exists() && tempZip.length() > 0) {
                syncManager.sendFile(endpointId, tempZip)
                // Lưu ý: Không xóa tempZip ngay lập tức vì Nearby Connections cần file để stream.
                // Temp file này nên được xóa khi onPayloadTransferUpdate báo SUCCESS.
            } else {
                // Nếu không có file media nào, thông báo hoàn tất luôn
                // syncManager.onStatusChanged("Không có dữ liệu đa phương tiện để gửi")
            }
            Unit
        }
    }

    // 3. Các hàm điều khiển kết nối
    fun startBroadcasting(deviceName: String): Result<Unit, Throwable> =
        runCatching { syncManager.startBroadcasting(deviceName) }

    fun startSearching(): Result<Unit, Throwable> =
        runCatching { syncManager.startSearching() }

    fun connect(endpointId: String): Result<Unit, Throwable> =
        runCatching { syncManager.connectToDevice(endpointId) }

    fun stopSync() {
        syncManager.stopAll()
    }
}

sealed interface SyncDomainEvent {
    data class DeviceFound(val id: String, val name: String) : SyncDomainEvent
    data class StatusChanged(val status: String) : SyncDomainEvent
    data class DataSavedSuccess(val notes: List<NoteModel>) : SyncDomainEvent // Trả về Model
    data class Progress(val value: Int) : SyncDomainEvent
}