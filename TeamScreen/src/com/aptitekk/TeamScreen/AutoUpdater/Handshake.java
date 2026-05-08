package com.aptitekk.TeamScreen.AutoUpdater;

import java.awt.List;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class Handshake
{
    
    private String input;
    private String salt;
    private CustomRNG rand;
    private String handShakeResult;
    
    public Handshake(String input, String salt)
    {
	this.input = input;
	this.salt = salt;
	String dec = this.strToDec(salt);
	if(dec.length() > 7)
	    dec = dec.substring(0, 7);
	this.rand = new CustomRNG(Integer.parseInt(dec));
	this.handShakeResult = this.generateHandShake();
    }
    
    private String bytesToBin(byte[] bytes)
    {
	StringBuilder bin = new StringBuilder();
	for(byte b : bytes)
	{
	    int val = b;
	    for(int i = 0; i < 8; i++)
	    {
		bin.append((val & 128) == 0 ? 0 : 1);
		val <<= 1;
	    }
	}
	
	return new String(bin);
    }
    
    private byte[] binToBytes(String bin)
    {
	byte[] bytes = new byte[bin.length() / 8];
	for(int i = 0; i < (bin.length() / 8); i++)
	{
	    bytes[i] = new BigInteger(bin.substring(i * 8, (i + 1) * 8), 2)
		    .byteValue();
	}
	return bytes;
    }
    
    private String strToDec(String str)
    {
	StringBuilder dec = new StringBuilder();
	for(byte b : str.getBytes())
	{
	    dec.append((int) b);
	}
	
	return new String(dec);
    }
    
    private byte[] CustomEncrypt(byte[] input)
    {
	String result = bytesToBin(input);
	
	int amount = rand.getInt(2, result.length());
	StringBuilder builder = new StringBuilder(result);
	for(int i = 0; i < amount; i++)
	{
	    builder.setCharAt(rand.getInt(0, result.length() - 1),
		    (char) (rand.getInt(0, 1) + "").getBytes()[0]);
	}
	
	return binToBytes(new String(builder));
    }
    
    private String bytesToHex(byte[] bytes)
    {
	char[] hexArray = "0123456789ABCDEF".toCharArray();
	char[] hexChars = new char[bytes.length * 2];
	for(int j = 0; j < bytes.length; j++)
	{
	    int v = bytes[j] & 0xFF;
	    hexChars[j * 2] = hexArray[v >>> 4];
	    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	}
	return new String(hexChars);
    }
    
    private String generateHandShake()
    {
	byte[] result = CustomEncrypt(this.input.getBytes());
	
	byte[] salt = this.salt.getBytes();
	
	byte[] combinedResult = new byte[result.length + salt.length];
	
	int halfResult = result.length / 2;
	int otherHalfResult = result.length - halfResult;
	
	for(int i = 0; i < halfResult; i++)
	{
	    combinedResult[i] = result[i];
	}
	
	for(int i = halfResult; i < halfResult + salt.length; i++)
	{
	    combinedResult[i] = salt[i - halfResult];
	}
	
	for(int i = halfResult + salt.length; i < halfResult + salt.length
		+ otherHalfResult; i++)
	{
	    combinedResult[i] = result[i - salt.length];
	}
	
	combinedResult = CustomEncrypt(CustomEncrypt(combinedResult));
	
	return this.bytesToHex(combinedResult);
    }
    
    public String getHandShakeResult()
    {
	return this.handShakeResult;
    }
    
}
