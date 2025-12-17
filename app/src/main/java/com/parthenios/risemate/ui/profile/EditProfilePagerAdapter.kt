package com.parthenios.risemate.ui.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EditProfilePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PersonalInfoEditFragment()
            1 -> CityEditFragment()
            2 -> KnownSkillsEditFragment()
            3 -> WantedSkillsEditFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}



