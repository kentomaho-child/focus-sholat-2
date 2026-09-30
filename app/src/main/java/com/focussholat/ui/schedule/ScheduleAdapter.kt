package com.focussholat.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.focussholat.data.entity.CustomSchedule
import com.focussholat.databinding.ItemScheduleBinding

class ScheduleAdapter(
    private val onToggle: (CustomSchedule, Boolean) -> Unit,
    private val onEdit: (CustomSchedule) -> Unit,
    private val onDelete: (CustomSchedule) -> Unit
) : ListAdapter<CustomSchedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: CustomSchedule) {
            binding.tvScheduleName.text = schedule.name
            binding.tvTimeRange.text = String.format(
                "%02d:%02d - %02d:%02d",
                schedule.startHour, schedule.startMinute,
                schedule.endHour, schedule.endMinute
            )

            val dayNames = mapOf(
                1 to "Sen", 2 to "Sel", 3 to "Rab", 4 to "Kam",
                5 to "Jum", 6 to "Sab", 7 to "Min"
            )
            val enabledDays = schedule.daysOfWeek.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .mapNotNull { dayNames[it] }
            binding.tvDays.text = enabledDays.joinToString(", ")

            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = schedule.isEnabled
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(schedule, checked)
            }

            binding.btnEdit.setOnClickListener { onEdit(schedule) }
            binding.btnDelete.setOnClickListener { onDelete(schedule) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<CustomSchedule>() {
        override fun areItemsTheSame(o: CustomSchedule, n: CustomSchedule) = o.id == n.id
        override fun areContentsTheSame(o: CustomSchedule, n: CustomSchedule) = o == n
    }
}
