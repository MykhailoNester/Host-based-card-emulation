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
 *  Created by mykhailo.nester on 21/06/2022, 15:12
 */

package com.app.hcereader.nfc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.hcereader.MainActivity
import com.app.hcereader.R
import com.app.hcereader.databinding.FragmentNfcBinding

class NfcFragment : Fragment() {

    private var _binding: FragmentNfcBinding? = null
    private val binding get() = _binding!!
    private var adapter: NfcAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNfcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        if (adapter == null) {
            val nfcManager = requireActivity().getSystemService(Context.NFC_SERVICE) as NfcManager
            adapter = nfcManager.defaultAdapter
        }

        try {
            val intent = Intent(requireActivity(), MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(requireActivity(), 0, intent, 0)
            adapter?.enableForegroundDispatch(requireActivity(), nfcPendingIntent, null, null)
            binding.stateTextView.text = getString(R.string.nfc_enabled)
            Log.d("NfcFragment", "NFC enabled")

        } catch (ex: IllegalStateException) {
            binding.stateTextView.text = getString(R.string.nfc_enable_error)
            Log.e("NfcFragment", "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(requireActivity())
            Log.d("NfcFragment", "NFC disabled")

        } catch (ex: IllegalStateException) {
            Log.e("NfcFragment", "Error disabling NFC foreground dispatch", ex)
        }
    }
}
