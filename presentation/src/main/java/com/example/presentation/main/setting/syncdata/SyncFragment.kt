package com.example.presentation.main.setting.syncdata

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.core.base.BaseFragment
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentSyncBinding
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.main.setting.syncdata.adapter.DeviceAdapter
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SyncFragment : BaseFragment(R.layout.fragment_sync) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentSyncBinding by viewBinding()
    override val viewModel: SyncViewModel by viewModels()

    private val deviceAdapter by lazy {
        DeviceAdapter { endpointId ->
            // 1. Lưu lại ID vào State ngay khi người dùng nhấn Connect
            viewModel.setConnectingDevice(endpointId)

            // 2. Dispatch action để thực hiện kết nối kỹ thuật
            viewModel.dispatch(SyncAction.ConnectToDevice(endpointId))
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                // Quyền đã cấp, có thể bắt đầu Sync
            } else {
                showPermissionDeniedDialog()
            }
        }

    override fun setupViews() {
        checkAndRequestPermissions()
        setupRecyclerView()
        setupClickListener()
    }

    private fun setupClickListener() = binding.apply {
        btnStartBroadcasting.setOnClickListener {
            viewModel.dispatch(SyncAction.StartBroadcasting(Build.MODEL))
        }

        btnStartSearching.setOnClickListener {
            viewModel.dispatch(SyncAction.StartSearching)
        }

        btnSendData.setOnClickListener {
            val state = viewModel.stateFlow.value
            state.connectedEndpointId?.let { id ->
                viewModel.dispatch(SyncAction.SendData(id))
            } ?: run {
                showTextDialog {
                    textTitle("Thông báo")
                    textContent("Chưa xác định được thiết bị nhận. Vui lòng thử lại.")
                }
            }
        }
        btnBack.setOnClickListener { mainNavigator.popBackStack() }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViewModel() {
        viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
            when (event) {
                is SyncSingleEvent.SyncSuccess -> {
                    // Khi thành công, ẩn progress bar
                    binding.progressBarSync.visibility = android.view.View.GONE
                    showTextDialog {
                        textTitle("Thành công"); textContent("Đã nhận dữ liệu mới")
                        positiveButtonAction(getString(R.string.title_ok)) {}
                    }
                }

                is SyncSingleEvent.SyncFailed -> {
                    Timber.e(event.error)
                    showTextDialog {
                        textTitle(getString(R.string.error))
                        textContent(event.error?.message ?: "Unknown Error")
                        negativeButtonAction(getString(R.string.title_ok)) {}
                    }
                }

                is SyncSingleEvent.StatusChanged -> {
                    binding.tvStatus.text = event.status
                }
            }
        }

        viewModel.stateFlow.collectIn(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                binding.progressBarSync.visibility = android.view.View.VISIBLE
                binding.progressBarSync.progress = state.syncProgress

                if (state.syncProgress > 0) {
                    binding.tvStatus.text = "Đang truyền tải: ${state.syncProgress}%"
                }
            } else {
                binding.progressBarSync.visibility = android.view.View.GONE
            }

            // 2. Cập nhật danh sách thiết bị
            deviceAdapter.submitList(state.foundDevices)

            // 3. Logic nút bấm
            binding.btnSendData.isEnabled = state.isConnected
            if (state.isConnected) {
                binding.btnSendData.alpha = 1.0f
                // Chỉ cập nhật text này nếu không trong quá trình gửi (progress = 0)
                if (state.syncProgress == 0) binding.tvStatus.text = "Đã kết nối thành công!"
            } else {
                binding.btnSendData.alpha = 0.5f
            }
        }
    }

    private fun setupRecyclerView() = binding.rvDevices.apply {
        adapter = deviceAdapter
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        // Quyền Location
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // Quyền Nearby Devices cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }

    private fun showPermissionDeniedDialog() {
        showTextDialog {
            textTitle("Permissions Required")
            textContent("Sync function needs Nearby and Location permissions to work.")
            positiveButtonAction("Retry") { checkAndRequestPermissions() }
            negativeButtonAction("Cancel") {}
        }
    }
}