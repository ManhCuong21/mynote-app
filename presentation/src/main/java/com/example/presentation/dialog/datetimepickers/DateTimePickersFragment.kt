package com.example.presentation.dialog.datetimepickers

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.core.external.AppConstants.FORMAT_TIME_DEFAULT_NOTIFICATION
import com.example.core.core.external.formatDate
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.core.external.collectIn
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
        if (noteModel.notificationModel != null) initialValue() else setupTextDateOfMonthDefault()
        setupRecyclerView()
        setupClickListener()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is DateTimePickersSingleEvent.UpdateTextDayOfMonth -> {
                            setupTextDateOfMonth(event.dateOfMonth)
                        }

                        is DateTimePickersSingleEvent.UpdateTextDayOfWeek -> {
                            setupTextDayOfWeek(event.dayOfWeek.sorted())
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
                                    requestCode = noteModel.idNote
                                )
                            } else {
                                NotificationUtils.setNotificationDayOfWeek(
                                    context = requireContext(),
                                    title = noteModel.titleNote,
                                    content = noteModel.contentNote,
                                    dayOfWeek = viewModel.stateFlow.value.dayOfWeek,
                                    hour = binding.timePicker.hour,
                                    minute = binding.timePicker.minute,
                                    requestCode = noteModel.idNote
                                )
                            }
                            mainNavigator.popBackStack()
                        }

                        is DateTimePickersSingleEvent.SaveNotification.Cancel -> {
                            NotificationUtils.cancelAlarm(requireContext(), noteModel.idNote)
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
        if (notification != null) {
            timePicker.hour = notification.hour
            timePicker.minute = notification.minute
            if (notification.dayOfMonth != null) {
                viewModel.dispatch(DateTimePickersAction.UpdateDayOfMonth(notification.dayOfMonth))
            } else if (notification.dayOfWeek != null) {
                notification.dayOfWeek!!.forEach {
                    viewModel.dispatch(DateTimePickersAction.UpdateDayOfWeek(it))
                }
            }
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

    private fun setupTextDateOfMonthDefault() = binding.apply {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        viewModel.dispatch(DateTimePickersAction.UpdateDayOfMonth(calendar.timeInMillis))
    }

    private fun setupTextDateOfMonth(date: Long) = binding.tvDateNotification.apply {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val calendarToday = Calendar.getInstance()
        val calendarNextDay = Calendar.getInstance()
        calendarNextDay.add(Calendar.DAY_OF_YEAR, 1)
        val textDate = when (calendar.get(Calendar.DAY_OF_YEAR)) {
            calendarToday.get(Calendar.DAY_OF_YEAR) -> getString(
                R.string.format_date_time_picker_today,
                formatDate(FORMAT_TIME_DEFAULT_NOTIFICATION, date)
            )

            calendarNextDay.get(Calendar.DAY_OF_YEAR) -> getString(
                R.string.format_date_time_picker_tomorrow,
                formatDate(FORMAT_TIME_DEFAULT_NOTIFICATION, date)
            )

            else -> formatDate(FORMAT_TIME_DEFAULT_NOTIFICATION, date)
        }
        text = textDate
    }

    private fun setupTextDayOfWeek(list: List<Int>) = binding.tvDateNotification.apply {
        var textDate = ""
        list.forEach {
            when (it) {
                Calendar.MONDAY -> textDate = plusText(textDate, "Mon")
                Calendar.TUESDAY -> textDate = plusText(textDate, "Tue")
                Calendar.WEDNESDAY -> textDate = plusText(textDate, "Wed")
                Calendar.THURSDAY -> textDate = plusText(textDate, "Thu")
                Calendar.FRIDAY -> textDate = plusText(textDate, "Fri")
                Calendar.SATURDAY -> textDate = plusText(textDate, "Sat")
                Calendar.SUNDAY -> textDate = plusText(textDate, "Sun")
            }
        }
        if (textDate.isNotEmpty()) text = textDate else setupTextDateOfMonthDefault()
    }

    private fun plusText(oldText: String, text: String): String {
        return if (oldText.isEmpty()) {
            oldText.plus("Every $text")
        } else oldText.plus(",$text")
    }

    companion object {
        private const val TAG = "DateTimePickersFragment.TAG"
    }
}