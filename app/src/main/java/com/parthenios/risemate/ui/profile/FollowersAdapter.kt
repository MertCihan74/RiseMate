package com.parthenios.risemate.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ItemFollowerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FollowersAdapter(
    private val onUserClick: (String) -> Unit
) : ListAdapter<String, FollowersAdapter.FollowerViewHolder>(FollowerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val binding = ItemFollowerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FollowerViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FollowerViewHolder(
        private val binding: ItemFollowerBinding,
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

    class FollowerDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
