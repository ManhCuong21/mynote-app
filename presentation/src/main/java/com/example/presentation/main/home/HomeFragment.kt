package com.example.presentation.main.home

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.core.base.BaseFragment
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.core.external.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentHomeBinding
import com.example.presentation.dialog.list.showListDialog
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {
    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentHomeBinding by viewBinding()

    override val viewModel: HomeViewModel by viewModels()

    private var viewPagerAdapter: NoteViewPagerAdapter? = null

    private val categoryAdapter by lazy(LazyThreadSafetyMode.NONE) {
        HomeListCategoryAdapter(
            isDarkMode = sharedPrefersManager.darkModeTheme,
            onItemClicked = { position ->
                binding.viewPagerNote.currentItem = position
            })
    }

    override fun onStart() {
        super.onStart()
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
                    positiveButtonAction(getString(R.string.title_ok)) { indexItem ->
                        listCategory[indexItem].let {
                            mainNavigator.navigate(
                                MainNavigator.Direction.MainFragmentToNoteFragment(
                                    category = it
                                )
                            )
                        }
                    }
                    negativeButtonAction(getString(R.string.button_cancel)) {}
                }
            } else {
                Toast.makeText(requireContext(), "Please add category before", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                when (event) {
                    is HomeSingleEvent.GetListCategory.Success -> {
                        val list =
                            arrayListOf(
                                CategoryUIModel(
                                    idCategory = -1,
                                    titleCategory = "All",
                                    imageCategory = R.drawable.icon_clock
                                )
                            )
                        list.addAll(event.list)
                        categoryAdapter.submitList(list)
                        setUpViewPager(list)
                        viewModel.dispatch(HomeAction.ListCategoryChanged(list))
                    }

                    is HomeSingleEvent.GetListCategory.Failed -> {
                        Timber.e(event.error)
                    }
                }
            }
        }
    }

    private fun setUpViewPager(list: List<CategoryUIModel>) = binding.apply {
        viewPagerAdapter = NoteViewPagerAdapter(this@HomeFragment, list)
        viewPagerNote.adapter = viewPagerAdapter
        viewPagerNote.unregisterOnPageChangeCallback(onPageChangeCallback)
        viewPagerNote.registerOnPageChangeCallback(onPageChangeCallback)
    }

    private val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.rvCategory.smoothScrollToPosition(position)
                categoryAdapter.setCurrentTab(position)
            }
        }

    private fun setupRecyclerView() = binding.rvCategory.run {
        setHasFixedSize(true)
        adapter = categoryAdapter
    }
}