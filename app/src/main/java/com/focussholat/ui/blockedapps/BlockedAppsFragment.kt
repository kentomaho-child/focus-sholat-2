package com.focussholat.ui.blockedapps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.focussholat.databinding.FragmentBlockedAppsBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedAppsFragment : Fragment() {

    private var _binding: FragmentBlockedAppsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BlockedAppsViewModel by viewModels()
    private lateinit var adapter: InstalledAppsAdapter
    private var currentTab = 0 // 0 = blocked, 1 = whitelist

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupTabs()
        setupObservers()
        viewModel.loadInstalledApps(requireContext())
    }

    private fun setupRecyclerView() {
        adapter = InstalledAppsAdapter(
            onBlockToggle = { app, isBlocked ->
                if (currentTab == 0) {
                    viewModel.toggleBlocked(app.packageName, app.appName, isBlocked)
                } else {
                    viewModel.toggleWhitelisted(app.packageName, app.appName, isBlocked)
                }
            }
        )
        binding.recyclerApps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerApps.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterApps(newText ?: "")
                return true
            }
        })
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateListForTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateListForTab() {
        if (currentTab == 0) {
            viewModel.blockedAppsDisplay.value?.let { adapter.submitList(it) }
        } else {
            viewModel.whitelistedAppsDisplay.value?.let { adapter.submitList(it) }
        }
    }

    private fun setupObservers() {
        viewModel.blockedAppsDisplay.observe(viewLifecycleOwner) { apps ->
            if (currentTab == 0) adapter.submitList(apps)
            binding.tvBlockedCount.text = "Diblokir: ${apps.count { it.isChecked }}"
        }

        viewModel.whitelistedAppsDisplay.observe(viewLifecycleOwner) { apps ->
            if (currentTab == 1) adapter.submitList(apps)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.recyclerApps.visibility = if (loading) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
