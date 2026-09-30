package com.focussholat.ui.stats

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.focussholat.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.totalBlockCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalBlocked.text = count.toString()
        }

        viewModel.totalFocusMinutes.observe(viewLifecycleOwner) { minutes ->
            val m = minutes ?: 0
            val hours = m / 60
            val mins = m % 60
            binding.tvTotalFocusTime.text = if (hours > 0) "${hours}j ${mins}m" else "${mins}m"
        }

        viewModel.blockCountToday.observe(viewLifecycleOwner) { count ->
            binding.tvBlockedToday.text = count.toString()
        }

        viewModel.focusMinutesWeek.observe(viewLifecycleOwner) { minutes ->
            val hours = minutes / 60
            val mins = minutes % 60
            binding.tvFocusTimeWeek.text = if (hours > 0) "${hours}j ${mins}m" else "${mins}m"
        }

        viewModel.recentEvents.observe(viewLifecycleOwner) { events ->
            val sb = StringBuilder()
            events.take(10).forEach { event ->
                val date = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(event.blockedAt))
                sb.appendLine("• ${event.appName} — $date")
            }
            binding.tvRecentAttempts.text = sb.toString().ifEmpty { "Belum ada percobaan" }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
