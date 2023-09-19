package com.example.presentation.dialog.datetimepickers

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentDateTimePickersBinding
import com.example.presentation.main.home.alarmclock.NotificationUtils
import com.example.presentation.navigation.MainNavigator
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class DateTimePickersFragment : BaseFragment(R.layout.fragment_date_time_pickers) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentDateTimePickersBinding by viewBinding()
    override val viewModel: DateTimePickersViewModel by viewModels()

    private val noteModel by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<DateTimePickersFragmentArgs>().value.noteModel }

    private val listDayOfWeek =
        listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )
    private val dayOfWeekPickersAdapter by lazy {
        DayOfWeekPickersAdapter(
            listDefault = noteModel.notificationModel?.dayOfWeek ?: listOf(),
            onItemClicked = {
                viewModel.dispatch(DateTimePickersAction.UpdateDayOfWeek(it))
            })
    }

    override fun setupViews() {
        initialValue()
        setupRecyclerView()
        setupClickListener()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is DateTimePickersSingleEvent.UpdateTextNotification -> {
                            binding.tvDateNotification.text = event.text
                        }

                        is DateTimePickersSingleEvent.SaveNotification.Success -> {
                            if (viewModel.stateFlow.value.dayOfMonth != null) {
                                NotificationUtils.setNotificationDayOfMonth(
                                    context = requireContext(),
                                    title = noteModel.titleNote,
                                    content = noteModel.contentNote,
                                    time = viewModel.stateFlow.value.dayOfMonth!!,
                                    hour = binding.timePicker.hour,
                                    minute = binding.timePicker.minute,
                                    requestCode = noteModel.idNote.toInt()
                                )
                            } else {
                                NotificationUtils.setNotificationDayOfWeek(
                                    context = requireContext(),
                                    title = noteModel.titleNote,
                                    content = noteModel.contentNote,
                                    dayOfWeek = viewModel.stateFlow.value.dayOfWeek,
                                    hour = binding.timePicker.hour,
                                    minute = binding.timePicker.minute,
                                    requestCode = noteModel.idNote.toInt()
                                )
                            }
                            mainNavigator.popBackStack()
                        }

                        is DateTimePickersSingleEvent.SaveNotification.Cancel -> {
                            NotificationUtils.cancelAlarm(
                                requireContext(),
                                noteModel.idNote.toInt()
                            )
                            mainNavigator.popBackStack()
                        }

                        is DateTimePickersSingleEvent.SaveNotification.Failed -> {
                            Timber.e(event.error)
                        }
                    }
                }
            }
        }
    }

    private fun initialValue() = binding.apply {
        val notification = noteModel.notificationModel
        if (notification?.idNotification != null) {
            timePicker.hour = notification.hour!!
            timePicker.minute = notification.minute!!
            if (notification.dayOfMonth != null) {
                viewModel.dispatch(DateTimePickersAction.UpdateDayOfMonth(notification.dayOfMonth))
            } else if (notification.dayOfWeek != null) {
                notification.dayOfWeek!!.forEach {
                    viewModel.dispatch(DateTimePickersAction.UpdateDayOfWeek(it))
                }
            }
        } else {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            viewModel.dispatch(
                DateTimePickersAction.UpdateDayOfMonth(calendar.timeInMillis, true)
            )
        }
    }

    private fun setupClickListener() = binding.apply {
        btnDatePicker.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.show(requireActivity().supportFragmentManager, TAG)
            datePicker.addOnPositiveButtonClickListener {
                dayOfWeekPickersAdapter.submitList(listDayOfWeek)
                viewModel.dispatch(DateTimePickersAction.UpdateDayOfMonth(it))
            }
        }
        btnSaveAlarm.setOnClickListener {
            viewModel.dispatch(
                DateTimePickersAction.SaveNotification(
                    noteModel,
                    timePicker.hour,
                    timePicker.minute
                )
            )
        }
        btnCancelAlarm.setOnClickListener {
            viewModel.dispatch(DateTimePickersAction.CancelNotification(noteModel))
        }
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }

    private fun setupRecyclerView() = binding.rvDayOfWeek.apply {
        adapter = dayOfWeekPickersAdapter
        dayOfWeekPickersAdapter.submitList(listDayOfWeek)
    }

    companion object {
        private const val TAG = "DateTimePickersFragment.TAG"
    }
}