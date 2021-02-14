package com.geekcommune.friendlybackup.integ;

import java.io.File;

import com.geekcommune.friendlybackup.config.BackupConfig;
import com.geekcommune.friendlybackup.main.FBNodeApp;
import com.geekcommune.integ.ClassloaderProxy;

/**
 * Wrapper around a backup client node (FBNodeApp instance) for use in integration tests.
 * @author bobbym
 *
 */
public class TestNode extends ClassloaderProxy {

    private BackupConfig backupConfig;
    private FBNodeApp service;
    
    public TestNode(String configFilePath) throws Exception {
        this.service = (FBNodeApp) invokeConstructor(
                "com.geekcommune.friendlybackup.main.FBNodeApp",
                new String[] { "java.lang.String" },
                new Object[] { configFilePath });
        backupConfig = (BackupConfig) invokeMethod(service, "getBackupConfig", new Class[0], new Object[0]);
    }

    public File getRestoreDirectory() throws Exception {
        return (File) invokeMethod(backupConfig, "getRestoreRootDirectory", new String[0], new Object[0]);
    }

    public void backup() throws Exception {
        invokeMethod(service, "backup", new Class[0], new Object[0]);
    }

    public void restore() throws Exception {
        invokeMethod(service, "restore", new Class[0], new Object[0]);
    }

    public File[] getBackupRootDirectories() throws Exception {
        return (File[]) invokeMethod(backupConfig, "getBackupRootDirectories", new String[0], new Object[0]);
    }

    public File getRestoreRootDirectory() throws Exception {
        return (File) invokeMethod(backupConfig, "getRestoreRootDirectory", new String[0], new Object[0]);
    }

    public File getDBFile() throws Exception {
        return (File) invokeMethod(backupConfig, "getDBFile", new String[0], new Object[0]);
    }

}
