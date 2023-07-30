package com.example.presentation.main.listcategory

import androidx.fragment.app.viewModels
import com.example.core.base.BaseFragment
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.core.external.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListCategoryBinding
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ListCategoryFragment : BaseFragment(R.layout.fragment_list_category) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentListCategoryBinding by viewBinding()
    override val viewModel: ListCategoryViewModel by viewModels()

    private val categoryAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListCategoryAdapter()
    }

    override fun onStart() {
        super.onStart()
        viewModel.dispatch(ListCategoryAction.GetListCategory)
    }

    override fun setupViews() {
        setupRecyclerView()
        setupClickListener()
    }

    override fun bindViewModel() {
        viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
            when (event) {
                is ListCategorySingleEvent.GetListCategory.Success -> {
                    categoryAdapter.submitList(event.list)
                }

                is ListCategorySingleEvent.GetListCategory.Failed -> {}
            }
        }
    }

    private fun setupClickListener() = binding.apply {
        fabAddCategory.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToCategoryFragment)
        }
    }

    private fun setupRecyclerView() = binding.rvCategory.apply {
        setHasFixedSize(true)
        adapter = categoryAdapter
    }
}