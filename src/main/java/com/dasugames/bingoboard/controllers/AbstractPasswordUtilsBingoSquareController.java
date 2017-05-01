package com.dasugames.bingoboard.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

public class AbstractPasswordUtilsBingoSquareController extends AbstractBingoController  {
	
	private final SecureRandom random = new SecureRandom();
	
	protected String generateHashFromPasswordAndSalt(String plainPassword, String salt) {
		
		byte[] decodedSalt = DatatypeConverter.parseBase64Binary(salt);

		if (plainPassword == null || salt == null) {
			return null;
		}
		
		KeySpec spec = new PBEKeySpec("password".toCharArray(), decodedSalt, 65536, 128);
		SecretKeyFactory f;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e1) {
			// Should never happen
			e1.printStackTrace();
			return null;
		}
		byte[] hash = null;
		try {
			hash = f.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String encodedStringPassword = DatatypeConverter.printBase64Binary(hash);
		return encodedStringPassword;
	}
	
	protected String generateRandomSalt() {
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		String encoded = DatatypeConverter.printBase64Binary(salt);
		
		return encoded;
	}
	
	protected boolean isValidPassword(String rawPassword) {

		if (StringUtils.isBlank(rawPassword)) {
			return false;
		}
		
		return rawPassword.matches("[a-zA-Z0-9]+");
	}
	
	protected boolean isValidBoardname(String boardName) {
		if (StringUtils.isBlank(boardName)) {
			return false;
		}
		
		return boardName.matches("[a-zA-Z0-9]+");

	}

}
