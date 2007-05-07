using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.InteropServices;

namespace Diwi {
    public class Backlight {

        //ensure the power requirement is released
        ~Backlight() {
            Release();
        }

        //handle to the power requirement
        private System.IntPtr handle;

        private enum PowerState {
            PwrDeviceUnspecified = -1,
            FULL = 0, //full on
            LOW = 1, //low power
            STANDBY = 2, //standby
            SLEEP = 3, //sleep
            OFF = 4, //off
            PwrDeviceMaximum = 5
        }

        //keep the backlight lit
        public void Activate() {
            handle = SetPowerRequirement("BKL1:", PowerState.PwrDeviceMaximum, 1, System.IntPtr.Zero, 0);
        }

        //release power requirement
        public void Release() {
            if (handle.ToInt32() != 0) {

                int result;
                result = ReleasePowerRequirement(handle);
                handle = System.IntPtr.Zero;
            }
        }



        [DllImportAttribute("CoreDll.dll")]
        private static extern System.IntPtr SetPowerRequirement(string pvDevice, PowerState DeviceState, int DeviceFlags, System.IntPtr pvSystemState, int StateFlags);


        [DllImportAttribute("CoreDll.dll")]
        private static extern int ReleasePowerRequirement(System.IntPtr hPowerReq);

    }

}
