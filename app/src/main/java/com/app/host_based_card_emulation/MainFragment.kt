/*
 *  ---license-start
 *  MykhailoNester / Host-based-card-emulation
 *  ---
 *  Copyright (C) 2022 Mykhailo Nester and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by mykhailo.nester on 23/05/2022, 17:33
 */

package com.app.host_based_card_emulation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.host_based_card_emulation.HceCardEmulationApduService.Companion.NFC_NDEF_KEY
import com.app.host_based_card_emulation.databinding.FragmentMainBinding
import timber.log.Timber

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        binding.startServiceBtn.setOnClickListener {
            initNFCFunction()
            binding.status.text = getString(R.string.service_enabled)
        }
        binding.stopServiceBtn.setOnClickListener {
            stopNfcService()
            binding.status.text = getString(R.string.service_disabled)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initNFCFunction() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
            binding.status.text = getString(R.string.hce_not_available)
            return
        }

        if (nfcAdapter?.isEnabled == true) {
            initNfcService()
        } else {
            showTurnOnNfcDialog()
        }
    }

    private fun initNfcService() {
        val inputText = binding.inputView.text.toString()

        val intent = Intent(requireContext(), HceCardEmulationApduService::class.java)
        intent.putExtra(NFC_NDEF_KEY, inputText)
        requireContext().startService(intent)

        val filter = IntentFilter(NFC_BROADCAST)
        requireContext().registerReceiver(nfcReceiver, filter)
    }

    private fun stopNfcService() {
        if (nfcAdapter?.isEnabled == true) {
            requireContext().stopService(Intent(requireContext(), HceCardEmulationApduService::class.java))
        }

        binding.status.text = getString(R.string.service_disabled)

        try {
            requireContext().unregisterReceiver(nfcReceiver)
        } catch (ex: Exception) {
            Timber.d("nfcReceiver not registered.")
        }
    }

    private fun showTurnOnNfcDialog() {
        val nfcDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.nfc_turn_on_title))
            .setMessage(getString(R.string.nfc_turn_on_message))
            .setPositiveButton(getString(R.string.nfc_turn_on_positive)) { dialog, _ ->
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.nfc_turn_on_negative)) { dialog, _ -> dialog.dismiss() }
            .create()
        nfcDialog.show()
    }

    private val nfcReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(NFC_EXTRA_NDEF_SENT)) {
                if (intent.getBooleanExtra(NFC_EXTRA_NDEF_SENT, false)) {
                    Toast.makeText(
                        requireContext(),
                        "NDEF was sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        const val NFC_BROADCAST = "com.app.host_based_card_emulation"
        const val NFC_EXTRA_NDEF_SENT = "nfc_ndef_sent"
    }
}