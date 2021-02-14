package com.geekcommune.friendlybackup.config;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.geekcommune.identity.SecretIdentity;

public class BackupConfigTest {
    private BackupConfig bakcfg;

    @Before
    public void setUp() throws Exception {
        File cfgFile = new File("test/integ/happy2/config1/BackupConfig.properties");
        bakcfg = new BackupConfig(cfgFile);
    }
    
    @Test
    public void testKeyInitialization() throws Exception {
        SecretIdentity authOwner =
                bakcfg.getAuthenticatedOwner();
        Assert.assertNotNull(authOwner);
    }
    
    @Test
    public void testBackupTime() throws Exception {
        Assert.assertEquals(22, bakcfg.getBackupHour());
        Assert.assertEquals(20, bakcfg.getBackupMinute());
    }
    
    @Test
    public void testToFromProperties() throws Exception {
        BackupConfig bakcfg = new BackupConfig(new File("test/integ/happy2/config1/BackupConfig.properties"));
        bakcfg.backupConfig = new File("test/integ/happy2/config1/BackupConfig-tmp.properties");
        bakcfg.dirty = true;
        bakcfg.save();

        BackupConfig bakcfg2 = new BackupConfig(bakcfg.backupConfig);
        Assert.assertEquals(bakcfg, bakcfg2);
    }
}
