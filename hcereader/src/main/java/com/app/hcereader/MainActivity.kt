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
 *  Created by mykhailo.nester on 23/05/2022, 17:53
 */

package com.app.hcereader

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.hcereader.databinding.ActivityMainBinding
import com.app.hcereader.nfc.NdefParser
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var adapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun enableNfcForegroundDispatch() {
        if (adapter == null) {
            val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
            adapter = nfcManager.defaultAdapter
        }

        try {
            val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
            binding.stateTextView.text = getString(R.string.nfc_enabled)
            Timber.d("NFC enabled")

        } catch (ex: IllegalStateException) {
            binding.stateTextView.text = getString(R.string.nfc_enable_error)
            Timber.e(ex, "Error enabling NFC foreground dispatch")
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
            Timber.d("NFC disabled")

        } catch (ex: IllegalStateException) {
            Timber.e("Error disabling NFC foreground dispatch", ex)
        }
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            checkNdefMessage(intent)
        }
    }

    private fun checkNdefMessage(intent: Intent) {
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
            val messages = rawMessages.map { it as NdefMessage }
            parseNdefMessages(messages)
            intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }
    }

    private fun parseNdefMessages(messages: List<NdefMessage>) {
        if (messages.isEmpty()) {
            return
        }

        val builder = StringBuilder()
        val records = NdefParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str)
        }

        val message = builder.toString()
        if (message.isNotEmpty()) {
            binding.messageValue.text = message
        } else {
            Timber.d("Received empty NDEFMessage")
        }
    }
}