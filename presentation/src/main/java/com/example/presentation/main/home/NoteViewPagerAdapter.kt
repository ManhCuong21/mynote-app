package com.example.presentation.main.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.core.core.model.CategoryUIModel
import com.example.presentation.main.home.listnote.ListNoteFragment

class NoteViewPagerAdapter(
    fragment: Fragment,
    private val listCategory: List<CategoryUIModel>
) : FragmentStateAdapter(
    fragment.childFragmentManager,
    fragment.viewLifecycleOwner.lifecycle
) {
    override fun getItemCount(): Int = listCategory.size

    override fun createFragment(position: Int): Fragment {
        return ListNoteFragment.newInstance(category = listCategory[position])
    }
}