#!/bin/sh

echo ">>> Re-enabling Turbo Boost"
echo 0 | sudo /usr/bin/tee /sys/devices/system/cpu/intel_pstate/no_turbo
echo ">>> Re-enabling Hyper-Threading"
#for cpu in {0..7}; do echo 1 | sudo /usr/bin/tee /sys/devices/system/cpu/cpu$cpu/online; done
echo on | sudo tee /sys/devices/system/cpu/smt/control
echo ">>> Re-enabling ASLR"
echo 2 | sudo /usr/bin/tee /proc/sys/kernel/randomize_va_space
echo ">>> Restarting services"
sudo /usr/bin/systemctl start bluetooth.service
sudo /usr/bin/systemctl start cups.service
sudo /usr/bin/systemctl start cups-browsed.service
sudo /usr/bin/systemctl start fwupd.service
sudo /usr/bin/systemctl start ModemManager.service
sudo /usr/bin/systemctl start NetworkManager.service
sudo /usr/bin/systemctl start wpa_supplicant.service
sudo /usr/bin/systemctl start upower.service
sudo /usr/bin/systemctl start switcheroo-control.service
