package com.bnnthang.fltestbed.Server;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AndroidDevices {
    private static final String APK_PATH = "C:\\Users\\buinn\\Repos\\FederatedLearningTestbed\\AndroidClient\\app\\build\\intermediates\\apk\\debug\\app-debug.apk";

    private static final String DEVICE_LIST_CMD = "adb devices -l";
    private static final String FORCE_STOP_CMD = "adb -s %s shell am force-stop com.bnnthang.fltestbed.androidclient";
    private static final String INSTALL_CMD = "adb -s %s install -r \"%s\"";
    private static final String START_CMD = "adb -s %s shell am start com.bnnthang.fltestbed.androidclient/.MainActivity";

    public static void install_apk() {
        List<String> devices = getDeviceNames();
        for (String device : devices) {
            // stop
            execCmd(String.format(FORCE_STOP_CMD, device));

            // install
            execCmd(String.format(INSTALL_CMD, device, APK_PATH));

            // start
            execCmd(String.format(START_CMD, device));
        }
    }

    private static String execCmd(String cmd) {
        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();
             Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> getDeviceNames() {
        List<String> devices = new ArrayList<>();
        String[] res = execCmd(DEVICE_LIST_CMD).split(System.lineSeparator());
        for (String s : res) {
            String[] s1 = s.split(" ");
            if (ArrayUtils.contains(s1, "device")) {
                System.out.println(s1[0]);
                devices.add(s1[0]);
            }
        }
        return devices;
    }
}
