package com.example.presentation.main.listcategory

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.ActionCategory
import com.example.core.core.external.loadImage
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListCategoryBinding

class ListCategoryAdapter(
    private val onItemClicked: (ActionCategory, CategoryUIModel) -> Unit
) : ListAdapter<CategoryUIModel, ListCategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CategoryUIModel>() {
        override fun areItemsTheSame(oldItem: CategoryUIModel, newItem: CategoryUIModel): Boolean =
            oldItem.idCategory == newItem.idCategory

        override fun areContentsTheSame(
            oldItem: CategoryUIModel,
            newItem: CategoryUIModel
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
        fun bind(item: CategoryUIModel) = binding.apply {
            imgCategory.loadImage(item.imageCategory)
            tvTitleCategory.text = item.titleCategory
            btnOptions.setOnClickListener {
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
    }
}