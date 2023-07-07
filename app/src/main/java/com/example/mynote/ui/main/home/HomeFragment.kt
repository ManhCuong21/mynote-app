package com.example.mynote.ui.main.home

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mynote.R
import com.example.mynote.base.BaseFragment
import com.example.mynote.core.navigation.MainNavigator
import com.example.mynote.core.viewbinding.viewBinding
import com.example.mynote.databinding.FragmentHomeBinding
import com.example.mynote.domain.mapper.toListDialogItem
import com.example.mynote.domain.model.CategoryModel
import com.example.mynote.ui.dialog.list.showListDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {
    override val binding: FragmentHomeBinding by viewBinding()

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var mainNavigator: MainNavigator

    private val categoryAdapter by lazy(LazyThreadSafetyMode.NONE) {
        HomeListCategoryAdapter(onItemClicked = { item ->

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.dispatch(HomeAction.GetListCategory)
    }

    override fun setupViews() {
        setupRecyclerView()
        setupClickListener()
    }

    private fun setupClickListener() = binding.apply {
        fabAddNote.setOnClickListener {
            val listCategory =
                viewModel.stateFlow.value.listCategory.filterIndexed { index, _ -> index != 0 }
            if (listCategory.isNotEmpty()) {
                showListDialog {
                    textTitle(getString(R.string.title_dialog_category))
                    listItem(listCategory.map { it.toListDialogItem() })
                    positiveAction(getString(R.string.title_ok)) { indexItem ->
                        listCategory[indexItem].id?.let {
                            mainNavigator.navigate(
                                MainNavigator.Direction.MainFragmentToAddNoteFragment(
                                    idCategoryNote = it
                                )
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please add category before", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.singleEventFlow.collect { event ->
                when (event) {
                    is HomeSingleEvent.GetListCategory.Success -> {
                        val list =
                            arrayListOf(CategoryModel(title = "All", image = R.drawable.icon_clock))
                        list.addAll(event.list)
                        categoryAdapter.submitList(list)
                        viewModel.dispatch(HomeAction.ListCategoryChanged(list))
                    }

                    is HomeSingleEvent.GetListCategory.Failed -> {
                        Timber.e(event.error)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvCategory.run {
        setHasFixedSize(true)
        adapter = categoryAdapter
    }
}