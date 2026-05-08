package com.aptitekk.TeamScreen.AutoUpdater;
/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class CustomRNG
{
    private long seed = 0;
    
    public CustomRNG(long seed)
    {
	this.setSeed(seed);
    }
    
    public void setSeed(long seed)
    {
	this.seed = Math.abs(seed) % 9999999 + 1;
	getInt(0, 9999999);
    }
    
    public int getInt(int min, int max)
    {
	this.seed = (this.seed * 125) % 2796203;
	return (int) this.seed % (max - min + 1) + min;
    }
}
