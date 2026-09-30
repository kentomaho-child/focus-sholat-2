package com.focussholat.ui.block

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.focussholat.R
import com.focussholat.databinding.ActivityBlockScreenBinding
import com.focussholat.databinding.DialogEmergencyOverrideBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockScreenBinding
    private val viewModel: BlockScreenViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_IS_PRAYER_TIME = "is_prayer_time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: ""
        val isPrayerTime = intent.getBooleanExtra(EXTRA_IS_PRAYER_TIME, true)

        setupUI(blockedPackage, prayerName, isPrayerTime)
        setupObservers(blockedPackage)
        startCountdown(isPrayerTime)
    }

    private fun setupUI(blockedPackage: String, prayerName: String, isPrayerTime: Boolean) {
        if (isPrayerTime && prayerName.isNotEmpty()) {
            binding.tvBlockTitle.text = "Waktu Sholat $prayerName"
            binding.tvBlockMessage.text =
                "Saatnya sholat $prayerName. Letakkan HP Anda dan ambil wudhu 🕌"
        } else {
            binding.tvBlockTitle.text = "Fokus Mode Aktif"
            binding.tvBlockMessage.text =
                "Anda sedang dalam jadwal fokus. Tetap semangat! 💪"
        }

        // Try to get app name for blocked package
        try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(blockedPackage, 0)
            val appName = pm.getApplicationLabel(appInfo).toString()
            binding.tvBlockedAppName.text = "Aplikasi diblokir: $appName"
            binding.ivBlockedAppIcon.setImageDrawable(pm.getApplicationIcon(blockedPackage))
        } catch (e: Exception) {
            binding.tvBlockedAppName.text = "Aplikasi diblokir"
            binding.ivBlockedAppIcon.setImageResource(R.drawable.ic_app_default)
        }

        binding.btnBack.setOnClickListener {
            goHome()
        }

        binding.btnEmergencyOverride.setOnClickListener {
            showEmergencyOverrideDialog(blockedPackage)
        }
    }

    private fun setupObservers(blockedPackage: String) {
        viewModel.pinRequired.observe(this) { required ->
            // PIN handling done in dialog
        }

        viewModel.overrideCreated.observe(this) { success ->
            if (success) {
                Snackbar.make(
                    binding.root,
                    "Override darurat aktif. Aplikasi tersedia sementara.",
                    Snackbar.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCountdown(isPrayerTime: Boolean) {
        if (!isPrayerTime) return
        // Show countdown if in prayer time
        binding.tvCountdown.visibility = View.VISIBLE
        var seconds = 0
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                seconds++
                val min = seconds / 60
                val sec = seconds % 60
                binding.tvCountdown.text = String.format("Waktu fokus: %02d:%02d", min, sec)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun showEmergencyOverrideDialog(blockedPackage: String) {
        val dialogBinding = DialogEmergencyOverrideBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Override Darurat")
            .setView(dialogBinding.root)
            .setPositiveButton("Izinkan") { _, _ ->
                val durationStr = dialogBinding.etDuration.text.toString()
                val duration = durationStr.toIntOrNull() ?: 5

                if (viewModel.isPinEnabled()) {
                    showPinVerificationDialog(
                        onVerified = {
                            viewModel.createEmergencyOverride(blockedPackage, duration)
                        }
                    )
                } else {
                    viewModel.createEmergencyOverride(blockedPackage, duration)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPinVerificationDialog(onVerified: () -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_verify_pin, null)
        val etPin = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPinVerify)

        AlertDialog.Builder(this)
            .setTitle("Masukkan PIN")
            .setView(view)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val pin = etPin?.text.toString()
                if (viewModel.verifyPin(pin)) {
                    onVerified()
                } else {
                    Snackbar.make(binding.root, "PIN salah", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        goHome()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
