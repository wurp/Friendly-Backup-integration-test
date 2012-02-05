package com.geekcommune.friendlybackup.integ;

import junit.framework.Assert;

import com.geekcommune.friendlybackup.communication.BackupMessageUtil;
import com.geekcommune.friendlybackup.config.SwingUIKeyDataSource;
import com.geekcommune.friendlybackup.main.Backup;
import com.geekcommune.friendlybackup.main.FBNodeApp;
import com.geekcommune.friendlybackup.main.MockBackupMessageUtil;
import com.geekcommune.friendlybackup.main.Restore;

public class BackupRestoreNoMessagingTest extends IntegrationTestCase {
    private FBNodeApp app;

    public void testBackupRestoreFakeMessageUtil() throws Exception {

        Backup backup = new Backup(app.getBackupConfig());
        backup.doBackup();
        
        Restore restore = new Restore(app.getBackupConfig());
        restore.doRestore();

        Assert.assertTrue(compareDirectories(
                app.getBackupConfig().getBackupRootDirectories()[0],
                app.getBackupConfig().getRestoreRootDirectory()));
    }

    public void setUp() throws Exception {
        System.setProperty(FBNodeApp.BACKUP_CONFIG_PROP_KEY, "test/integ/happy1/config/BackupConfig.properties");
        app = new FBNodeApp();
        app.wire();

        char[] passphrase = "password".toCharArray();
        ((SwingUIKeyDataSource)app.getBackupConfig().getKeyDataSource()).
            setPassphrase(passphrase);

        BackupMessageUtil.setInstance(new MockBackupMessageUtil(app.getBackupConfig()));
        
        cleanDirectory(app.getBackupConfig().getRestoreRootDirectory());
    }
}
