package com.example.presentation.main.home.listnote

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.core.base.BaseFragment
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.core.external.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ListNoteFragment : BaseFragment(R.layout.fragment_list_note) {
    override val binding: FragmentListNoteBinding by viewBinding()
    override val viewModel: ListNoteViewModel by viewModels()

    private lateinit var categoryNote: CategoryUIModel
    private val listNoteAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListNoteAdapter()
    }

    override fun onStart() {
        super.onStart()
        viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
    }

    override fun setupViews() {
        setupRecyclerView()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                when (event) {
                    is ListNoteSingleEvent.GetListNote.Success -> {
                        listNoteAdapter.submitList(event.listNote)
                    }

                    is ListNoteSingleEvent.GetListNote.Failed -> {
                        Timber.e(event.error)
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