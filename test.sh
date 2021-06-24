set -e

###
# Set up basic variables
###
# PIDs to clean up before exiting
CLEANUPPID=""

# Directory where script resides
RUNDIR=$(readlink -m $(dirname "$0"))
cd "$RUNDIR"

###
# Kill the background processes before exiting
###
function finish {
  eval kill $CLEANUPPID
}

trap finish EXIT
##

# Clean last run
rm -rf target/integ-working

export fbdir=$(readlink -m ../Friendly-Backup)

for testbasedir in test/integ/happy*
do
  testname=$(basename $testbasedir)

  # Install and start up each client
  for srcdir in $testbasedir/client*
  do
    testdir=target/integ-working/$testname/$(basename $srcdir)
  
    # install FriendlyBackup
    "$fbdir/install.sh" "$testdir"
  
    # Copy test client config, identity, etc.
    [ -d "$srcdir"/dir-to-backup ] && cp -r "$srcdir"/dir-to-backup "$testdir"
    cp -r "$srcdir"/gnupg "$testdir/var/"
    cp "$srcdir"/BackupConfig.properties "$testdir"/var/
  
    # Clean logs & start process
    pushd "$testdir"
    rm -f *.log
    ./bin/run.sh > ./run.out &
    CLEANUPPID="$! $CLEANUPPID "
    popd
  done
  
  # wait for the clients to begin listening
  sleep 1
  cd "$RUNDIR"
  TESTCLIENTBASE=target/integ-working/$testname/client1/
  
  # Start the backup
  tail -f $TESTCLIENTBASE/service.log | sed '/Starting backup/ q' > /dev/null&
  pid=$!

  touch "$TESTCLIENTBASE/backup.txt"
  
  echo "Waiting for backup to start"
  wait $pid

  echo "Backup started"

  tail -f $TESTCLIENTBASE/service.log | sed '/Backup complete/ q' > /dev/null
  echo "Backup complete"
  
  tail -f $TESTCLIENTBASE/service.log | sed '/Starting restore/ q' > /dev/null&
  pid=$!

  touch "$TESTCLIENTBASE/restore.txt"
  
  echo "Waiting for restore to start"
  wait $pid
  echo "Restore started"
  tail -f $TESTCLIENTBASE/service.log | sed '/Restore complete/ q' > /dev/null
  echo "Restore complete"
  
  if diff -qr "$TESTCLIENTBASE/dir-to-backup/" "$TESTCLIENTBASE/restore-dir/"
  then
    echo "Test passed"
  else
    echo "Test failed - backup & restore files not the same" 1>&2
  fi
done


# mkdir -p target/integ-working/happy2
cat  > /dev/null <<EOF
        Backup backup = new Backup(app.getBackupConfig());
        backup.doBackup();
        
        Restore restore = new Restore(app.getBackupConfig());
        restore.doRestore();

        Assert.assertTrue(compareDirectories(
                app.getBackupConfig().getBackupRootDirectories()[0],
                app.getBackupConfig().getRestoreRootDirectory()));
EOF

cat  > /dev/null <<EOF
        //wait for everyone to get started listening
        Thread.sleep(100);

        //clean out the db
        for(TestNode tn : testNodes) {
            File dbRoot = tn.getDBFile();
            cleanDirectory(dbRoot.getParentFile());
        }
        
        testNodes[0].backup();
        
        tryRestore();
        tryRestore();

        //touch a backup file
        File f = new File("test/integ/happy2/config1/dir-to-backup/hi.txt");
        FileOutputStream fos = new FileOutputStream(f, true);
        fos.write('A');
        fos.close();
        
        Thread.sleep(500);
        
        testNodes[0].backup();
        
        tryRestore();
        tryRestore();
EOF
