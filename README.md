# ShuntFlowMonitor
App for Rhaeos shunt flow detection device
Run following in gitbash:
git update-index --skip-worktree master_device/components/toolchain/gcc/Makefile.windows
git update-index --skip-worktree Android-nRF-Toolbox-master/app/app.iml
git update-index --skip-worktree DFULibrary/dfu/dfu.iml
git update-index --skip-worktree Android-nRF-Toolbox-master/.idea/caches/*

to check for skipped files/directories:
git ls-files -v | grep ^S