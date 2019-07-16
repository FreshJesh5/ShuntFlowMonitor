# NICU Master Project
***
NRF52 code for obtaining ECG and SCG.
## Usage
---
Go to the armgcc folder: `$(Project_home)\ble_adxl345\pca10040\s132\armgcc`

- Erase device: `make erase`

- Install Soft Device: `make flash_softdevice`

- Complie Project: `make`

- Flash the Project to NRF52 Chip: `make flash`

## Prerequisite Setup
---
**Windows**

1. Setting up GIT
    - Download [GIT](https://git-scm.com/downloads).**note:**Make sure to install git bash as well.

    - Setup public key

        i. Generate public Key. Follow instructions [here](https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/). 
**note:** Follow only `Generating a new SSH key` section. When generating, have every field to be default( press enter for all of them).

        ii. Add public Key. Follow instructions [here](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/).

        iii. Restart Computer.

    Now you will be able to clone any projects in bitbucket repository. use `git clone [address]` command to download the code in cmd.

2. Setup [NRF5x-Command Line tool](https://www.nordicsemi.com/eng/nordic/Products/nRF51822/nRF5x-Command-Line-Tools-Win32/33444)
3. Setting up Enviroment for Embedded development for GCC
    - install [GNU ARM Embedded Toolchain](https://launchpad.net/gcc-arm-embedded/+download)
**note:** Select  `gcc-arm-none-eabi-5_4-2016q2-20160622-win32.exe (md5)`.
Add path to enviroment variable.

    - install [GNU ARM Eclipse Windows Build Tools](https://github.com/gnuarmeclipse/windows-build-tools/releases)
**note:** Select  `gnuarmeclipse-build-tools-win32-2.8-201611221915-setup.exe`)
    - Add the `bin` folder of GNU ARM Eclipse Windows Build tools To environment Path.

        i. **RightClick windows Button** (on the left bottom corner of the screen). -> **System** -> **Andvanced System Settings** -> **Environment Variables** -> **Double Click Path on the upper list** -> **NEW** -> **Paste the installation Dir** ( By default `C:\Program Files (x86)\GNU ARM Eclipse\Build\bin)