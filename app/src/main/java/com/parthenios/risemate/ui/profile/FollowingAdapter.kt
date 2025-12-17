package com.parthenios.risemate.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ItemFollowingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FollowingAdapter(
    private val onUserClick: (String) -> Unit
) : ListAdapter<String, FollowingAdapter.FollowingViewHolder>(FollowingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val binding = ItemFollowingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FollowingViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FollowingViewHolder(
        private val binding: ItemFollowingBinding,
        private val onUserClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(userId: String) {
            loadUserData(userId)
            binding.root.setOnClickListener { onUserClick(userId) }
        }

        private fun loadUserData(userId: String) {
            val firestore = FirebaseFirestore.getInstance()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userDoc = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        val firstName = userDoc.getString("firstName") ?: ""
                        val lastName = userDoc.getString("lastName") ?: ""
                        val photoUrl = userDoc.getString("photoUrl") ?: ""

                        withContext(Dispatchers.Main) {
                            binding.tvUserName.text = "$firstName $lastName"

                            if (photoUrl.isNotEmpty()) {
                                Glide.with(binding.root.context)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_profile)
                                    .into(binding.ivUserPhoto)
                            } else {
                                binding.ivUserPhoto.setImageResource(R.drawable.ic_profile)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.tvUserName.text = "Kullanıcı"
                        binding.ivUserPhoto.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
        }
    }

    class FollowingDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
