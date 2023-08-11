package com.example.presentation.main.home.listnote

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionNote
import com.example.core.core.file.image.ImageFile
import com.example.core.core.file.record.RecordFile
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.core.external.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListNoteBinding
import com.example.presentation.dialog.list.showListDialog
import com.example.presentation.main.home.toListDialogItem
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ListNoteFragment : BaseFragment(R.layout.fragment_list_note) {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var imageFile: ImageFile

    @Inject
    lateinit var recordFile: RecordFile

    override val binding: FragmentListNoteBinding by viewBinding()
    override val viewModel: ListNoteViewModel by viewModels()

    private lateinit var categoryNote: CategoryUIModel
    private val listNoteAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListNoteAdapter(
            fragmentActivity = requireActivity(),
            imageFile = imageFile,
            recordFile = recordFile,
            format24Hour = sharedPrefersManager.format24Hour,
            onItemClicked = { action, noteModel ->
                when (action) {
                    ActionNote.SHOW_ON_MAP -> {}
                    ActionNote.UPDATE_NOTE -> {
                        mainNavigator.navigate(
                            MainNavigator.Direction.MainFragmentToUpdateNoteFragment(noteModel = noteModel)
                        )
                    }

                    ActionNote.CHANGE_CATEGORY -> {
                        showListDialog {
                            val listCategory = viewModel.stateFlow.value.listCategory
                            textTitle(getString(R.string.title_dialog_change_category))
                            listItem(listCategory.map { it.toListDialogItem() })
                            positionSelected(listCategory.indexOf(noteModel.categoryNote))
                            positiveButtonAction(getString(R.string.title_ok)) { indexItem ->
                                listCategory[indexItem].let {
                                    viewModel.dispatch(
                                        ListNoteAction.ChangeCategoryNote(
                                            noteUIModel = noteModel,
                                            category = it
                                        )
                                    )
                                }
                            }
                            negativeButtonAction(getString(R.string.button_cancel)) {}
                        }
                    }

                    ActionNote.DELETE_NOTE -> {
                        viewModel.dispatch(ListNoteAction.DeleteNote(noteModel))
                    }

                    else -> {}
                }
            })
    }

    override fun onResume() {
        super.onResume()
        viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
    }

    override fun setupViews() {
        setupRecyclerView()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is ListNoteSingleEvent.GetListNoteSuccess -> {
                            listNoteAdapter.submitList(event.listNote)
                        }

                        is ListNoteSingleEvent.UpdateNote -> {
                            viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
                        }

                        is ListNoteSingleEvent.DeleteNoteSuccess -> {
                            viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
                        }

                        is ListNoteSingleEvent.SingleEventFailed -> {
                            Timber.e(event.error)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvNote.apply {
        smoothScrollToPosition(0)
        adapter = listNoteAdapter
    }


    companion object {
        fun newInstance(category: CategoryUIModel) = ListNoteFragment().apply {
            categoryNote = category
        }
    }
}