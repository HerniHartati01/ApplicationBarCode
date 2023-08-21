package com.example.applicationbarcode.data.repo

import com.example.applicationbarcode.domain.repo.MainRepo
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainRepoImpl @Inject constructor(
    private val scanner: GmsBarcodeScanner,
) : MainRepo {

    override fun startScanning(): Flow<String?>{
        return callbackFlow {
            scanner.startScan()
                .addOnSuccessListener {
                    launch {
                        send(getDetails(it))
                    }
                }.addOnFailureListener {
                    it.printStackTrace()
                }
            awaitClose {  }
        }
    }

    private fun getDetails(barcode: Barcode): String {
        val dataLines = barcode.rawValue?.split("\n") ?: emptyList()

        val sumber = dataLines.find { it.startsWith("sumber :") }?.substringAfter("sumber :")?.trim()
        val transaksi = dataLines.find { it.startsWith("transaksi :") }?.substringAfter("transaksi :")?.trim()
        val merchant = dataLines.find { it.startsWith("merchant :") }?.substringAfter("merchant :")?.trim()
        val nominalTransaksi = dataLines.find { it.startsWith("transaksi :") }?.substringAfter("transaksi :")?.trim()

        return when (barcode.valueType) {
            Barcode.TYPE_TEXT -> {
                "Sumber: $sumber\n" + "ID Transaksi: $transaksi\n" + "Merchant: $merchant\n" + "Nominal Transaksi: $nominalTransaksi"
            }
            else -> {
                barcode.rawValue ?: "Couldn't determine"
            }
        }
    }

}