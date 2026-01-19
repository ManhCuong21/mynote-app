package com.example.data.syncdata.manager

import com.example.data.syncdata.model.SyncPackage
import java.io.File

interface SyncManager {
    fun startBroadcasting(deviceName: String)
    fun startSearching()
    fun connectToDevice(endpointId: String)
    fun stopAll()
    fun sendData(endpointId: String, data: SyncPackage)
    fun setListener(listener: SyncEventListener)
    fun sendFile(endpointId: String, file: File)
}