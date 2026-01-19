package com.example.data.syncdata.manager

import com.example.data.syncdata.model.SyncPackage

interface SyncEventListener {
    fun onSyncPackageReceived(syncPackage: SyncPackage)
    fun onStatusChanged(status: String)
    fun onProgressUpdate(progress: Int)
    fun onDeviceFound(id: String, name: String)
}