package com.focussholat.ui.schedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.focussholat.R
import com.focussholat.data.entity.CustomSchedule
import com.focussholat.databinding.DialogAddScheduleBinding
import com.focussholat.databinding.FragmentScheduleBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()

        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            onToggle = { schedule, enabled ->
                viewModel.toggleSchedule(schedule.id, enabled)
            },
            onEdit = { schedule ->
                showAddScheduleDialog(schedule)
            },
            onDelete = { schedule ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Jadwal")
                    .setMessage("Hapus jadwal \"${schedule.name}\"?")
                    .setPositiveButton("Hapus") { _, _ ->
                        viewModel.deleteSchedule(schedule)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )
        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSchedules.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
            adapter.submitList(schedules)
            binding.tvEmptyState.visibility =
                if (schedules.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddScheduleDialog(existing: CustomSchedule?) {
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Tambah Jadwal" else "Edit Jadwal")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        // Pre-fill if editing
        existing?.let { s ->
            dialogBinding.etScheduleName.setText(s.name)
            dialogBinding.tvStartTime.text = String.format("%02d:%02d", s.startHour, s.startMinute)
            dialogBinding.tvEndTime.text = String.format("%02d:%02d", s.endHour, s.endMinute)

            val days = s.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            dialogBinding.cbMonday.isChecked = 1 in days
            dialogBinding.cbTuesday.isChecked = 2 in days
            dialogBinding.cbWednesday.isChecked = 3 in days
            dialogBinding.cbThursday.isChecked = 4 in days
            dialogBinding.cbFriday.isChecked = 5 in days
            dialogBinding.cbSaturday.isChecked = 6 in days
            dialogBinding.cbSunday.isChecked = 7 in days
        } ?: run {
            dialogBinding.tvStartTime.text = "08:00"
            dialogBinding.tvEndTime.text = "09:00"
            // Default: all weekdays
            dialogBinding.cbMonday.isChecked = true
            dialogBinding.cbTuesday.isChecked = true
            dialogBinding.cbWednesday.isChecked = true
            dialogBinding.cbThursday.isChecked = true
            dialogBinding.cbFriday.isChecked = true
        }

        var startHour = existing?.startHour ?: 8
        var startMinute = existing?.startMinute ?: 0
        var endHour = existing?.endHour ?: 9
        var endMinute = existing?.endMinute ?: 0

        dialogBinding.btnPickStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                startHour = hour; startMinute = minute
                dialogBinding.tvStartTime.text = String.format("%02d:%02d", hour, minute)
            }, startHour, startMinute, true).show()
        }

        dialogBinding.btnPickEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                endHour = hour; endMinute = minute
                dialogBinding.tvEndTime.text = String.format("%02d:%02d", hour, minute)
            }, endHour, endMinute, true).show()
        }

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.etScheduleName.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.etScheduleName.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            val days = mutableListOf<Int>()
            if (dialogBinding.cbMonday.isChecked) days.add(1)
            if (dialogBinding.cbTuesday.isChecked) days.add(2)
            if (dialogBinding.cbWednesday.isChecked) days.add(3)
            if (dialogBinding.cbThursday.isChecked) days.add(4)
            if (dialogBinding.cbFriday.isChecked) days.add(5)
            if (dialogBinding.cbSaturday.isChecked) days.add(6)
            if (dialogBinding.cbSunday.isChecked) days.add(7)

            if (days.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root, "Pilih minimal 1 hari", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val schedule = CustomSchedule(
                id = existing?.id ?: 0,
                name = name,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                daysOfWeek = days.joinToString(","),
                isEnabled = existing?.isEnabled ?: true
            )

            if (existing == null) viewModel.addSchedule(schedule)
            else viewModel.updateSchedule(schedule)
            dialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
