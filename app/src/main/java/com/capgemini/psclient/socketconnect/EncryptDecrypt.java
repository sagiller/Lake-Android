package com.capgemini.psclient.socketconnect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt{
	private static final byte[] SALT = {'A', 'd', 'o', 'b', 'e', ' ', 'P', 'h', 'o', 't', 'o', 's', 'h', 'o', 'p'}; // The salt value must match the values used in Photoshop DO NOT CHANGE
	private static final int ITERACTIONCOUNT = 1000;
	private static final int KEY_LENGTH = 24;
	private Cipher mECipher;
	private Cipher mDCipher;

	public EncryptDecrypt(String passPhrase) throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException{
		DESedeKeySpec keySpec = new DESedeKeySpec(PBKDF2.deriveKey(passPhrase.getBytes(), SALT, ITERACTIONCOUNT, KEY_LENGTH));
		Key key = new SecretKeySpec(keySpec.getKey(), "DESede");
		mECipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		mDCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		mECipher.init(Cipher.ENCRYPT_MODE, key, iv);
		mDCipher.init(Cipher.DECRYPT_MODE, key, iv);
	}


	public byte[] encrypt(byte[] inBytes) throws BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException{
		byte[] encryptedBytes = mECipher.doFinal(inBytes);
		return encryptedBytes;
	}


	public byte[] decrypt(byte[] strBytes) throws BadPaddingException, IllegalBlockSizeException, IOException{
		byte[] utf8 = mDCipher.doFinal(strBytes);
		return utf8;
	}
}
