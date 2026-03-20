package com.example.hotel_app.presentation.ui.fragments

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentKeyBinding
import com.example.hotel_app.domain.repository.KeyAction
import com.example.hotel_app.presentation.ui.adapter.NfcKeyAdapter
import com.example.hotel_app.presentation.viewmodel.NfcViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class KeyFragment : Fragment(R.layout.fragment_key) {

    private val viewModel: NfcViewModel by viewModel()
    private var _binding: FragmentKeyBinding? = null
    private val binding get() = _binding!!

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>

    private val keyAdapter = NfcKeyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNfcForegroundDispatch()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentKeyBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.dashboardFragment, false)
                .setLaunchSingleTop(true)
                .build()
            findNavController().navigate(R.id.dashboardFragment, null, options)
        }

        binding.btnEmulateNfc.setOnClickListener {
            emulateNfcTouch()
        }

        setupRecyclerView()
        observeState()
    }

    private fun setupNfcForegroundDispatch() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        val intent = Intent(requireContext(), requireActivity().javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        
        pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, flags)

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
        
        intentFiltersArray = arrayOf(ndef, IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        techListsArray = arrayOf(arrayOf(NfcA::class.java.name), arrayOf(IsoDep::class.java.name))
    }

    private fun setupRecyclerView() {
        binding.rvKeys.apply {
            adapter = keyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.nfcKeys.collect { keys ->
                        keyAdapter.submitList(keys)
                        binding.btnEmulateNfc.isEnabled = keys.isNotEmpty()
                    }
                }

                launch {
                    viewModel.nfcEvent.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }
            }
        }
    }

    private fun emulateNfcTouch() {
        val activeKey = viewModel.nfcKeys.value.firstOrNull { it.isActive }
        if (activeKey != null) {
            viewModel.performAction(activeKey.id, KeyAction.OPEN_DOOR)
        } else {
            Toast.makeText(requireContext(), "No active keys found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(requireActivity(), pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
