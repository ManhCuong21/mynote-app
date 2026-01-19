package com.example.data.syncdata.manager.impl

import android.util.Log
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.data.datalocal.repository.CategoryRepository
import com.example.data.datalocal.repository.NoteRepository
import com.example.data.file.file.FileRepository
import com.example.data.syncdata.manager.SyncEventListener
import com.example.data.syncdata.manager.SyncManager
import com.example.data.syncdata.model.SyncPackage
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbySyncManagerImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val categoryRepository: CategoryRepository,
    private val noteRepository: NoteRepository,
    private val fileRepository: FileRepository,
    private val connectionsClient: ConnectionsClient,
    private val gson: Gson
) : SyncManager {

    private val serviceID = "com.myapp.note.P2P_SYNC"
    private var eventListener: SyncEventListener? = null
    private val incomingFilePayloads = mutableMapOf<Long, Payload>()
    override fun setListener(listener: SyncEventListener) {
        this.eventListener = listener
    }

    override fun startBroadcasting(deviceName: String) {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(
            deviceName,
            serviceID,
            connectionLifecycleCallback,
            options
        )
    }

    override fun startSearching() {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, options)
    }

    override fun connectToDevice(endpointId: String) {
        connectionsClient.requestConnection("NoteApp", endpointId, connectionLifecycleCallback)
    }

    override fun stopAll() {
        connectionsClient.stopAllEndpoints()
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
    }

    override fun sendData(endpointId: String, data: SyncPackage) {
        val json = gson.toJson(data)
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(json.toByteArray()))
    }

    override fun sendFile(endpointId: String, file: File) {
        val payload = Payload.fromFile(file)
        connectionsClient.sendPayload(endpointId, payload)
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    eventListener?.onStatusChanged("Đang bắt tay với ${info.endpointName}")
                }.addOnFailureListener { e ->
                    eventListener?.onStatusChanged("Lỗi bắt tay: ${e.message}")
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                eventListener?.onStatusChanged("Connected")
            }
        }

        override fun onDisconnected(endpointId: String) {
            eventListener?.onStatusChanged("Disconnected")
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            eventListener?.onDeviceFound(endpointId, info.endpointName)
        }

        override fun onEndpointLost(endpointId: String) {}
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val json = String(payload.asBytes()!!, Charsets.UTF_8)
                    handleJsonPackage(json)
                }
                Payload.Type.FILE -> {
                    incomingFilePayloads[payload.id] = payload
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            when (update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    val payload = incomingFilePayloads[update.payloadId]
                    if (payload != null && payload.type == Payload.Type.FILE) {
                        // Kích hoạt xử lý file ZIP khi nhận thành công
                        handleReceivedZip(payload)
                    }
                }
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    if (update.totalBytes > 0) {
                        val progress = (update.bytesTransferred * 100 / update.totalBytes).toInt()
                        eventListener?.onProgressUpdate(progress)
                    }
                }
            }
        }
    }

    private fun handleJsonPackage(json: String) {
        CoroutineScope(appCoroutineDispatchers.io).launch {
            try {
                val syncPackage = gson.fromJson(json, SyncPackage::class.java)

                // 1. Lưu Categories trước (để đảm bảo ràng buộc nếu có)
                syncPackage.categories.forEach { categoryRepository.insertCategory(it) }

                // 2. Lưu Notes (Lưu ý: lúc này fileMediaNote vẫn là path của máy cũ)
                syncPackage.notes.forEach { noteRepository.insertNote(it) }

                eventListener?.onSyncPackageReceived(syncPackage)
            } catch (e: Exception) {
                eventListener?.onStatusChanged("Lỗi xử lý JSON: ${e.message}")
            }
        }
    }

    private fun handleReceivedZip(payload: Payload) {
        CoroutineScope(appCoroutineDispatchers.io).launch {
            try {
                // DO NOT use payload.asFile()!!.asJavaFile() as it will cause an EACCES error
                val pfd = payload.asFile()?.asParcelFileDescriptor() ?: return@launch

                // Get the InputStream directly from the File Descriptor
                val inputStream = FileInputStream(pfd.fileDescriptor)

                // Point to the Pictures folder of the app
                val targetPicturesDir = fileRepository.createOrGetDirectory("")

                // Extract from Stream
                fileRepository.unzipFromStream(inputStream, targetPicturesDir)

                // Close the threads after completion
                inputStream.close()
                pfd.close()

                eventListener?.onStatusChanged("Đồng bộ đa phương tiện thành công!")
            } catch (e: Exception) {
                Log.e("SyncError", "Lỗi giải nén: ${e.message}")
                eventListener?.onStatusChanged("Lỗi giải nén: Hãy kiểm tra lại quyền.")
            }
        }
    }
}