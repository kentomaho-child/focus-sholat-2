package com.focussholat.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.focussholat.R
import com.focussholat.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 4101
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.locationMode.observe(viewLifecycleOwner) { mode ->
            binding.radioGps.isChecked = mode == "GPS"
            binding.radioManual.isChecked = mode == "MANUAL"
            binding.layoutManualCity.visibility =
                if (mode == "MANUAL") View.VISIBLE else View.GONE
        }

        viewModel.cityName.observe(viewLifecycleOwner) { city ->
            binding.etCityName.setText(city)
        }

        viewModel.reminderMinutes.observe(viewLifecycleOwner) { minutes ->
            binding.sliderReminder.value = minutes.toFloat()
            binding.tvReminderValue.text = "$minutes menit sebelum"
        }

        viewModel.prayerDuration.observe(viewLifecycleOwner) { duration ->
            binding.sliderPrayerDuration.value = duration.toFloat()
            binding.tvPrayerDurationValue.text = "$duration menit"
        }

        viewModel.pinEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchPin.isChecked = enabled
        }

        viewModel.strictMode.observe(viewLifecycleOwner) { enabled ->
            binding.switchStrictMode.isChecked = enabled
        }

        viewModel.fajrEnabled.observe(viewLifecycleOwner) { binding.switchFajr.isChecked = it }
        viewModel.dhuhrEnabled.observe(viewLifecycleOwner) { binding.switchDhuhr.isChecked = it }
        viewModel.asrEnabled.observe(viewLifecycleOwner) { binding.switchAsr.isChecked = it }
        viewModel.maghribEnabled.observe(viewLifecycleOwner) { binding.switchMaghrib.isChecked = it }
        viewModel.ishaEnabled.observe(viewLifecycleOwner) { binding.switchIsha.isChecked = it }
    }

    private fun setupClickListeners() {
        binding.radioGps.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.setLocationMode("GPS", requireContext())
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST
                )
            }
        }

        binding.radioManual.setOnClickListener {
            viewModel.setLocationMode("MANUAL")
        }

        binding.btnSaveCity.setOnClickListener {
            val city = binding.etCityName.text.toString().trim()
            if (city.isEmpty()) {
                binding.etCityName.error = "Masukkan nama kota"
                return@setOnClickListener
            }
            viewModel.setCity(city, requireContext())
            Snackbar.make(binding.root, "Kota disimpan: $city. Jadwal sedang diperbarui.", Snackbar.LENGTH_SHORT).show()
        }

        binding.sliderReminder.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            binding.tvReminderValue.text = "$minutes menit sebelum"
            viewModel.setReminderMinutes(minutes)
        }

        binding.sliderPrayerDuration.addOnChangeListener { _, value, _ ->
            val duration = value.toInt()
            binding.tvPrayerDurationValue.text = "$duration menit"
            viewModel.setPrayerDuration(duration)
        }

        binding.switchPin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSetPinDialog()
            } else {
                showVerifyPinDialog(onVerified = {
                    viewModel.disablePin()
                })
            }
        }

        binding.switchStrictMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setStrictMode(isChecked)
        }

        binding.switchFajr.setOnCheckedChangeListener { _, checked -> viewModel.setPrayerEnabled("fajr", checked) }
        binding.switchDhuhr.setOnCheckedChangeListener { _, checked -> viewModel.setPrayerEnabled("dhuhr", checked) }
        binding.switchAsr.setOnCheckedChangeListener { _, checked -> viewModel.setPrayerEnabled("asr", checked) }
        binding.switchMaghrib.setOnCheckedChangeListener { _, checked -> viewModel.setPrayerEnabled("maghrib", checked) }
        binding.switchIsha.setOnCheckedChangeListener { _, checked -> viewModel.setPrayerEnabled("isha", checked) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            viewModel.setLocationMode("GPS", requireContext())
        }
    }

    private fun showSetPinDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_set_pin, null)
        val etPin = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPin)
        val etPinConfirm = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPinConfirm)

        AlertDialog.Builder(requireContext())
            .setTitle("Atur PIN")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val pin = etPin?.text.toString()
                val confirm = etPinConfirm?.text.toString()
                if (pin.length < 4) {
                    Snackbar.make(binding.root, "PIN minimal 4 digit", Snackbar.LENGTH_SHORT).show()
                    binding.switchPin.isChecked = false
                } else if (pin != confirm) {
                    Snackbar.make(binding.root, "PIN tidak cocok", Snackbar.LENGTH_SHORT).show()
                    binding.switchPin.isChecked = false
                } else {
                    viewModel.setPin(pin)
                }
            }
            .setNegativeButton("Batal") { _, _ ->
                binding.switchPin.isChecked = false
            }
            .show()
    }

    private fun showVerifyPinDialog(onVerified: () -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_verify_pin, null)
        val etPin = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPinVerify)

        AlertDialog.Builder(requireContext())
            .setTitle("Masukkan PIN")
            .setView(view)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val pin = etPin?.text.toString()
                if (viewModel.verifyPin(pin)) {
                    onVerified()
                } else {
                    Snackbar.make(binding.root, "PIN salah", Snackbar.LENGTH_SHORT).show()
                    binding.switchPin.isChecked = true
                }
            }
            .setNegativeButton("Batal") { _, _ ->
                binding.switchPin.isChecked = true
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
