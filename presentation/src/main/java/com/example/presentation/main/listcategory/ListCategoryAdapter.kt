package com.example.presentation.main.listcategory

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.ActionCategory
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.model.CategoryModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListCategoryBinding
import java.util.Random
import kotlin.math.roundToInt

class ListCategoryAdapter(
    private val onItemClicked: (ActionCategory, CategoryModel) -> Unit
) : ListAdapter<CategoryModel, ListCategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CategoryModel>() {
        override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean =
            oldItem.idCategory == newItem.idCategory

        override fun areContentsTheSame(
            oldItem: CategoryModel,
            newItem: CategoryModel
        ): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class ViewHolder(val binding: ItemListCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        @SuppressLint("DiscouragedPrivateApi")
        fun bind(item: CategoryModel) = binding.apply {
            imgCategory.loadImageDrawable(item.imageCategory)
            tvTitleCategory.text = item.titleCategory
            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            flCategory.setBackgroundColor(adjustAlpha(color, 0.6f))
            flImgCategory.setBackgroundColor(adjustAlpha(color, 0.3f))
            root.setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                popupMenu.inflate(R.menu.popup_menu_category)
                popupMenu.setOnMenuItemClickListener { menu ->
                    when (menu.itemId) {
                        R.id.btnUpdateCategory -> {
                            onItemClicked(ActionCategory.UPDATE_CATEGORY, item)
                            true
                        }

                        R.id.btnDeleteCategory -> {
                            onItemClicked(ActionCategory.DELETE_CATEGORY, item)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
                try {
                    val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    fieldMPopup.isAccessible = true
                    val mPopup = fieldMPopup.get(popupMenu)
                    mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                popupMenu.show()
            }
        }

        @ColorInt
        fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
            val alpha = (Color.alpha(color) * factor).roundToInt()
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }
    }
}