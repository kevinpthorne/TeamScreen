package com.aptitekk.TeamScreen.AutoUpdater;
/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public interface DLProgressListener
{
    
    public void progressChanged(int newProgress);
    
    public void downloadStarted();
    
    public void downloadFailed(String reason);
    
    public void downloadCompleted();
    
    public boolean shouldCancelDownload();
    
}
