package com.focussholat.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.focussholat.databinding.FragmentHomeBinding
import com.focussholat.util.PrayerTimeCalculator
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import android.widget.TextView
import com.focussholat.R
import com.focussholat.data.entity.PrayerTime

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        viewModel.refresh()
    }

    private fun setupObservers() {
        viewModel.prayerTime.observe(viewLifecycleOwner) { prayerTime ->
            if (prayerTime != null) {
                binding.cardPrayerTimes.visibility = View.VISIBLE
                bindPrayerRows(prayerTime)
                binding.tvCityName.text = if (prayerTime.cityName.isNotEmpty())
                    prayerTime.cityName else "Lokasi GPS"
            } else {
                binding.cardPrayerTimes.visibility = View.GONE
            }
        }

        viewModel.currentTime.observe(viewLifecycleOwner) { time ->
            binding.tvCurrentTime.text = time
        }

        viewModel.isManualBlocking.observe(viewLifecycleOwner) { enabled ->
            binding.btnManualBlock.text = if (enabled) "Matikan Blokir Sekarang" else "Aktifkan Blokir Sekarang"
        }

        viewModel.nextPrayer.observe(viewLifecycleOwner) { info ->
            if (info != null) {
                binding.tvNextPrayerName.text = "Selanjutnya: ${info.displayName}"
                binding.tvNextPrayerTime.text = PrayerTimeCalculator.formatTime(info.timeString)
            } else {
                binding.tvNextPrayerName.text = "Waktu Sholat"
                binding.tvNextPrayerTime.text = "--:--"
            }
        }

        viewModel.isBlockingActive.observe(viewLifecycleOwner) { isActive ->
            binding.switchBlocking.isChecked = isActive
            if (isActive) {
                binding.tvBlockingStatus.text = "Pemblokiran Aktif"
                binding.ivBlockingStatus.setImageResource(com.focussholat.R.drawable.ic_shield_on)
            } else {
                binding.tvBlockingStatus.text = "Pemblokiran Nonaktif"
                binding.ivBlockingStatus.setImageResource(com.focussholat.R.drawable.ic_shield_off)
            }
        }

        viewModel.permissionRequired.observe(viewLifecycleOwner) { permType ->
            if (permType != null) {
                binding.switchBlocking.isChecked = false
                showPermissionDialog(permType)
                viewModel.onPermissionHandled()
            }
        }

        viewModel.isAccessibilityEnabled.observe(viewLifecycleOwner) { enabled ->
            if (!enabled) {
                binding.cardAccessibilityWarning.visibility = View.VISIBLE
            } else {
                binding.cardAccessibilityWarning.visibility = View.GONE
            }
        }

        viewModel.isUsageAccessEnabled.observe(viewLifecycleOwner) { enabled ->
            if (!enabled) {
                binding.cardUsageAccessWarning.visibility = View.VISIBLE
            } else {
                binding.cardUsageAccessWarning.visibility = View.GONE
            }
        }

        viewModel.blockCountToday.observe(viewLifecycleOwner) { count ->
            binding.tvBlockCountToday.text = count.toString()
        }

        viewModel.focusMinutesThisWeek.observe(viewLifecycleOwner) { minutes ->
            val hours = minutes / 60
            val mins = minutes % 60
            binding.tvFocusTime.text = if (hours > 0) "${hours}j ${mins}m" else "${mins}m"
        }
    }

    private fun bindPrayerRows(prayerTime: PrayerTime) {
        val rows = listOf(
            R.id.row_fajr to Pair("Subuh", prayerTime.fajr),
            R.id.row_dhuhr to Pair("Dzuhur", prayerTime.dhuhr),
            R.id.row_asr to Pair("Ashar", prayerTime.asr),
            R.id.row_maghrib to Pair("Maghrib", prayerTime.maghrib),
            R.id.row_isha to Pair("Isya", prayerTime.isha)
        )
        rows.forEach { (rowId, value) ->
            val row = binding.root.findViewById<View>(rowId)
            row.findViewById<TextView>(R.id.tvPrayerLabel).text = value.first
            row.findViewById<TextView>(R.id.tvPrayerTime).text =
                PrayerTimeCalculator.formatTime(value.second)
        }
    }

    private fun setupClickListeners() {
        binding.switchBlocking.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setBlockingEnabled(isChecked)
        }

        binding.btnManualBlock.setOnClickListener {
            val enabled = viewModel.isManualBlocking.value != true
            viewModel.setManualBlockingEnabled(enabled)
        }


        binding.btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnEnableUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        binding.btnRefreshPrayerTime.setOnClickListener {
            viewModel.fetchPrayerTimes()
        }

        binding.cardPrayerTimes.setOnClickListener {
            viewModel.fetchPrayerTimes()
        }

        binding.btnTestBlock.setOnClickListener {
            val intent = Intent(requireContext(), com.focussholat.ui.block.BlockScreenActivity::class.java)
            intent.putExtra("app_name", "Instagram")
            intent.putExtra("package_name", "com.instagram.android")
            startActivity(intent)
        }
    }

    private fun showPermissionDialog(permType: String) {
        val title: String
        val message: String
        val intent: Intent

        if (permType == "accessibility") {
            title = "Izin Aksesibilitas Diperlukan"
            message = "FocusSholat membutuhkan Izin Aksesibilitas untuk mendeteksi dan memblokir aplikasi yang dibuka saat waktu sholat.\n\nKlik 'Buka Pengaturan', lalu pilih FocusSholat dan aktifkan."
            intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        } else {
            title = "Izin Akses Penggunaan Diperlukan"
            message = "FocusSholat membutuhkan Izin Akses Data Penggunaan sebagai pendukung fitur pemblokiran.\n\nKlik 'Buka Pengaturan', lalu cari FocusSholat dan berikan izin."
            intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions(requireContext())
        // Re-read the database when returning from Settings so a newly fetched
        // city schedule is visible immediately.
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
