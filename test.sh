set -e

cd $(dirname "$0")

rm -rf target/integ-working

export testdir=target/integ-working/happy1/client1
export srcdir=test/integ/happy1/client1
export fbdir=$(readlink -m ../Friendly-Backup)

mkdir -p "$testdir"/bin/var
cp -r "$fbdir"/dist/generic/* "$testdir"/bin/
cp -r "$srcdir"/dir-to-backup "$testdir"
cp -r "$srcdir"/gnupg "$testdir"
cp "$srcdir"/BackupConfig.properties "$testdir"/bin/var/
ln -s $(readlink -m "$fbdir"/target/Friendly-Backup-*-jar-with-dependencies.jar) "$testdir"/bin/
cd "$testdir"/bin
rm *.log
./run.sh

# mkdir -p target/integ-working/happy2
