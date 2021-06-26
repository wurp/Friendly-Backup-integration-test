package com.geekcommune.friendlybackup.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.geekcommune.communication.RemoteNodeHandle;
import com.geekcommune.communication.message.Message;
import com.geekcommune.friendlybackup.FriendlyBackupException;
import com.geekcommune.friendlybackup.communication.BackupMessageUtil;
import com.geekcommune.friendlybackup.communication.message.RetrieveDataMessage;
import com.geekcommune.friendlybackup.communication.message.VerifyMaybeSendDataMessage;
import com.geekcommune.friendlybackup.communication.message.VerifyMaybeSendMessage;
import com.geekcommune.friendlybackup.config.BackupConfig;
import com.geekcommune.friendlybackup.datastore.DataStore;
import com.geekcommune.friendlybackup.datastore.Lease;
import com.geekcommune.friendlybackup.format.low.HashIdentifier;
import com.geekcommune.friendlybackup.logging.UserLog;
import com.geekcommune.identity.Signature;
import com.geekcommune.util.DateUtil;
import com.geekcommune.util.Pair;

public class MockBackupMessageUtil extends BackupMessageUtil {
    private static final Logger log = LogManager.getLogger(MockBackupMessageUtil.class);

    private Map<Pair<RemoteNodeHandle,HashIdentifier>, byte[]> dataSent = new HashMap<Pair<RemoteNodeHandle,HashIdentifier>, byte[]>();

    private List<HashIdentifier> dontListenList = new ArrayList<HashIdentifier>();

    public MockBackupMessageUtil(BackupConfig backupConfig) {
        this.bakcfg = backupConfig;
    }

    @Override
    public void queueMessage(
            Message msg) {
        Message.State endState = Message.State.Error;
        try {
            if( msg instanceof VerifyMaybeSendMessage ) {
                VerifyMaybeSendMessage vmsm = (VerifyMaybeSendMessage) msg;
                Pair<RemoteNodeHandle,HashIdentifier> key = new Pair<RemoteNodeHandle,HashIdentifier>(msg.getDestination(), vmsm.getDataHashID());
                log.info("Putting {} bytes for key {}", vmsm.getData().length, key);
                dataSent.put(key, vmsm.getData());
                
                try {
                    DataStore.instance().storeData(
                            vmsm.getDataHashID(),
                            vmsm.getData(),
                            new Lease(
                                    DateUtil.oneHourHence(),
                                    getBackupConfig().getOwner().getHandle(),
                                    Signature.INTERNAL_SELF_SIGNED,
                                    vmsm.getDataHashID()));
                    endState = Message.State.Finished;
                } catch (FriendlyBackupException e) {
                    log.error(e.getMessage(), e);
                }
            } else if( msg instanceof RetrieveDataMessage ){
                RetrieveDataMessage rdm = (RetrieveDataMessage) msg;
                
                if( dontListenList.contains(rdm.getHashIDOfDataToRetrieve()) ) {
                    log.info("not listening to {}", rdm.getHashIDOfDataToRetrieve());
                    endState = Message.State.Finished;
                } else {
                    Pair<RemoteNodeHandle,HashIdentifier> key = new Pair<RemoteNodeHandle,HashIdentifier>(msg.getDestination(), rdm.getHashIDOfDataToRetrieve());
                    byte[] data = dataSent.get(key);
                    log.info("Retrieved {} bytes for key {}", (data == null ? null : data.length), key);

                    if( data == null ) {
                        byte[] dsdata = DataStore.instance().getData(rdm.getHashIDOfDataToRetrieve());
                        log.info("Retrieved {} bytes for key {}", dsdata == null ? null : dsdata.length, rdm.getHashIDOfDataToRetrieve());
                        rdm.handleResponse(makeMockDataMessage(dsdata));
                        endState = Message.State.Finished;
                    } else {
                        rdm.handleResponse(makeMockDataMessage(data));
                        endState = Message.State.Finished;
                    }
                }
            } else {
                System.out.println("Unhandled message " + msg);
            }
        } catch (FriendlyBackupException e) {
            UserLog.instance().logError("failed to queue message", e);
        } finally {
            msg.setState(endState);
        }
    }

	private Message makeMockDataMessage(byte[] data) {
		return new VerifyMaybeSendDataMessage(null, 0, null, data, null);
	}
}
