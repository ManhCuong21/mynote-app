package com.example.presentation.category

import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionCategory
import com.example.core.core.external.loadImage
import com.example.core.core.model.ItemCategory
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.core.external.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentCategoryBinding
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoryFragment : BaseFragment(R.layout.fragment_category) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentCategoryBinding by viewBinding()
    override val viewModel: CategoryViewModel by viewModels()

    private val actionCategory by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<CategoryFragmentArgs>().value.actionCategory }

    private val categoryModel by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<CategoryFragmentArgs>().value.category }

    private val listCategory = listOf(
        ItemCategory(title = "Ex", image = R.drawable.icon_ex),
        ItemCategory(title = "Default", image = R.drawable.icon_default),
        ItemCategory(title = "Baby", image = R.drawable.icon_baby),
        ItemCategory(title = "Basketball", image = R.drawable.icon_basketball),
        ItemCategory(title = "Book", image = R.drawable.icon_book),
        ItemCategory(title = "Building", image = R.drawable.icon_building),
        ItemCategory(title = "Cake", image = R.drawable.icon_cake),
        ItemCategory(title = "Camping", image = R.drawable.icon_camping),
        ItemCategory(title = "Car", image = R.drawable.icon_car),
        ItemCategory(title = "Cat", image = R.drawable.icon_cat),
        ItemCategory(title = "Clock", image = R.drawable.icon_clock),
        ItemCategory(title = "Flower", image = R.drawable.icon_flower),
        ItemCategory(title = "Gift", image = R.drawable.icon_gift),
        ItemCategory(title = "Gym", image = R.drawable.icon_gym),
        ItemCategory(title = "Head phone", image = R.drawable.icon_headphone),
        ItemCategory(title = "Hearth", image = R.drawable.icon_hearth),
        ItemCategory(title = "Home", image = R.drawable.icon_home),
        ItemCategory(title = "Map", image = R.drawable.icon_map),
        ItemCategory(title = "Morning", image = R.drawable.icon_morning),
        ItemCategory(title = "Movie", image = R.drawable.icon_movie),
        ItemCategory(title = "Night", image = R.drawable.icon_night),
        ItemCategory(title = "Office", image = R.drawable.icon_office),
        ItemCategory(title = "Phone", image = R.drawable.icon_phone),
        ItemCategory(title = "Pill", image = R.drawable.icon_pill),
        ItemCategory(title = "Pizza", image = R.drawable.icon_pizza),
        ItemCategory(title = "Shopping", image = R.drawable.icon_shopping),
        ItemCategory(title = "Stopwatch", image = R.drawable.icon_stopwatch),
        ItemCategory(title = "Study", image = R.drawable.icon_study),
        ItemCategory(title = "Train", image = R.drawable.icon_train),
        ItemCategory(title = "Travel", image = R.drawable.icon_travel),
        ItemCategory(title = "Wallet", image = R.drawable.icon_wallet),
        ItemCategory(title = "Water", image = R.drawable.icon_water)
    )

    private val categoryAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CategoryAdapter(
            isDarkMode = sharedPrefersManager.darkModeTheme,
            defaultPosition = getDefaultPosition(),
            onItemClicked = { item ->
                binding.edtCategoryName.editText?.setText(item.title)
                binding.imgItemCategory.loadImage(item.image)
                viewModel.dispatch(CategoryAction.TitleCategoryChanged(item.title))
                viewModel.dispatch(CategoryAction.ImageCategoryChanged(item.image))
            })
    }

    override fun setupViews() {
        setupInitialValues()
        setupInitCategory()
        setupRecyclerView()
        setupClickListener()
        setupCategoryTitleInput()
    }

    private fun setupRecyclerView() = binding.rvItemCategory.run {
        setHasFixedSize(true)
        adapter = categoryAdapter
        categoryAdapter.submitList(listCategory)
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is AddCategorySingleEvent.SaveCategory.Success -> {
                            binding.edtCategoryName.editText?.text = null
                            mainNavigator.popBackStack()
                        }

                        is AddCategorySingleEvent.SaveCategory.Failed -> {
                            Toast.makeText(
                                requireContext(),
                                "Save category failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupInitialValues() = binding.apply {
        val state = viewModel.stateFlow.value
        edtCategoryName.editText?.setText(state.title)
        imgItemCategory.setImageResource(state.image)
        viewModel.dispatch(CategoryAction.TitleCategoryChanged(state.title))
        viewModel.dispatch(CategoryAction.ImageCategoryChanged(state.image))
    }

    private fun setupInitCategory() = binding.apply {
        categoryModel?.let {
            edtCategoryName.editText?.setText(it.titleCategory)
            imgItemCategory.setImageResource(it.imageCategory)
            viewModel.dispatch(CategoryAction.ImageCategoryChanged(it.imageCategory))
            viewModel.dispatch(CategoryAction.TitleCategoryChanged(it.titleCategory))
        }
    }

    private fun setupClickListener() = binding.apply {
        btnSaveCategory.setOnClickListener {
            val title = edtCategoryName.editText?.text
            if (title.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Title category is not empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (actionCategory == ActionCategory.UPDATE_CATEGORY) {
                    categoryModel?.let { viewModel.dispatch(CategoryAction.UpdateCategory(it)) }
                } else {
                    viewModel.dispatch(CategoryAction.InsertCategory)
                }
            }
        }
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }


    private fun setupCategoryTitleInput() = binding.edtCategoryName.run {
        editText?.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(
                    CategoryAction.TitleCategoryChanged(
                        text?.toString().orEmpty()
                    )
                )
            }
        }
    }

    private fun getDefaultPosition(): Int {
        var position = 0
        if (categoryModel != null) {
            listCategory.forEachIndexed { index, category ->
                run {
                    if (category.image == categoryModel?.imageCategory) {
                        position = index
                    }
                }
            }
        }
        return position
    }
}