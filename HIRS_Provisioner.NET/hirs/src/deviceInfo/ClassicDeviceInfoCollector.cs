﻿using Hirs.Pb;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Management;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Text;
using System.IO;
using Serilog;

namespace hirs {
    public class ClassicDeviceInfoCollector : IHirsDeviceInfoCollector {
        public static readonly string NOT_SPECIFIED = "Not Specified";
        public static readonly string LINUX_DEFAULT_BIOS_VENDOR_PATH = "/sys/class/dmi/id";
        public static readonly string LINUX_DEFAULT_BIOS_VERSION_PATH = "/sys/class/dmi/id";
        public static readonly string LINUX_DEFAULT_BIOS_DATE_PATH = "/sys/class/dmi/id";
        public static readonly string LINUX_DEFAULT_SYS_VENDOR_PATH = "/sys/class/dmi/id";
        public static readonly string LINUX_DEFAULT_PRODUCT_NAME_PATH = "/sys/class/dmi/id";
        public static readonly string LINUX_DEFAULT_PRODUCT_VERSION_PATH = "/sys/class/dmi/id";
        public static readonly string LINUX_DEFAULT_PRODUCT_SERIAL_PATH = "/sys/class/dmi/id";
        private readonly Settings? settings;

        public ClassicDeviceInfoCollector() {
            settings = null;
        }
        public ClassicDeviceInfoCollector(Settings settings) {
            this.settings = settings;
        }

        public static string FileToString(string path, string def) {
            string result;
            try {
                result = File.ReadAllText(path).Trim();
            } catch {
                result = def;
            }
            if (string.IsNullOrWhiteSpace(result)) {
                result = def;
            }
            return result;
        }

        public DeviceInfo CollectDeviceInfo(string acaAddress) {
            DeviceInfo dv = new();
            dv.Fw = CollectFirmwareInfo();
            dv.Hw = CollectHardwareInfo();
            dv.Nw = CollectNetworkInfo(acaAddress);
            dv.Os = CollectOsInfo();
            return dv;
        }

        public FirmwareInfo CollectFirmwareInfo() {
            FirmwareInfo fw = new();

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                ManagementScope myScope = new("root\\CIMV2");
                ManagementObjectSearcher s = new("SELECT * FROM Win32_BIOS");
                fw.BiosVendor = NOT_SPECIFIED;
                fw.BiosVersion = NOT_SPECIFIED;
                fw.BiosReleaseDate = NOT_SPECIFIED;
                foreach (ManagementObject o in s.Get()) {
                    string manufacturer = (string)o.GetPropertyValue("Manufacturer");
                    string version = (string)o.GetPropertyValue("Version");
                    string releasedate = (string)o.GetPropertyValue("ReleaseDate");
                    fw.BiosVendor = string.IsNullOrEmpty(manufacturer) ? NOT_SPECIFIED : manufacturer;
                    fw.BiosVersion = string.IsNullOrEmpty(version) ? NOT_SPECIFIED : version;
                    fw.BiosReleaseDate = string.IsNullOrEmpty(releasedate) ? NOT_SPECIFIED : releasedate;
                    break;
                }
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                fw.BiosVendor = FileToString(settings != null ? settings.linux_bios_vendor_file : LINUX_DEFAULT_BIOS_VENDOR_PATH, NOT_SPECIFIED);
                fw.BiosVersion = FileToString(settings != null ? settings.linux_bios_version_file : LINUX_DEFAULT_BIOS_VERSION_PATH, NOT_SPECIFIED);
                fw.BiosReleaseDate = FileToString(settings != null ? settings.linux_bios_date_file : LINUX_DEFAULT_BIOS_DATE_PATH, NOT_SPECIFIED);
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.OSX)) {
                // tbd
            } else {
                // tbd
            }

            Log.Debug("Bios Vendor: " + fw.BiosVendor);
            Log.Debug("Bios Version: " + fw.BiosVersion);
            Log.Debug("Bios Date: " + fw.BiosReleaseDate);

            return fw;
        }

        

        public HardwareInfo CollectHardwareInfo() {
            HardwareInfo hw = new();

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                ManagementScope myScope = new("root\\CIMV2");
                ManagementObjectSearcher s = new("SELECT * FROM Win32_ComputerSystemProduct");
                hw.Manufacturer = NOT_SPECIFIED;
                hw.ProductName = NOT_SPECIFIED;
                hw.ProductVersion = NOT_SPECIFIED;
                hw.SystemSerialNumber = NOT_SPECIFIED;
                foreach (ManagementObject o in s.Get()) {
                    string vendor = (string)o.GetPropertyValue("Vendor");
                    string name = (string)o.GetPropertyValue("Name");
                    string version = (string)o.GetPropertyValue("Version");
                    string identifyingnumber = (string)o.GetPropertyValue("IdentifyingNumber");
                    hw.Manufacturer = string.IsNullOrWhiteSpace(vendor) ? NOT_SPECIFIED : vendor;
                    hw.ProductName = string.IsNullOrWhiteSpace(name) ? NOT_SPECIFIED : name;
                    hw.ProductVersion = string.IsNullOrWhiteSpace(version) ? NOT_SPECIFIED : version;
                    hw.SystemSerialNumber = string.IsNullOrWhiteSpace(identifyingnumber) ? NOT_SPECIFIED : identifyingnumber;
                    break;
                }
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                hw.Manufacturer = FileToString(settings != null ? settings.linux_sys_vendor_file : LINUX_DEFAULT_SYS_VENDOR_PATH, NOT_SPECIFIED);
                hw.ProductName = FileToString(settings != null ? settings.linux_product_name_file : LINUX_DEFAULT_PRODUCT_NAME_PATH, NOT_SPECIFIED);
                hw.ProductVersion = FileToString(settings != null ? settings.linux_product_version_file : LINUX_DEFAULT_PRODUCT_VERSION_PATH, NOT_SPECIFIED);
                hw.SystemSerialNumber = FileToString(settings != null ? settings.linux_product_serial_file : LINUX_DEFAULT_PRODUCT_SERIAL_PATH, NOT_SPECIFIED);
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.OSX)) {
                // tbd
            } else {
                // tbd
            }

            Log.Debug("System Manufacturer: " + hw.Manufacturer);
            Log.Debug("Product Name: " + hw.ProductName);
            Log.Debug("Product Version: " + hw.ProductVersion);
            Log.Debug("System Serial Number: " + hw.SystemSerialNumber);

            return hw;
        }

        public NetworkInfo CollectNetworkInfo(string acaAddress) {
            NetworkInfo nw = new();

            NetworkInterface iface = NetworkInterface
                .GetAllNetworkInterfaces()
                .Where(nic => nic.OperationalStatus == OperationalStatus.Up && nic.NetworkInterfaceType != NetworkInterfaceType.Loopback)
                .FirstOrDefault();

            nw.MacAddress = iface.GetPhysicalAddress().ToString();

            nw.ClearIpAddress();
            // First attempt to find local ip by connecting ACA
            if (string.IsNullOrWhiteSpace(acaAddress)) {
                Uri uri = new(acaAddress);
                try {
                    Socket socket = new(AddressFamily.InterNetwork, SocketType.Dgram, 0);
                    socket.Connect(uri.Host, uri.Port);
                    IPEndPoint endPoint = socket.LocalEndPoint as IPEndPoint;
                    nw.IpAddress = endPoint.Address.ToString();
                } catch {
                    Log.Debug("Not connected to the internet. Trying another search.");
                }
            }
            // Second attempt to find local ip by scanning first interface that is up and not a loopback address 
            if (!nw.HasIpAddress) {
                foreach (UnicastIPAddressInformation ip in iface.GetIPProperties().UnicastAddresses) {
                    if (ip.Address.AddressFamily == AddressFamily.InterNetwork) {
                        nw.IpAddress = ip.Address.ToString();
                        break;
                    }
                }
            }
            if (nw.HasIpAddress) {
                nw.Hostname = Dns.GetHostEntry(nw.IpAddress).HostName;
            } else {
                nw.Hostname = Dns.GetHostName();
            }
            

            Log.Debug("Network Info IP: " + nw.IpAddress);
            Log.Debug("Network Info MAC: " + nw.MacAddress);
            Log.Debug("Network Info Hostname: " + nw.Hostname);
            
            return nw;
        }

        public OsInfo CollectOsInfo() {
            OsInfo info = new();

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                info.OsName = OSPlatform.Windows.ToString();
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                info.OsName = OSPlatform.Linux.ToString();
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.OSX)) {
                info.OsName = OSPlatform.OSX.ToString();
            } else {
                info.OsName = RuntimeInformation.OSDescription;
            }
            info.OsVersion = RuntimeInformation.OSDescription;
            info.OsArch = RuntimeInformation.OSArchitecture.ToString();
            info.Distribution = RuntimeInformation.FrameworkDescription;
            info.DistributionRelease = RuntimeInformation.FrameworkDescription;

            Log.Debug("OS Name: " + info.OsName);
            Log.Debug("OS Version: " + info.OsVersion);
            Log.Debug("Architecture: " + info.OsArch);
            Log.Debug("Distribution: " + info.Distribution);
            Log.Debug("Distribution Release: " + info.DistributionRelease);

            return info;
        }
    }
}
