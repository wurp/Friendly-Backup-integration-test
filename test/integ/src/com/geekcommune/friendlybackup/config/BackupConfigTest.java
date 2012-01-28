package com.geekcommune.friendlybackup.config;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.geekcommune.friendlybackup.main.App;
import com.geekcommune.identity.SecretIdentity;

public class BackupConfigTest extends TestCase {
    private App app;

    public void setUp() throws Exception {
        app = new App();
        app.wire("test/integ/happy2/config1/BackupConfig.properties");
    }
    
    public void testKeyInitialization() throws Exception {
        SecretIdentity authOwner =
                app.getBackupConfig().getAuthenticatedOwner();
        Assert.assertNotNull(authOwner);
    }
    
    public void testBackupTime() throws Exception {
        Assert.assertEquals(22, app.getBackupConfig().getBackupHour());
        Assert.assertEquals(20, app.getBackupConfig().getBackupMinute());
    }
    
    public void testToFromProperties() throws Exception {
        BackupConfig bakcfg = new BackupConfig(new File("test/integ/happy2/config1/BackupConfig.properties"));
        bakcfg.backupConfig = new File("test/integ/happy2/config1/BackupConfig-tmp.properties");
        bakcfg.dirty = true;
        bakcfg.save();

        BackupConfig bakcfg2 = new BackupConfig(bakcfg.backupConfig);
        Assert.assertEquals(bakcfg, bakcfg2);
    }
}
