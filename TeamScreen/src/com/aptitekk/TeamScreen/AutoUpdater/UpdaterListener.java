package com.aptitekk.TeamScreen.AutoUpdater;

/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public interface UpdaterListener
{
    
    /**
     * Used by the AutoUpdater to halt the program before opening the installer.
     * DO NOT CALL SYSTEM.EXIT();
     */
    public void haltProgram();
    
    public void logMessage(String message, LevelEnum level);
    
}
