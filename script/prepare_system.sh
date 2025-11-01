#!/bin/sh

echo ">>> Disabling Turbo Boost"
echo 1 | sudo /usr/bin/tee /sys/devices/system/cpu/intel_pstate/no_turbo
echo ">>> Disabling Hyper-Threading"
#for cpu in {0..7}; do echo 0 | sudo /usr/bin/tee /sys/devices/system/cpu/cpu$cpu/online; done
echo off | sudo tee /sys/devices/system/cpu/smt/control
echo ">>> Disabling ASLR"
echo 0 | sudo /usr/bin/tee /proc/sys/kernel/randomize_va_space
echo ">>> Stopping non-essential services"
sudo /usr/bin/systemctl stop bluetooth.service
sudo /usr/bin/systemctl stop cups.service
sudo /usr/bin/systemctl stop cups-browsed.service
sudo /usr/bin/systemctl stop fwupd.service
sudo /usr/bin/systemctl stop ModemManager.service
sudo /usr/bin/systemctl stop NetworkManager.service
sudo /usr/bin/systemctl stop wpa_supplicant.service
sudo /usr/bin/systemctl stop upower.service
sudo /usr/bin/systemctl stop switcheroo-control.service
