package com.example.presentation.main.home.listnote.image

import android.graphics.Bitmap
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.external.loadImageFile
import com.example.core.core.model.ItemChooseColor
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentEditImageBinding
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.adapter.NoteChooseColorAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class EditImageFragment : BaseFragment(R.layout.fragment_edit_image) {
    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentEditImageBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    private lateinit var bitmap: Bitmap
    private var selectedPosition = 0
    private val imagePath by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<EditImageFragmentArgs>().value.imagePath }

    private val listColor = listOf(
        ItemChooseColor(R.color.blackTitle, R.color.blackContent),
        ItemChooseColor(R.color.orangeTitle, R.color.orangeContent),
        ItemChooseColor(R.color.blueTitle, R.color.blueContent),
        ItemChooseColor(R.color.greenTitle, R.color.greenContent),
        ItemChooseColor(R.color.yellowTitle, R.color.yellowContent),
        ItemChooseColor(R.color.violetTitle, R.color.violetContent),
        ItemChooseColor(R.color.redTitle, R.color.redContent)
    )

    private val chooseColorAdapter by lazy {
        NoteChooseColorAdapter(
            onItemClicked = { position ->
                selectedPosition = position
                val color = ContextCompat.getColor(requireContext(), listColor[position].colorTitle)
                binding.drawingView.setColor(color)
            })
    }

    override fun setupViews() {
        imagePath?.let { binding.imageView.loadImageFile(it) }
        setupClickListeners()
        setupRecyclerView()
    }

    private fun setupClickListeners() = binding.apply {
        btnBack.setOnClickListener { mainNavigator.popBackStack() }
        btnPen.setOnClickListener {
            toggleColorPicker()
            val color =
                ContextCompat.getColor(requireContext(), listColor[selectedPosition].colorTitle)
            binding.drawingView.setColor(color)
        }
        btnStrokeSeekBar.setOnClickListener { toggleStrokeSeekBar() }
        btnSave.setOnClickListener {
            val resultBitmap = binding.drawingView.saveToBitmap(bitmap)
            saveBitmap(resultBitmap, imagePath)
            mainNavigator.popBackStack()
        }
        btnEraser.setOnClickListener {
            binding.drawingView.enableEraser(true)
        }
        strokeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.drawingView.setStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupRecyclerView() = binding.apply {
        rvChooseColor.apply {
            setHasFixedSize(true)
            adapter = chooseColorAdapter
            chooseColorAdapter.submitList(listColor)
        }
    }

    override fun bindViewModel() {
        //NO TODO here
    }

    private fun saveBitmap(bitmap: Bitmap, path: String?) {
        if (path == null) return
        try {
            FileOutputStream(path).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleColorPicker() = binding.apply {
        if (strokeSeekBar.isVisible) strokeSeekBar.isVisible = false
        rvChooseColor.isVisible = !rvChooseColor.isVisible
    }

    private fun toggleStrokeSeekBar() = binding.apply {
        if (rvChooseColor.isVisible) rvChooseColor.isVisible = false
        strokeSeekBar.isVisible = !strokeSeekBar.isVisible
    }
}