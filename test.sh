set -e

cd $(dirname "$0")

rm -rf target/integ-working

export testdir=target/integ-working/happy1/client1
export srcdir=test/integ/happy1/client1
export fbdir=$(readlink -m ../Friendly-Backup)

"$fbdir/install.sh" "$testdir"
cp -r "$srcdir"/dir-to-backup "$testdir"
cp -r "$srcdir"/gnupg "$testdir"
cp "$srcdir"/BackupConfig.properties "$testdir"/bin/var/
cd "$testdir"/bin
rm -f *.log
./run.sh

# mkdir -p target/integ-working/happy2
