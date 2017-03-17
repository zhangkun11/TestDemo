package com.example.admin.myapplication.nfc;

import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

public class Card {
	public static MifareClassic mMifareClassic;
	public static MifareUltralight mMifareUltralight;
	public static NdefFormatable mNdefFormatable;
	public static Ndef mNdef;
	public static NfcV mNfcV;
	public static NfcF mNfcF;
	public static NfcB mNfcB;
	public static NfcA mNfcA;
	public static IsoDep mIsoDep;
	public static NfcBarcode mNfcBarcode;
}
