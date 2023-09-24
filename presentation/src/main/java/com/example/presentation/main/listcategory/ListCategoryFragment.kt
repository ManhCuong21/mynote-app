package com.example.presentation.main.listcategory

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionCategory
import com.example.core.core.viewbinding.viewBinding
import com.example.core.core.lifecycle.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListCategoryBinding
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ListCategoryFragment : BaseFragment(R.layout.fragment_list_category) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentListCategoryBinding by viewBinding()
    override val viewModel: ListCategoryViewModel by viewModels()

    private val categoryAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListCategoryAdapter(
            onItemClicked = { action, categoryModel ->
                when (action) {
                    ActionCategory.UPDATE_CATEGORY -> {
                        mainNavigator.navigate(
                            MainNavigator.Direction.MainFragmentToUpdateCategoryFragment(
                                categoryModel
                            )
                        )
                    }

                    ActionCategory.DELETE_CATEGORY -> {
                        viewModel.dispatch(ListCategoryAction.DeleteCategory(categoryModel))
                    }

                    else -> {}
                }
            }
        )
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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is ListCategorySingleEvent.GetListCategorySuccess -> {
                            categoryAdapter.submitList(event.listCategory)
                            binding.rvCategory.isVisible = event.listCategory.isNotEmpty()
                            binding.lnEmptyCategory.isVisible = event.listCategory.isEmpty()
                        }

                        is ListCategorySingleEvent.DeleteCategorySuccess -> {
                            viewModel.dispatch(ListCategoryAction.GetListCategory)
                        }

                        is ListCategorySingleEvent.SingleEventFailed -> {
                            Timber.e(event.error)
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListener() = binding.apply {
        fabAddCategory.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToAddCategoryFragment)
        }
    }

    private fun setupRecyclerView() = binding.rvCategory.apply {
        setHasFixedSize(true)
        adapter = categoryAdapter
    }
}