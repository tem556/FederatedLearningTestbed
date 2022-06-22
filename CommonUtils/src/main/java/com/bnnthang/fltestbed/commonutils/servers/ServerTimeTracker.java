package com.bnnthang.fltestbed.commonutils.servers;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerTimeTracker {
    public List<Long> roundStartTimes;
    public List<Long> configurationEndTimes;
    public List<Long> waitingEndTimes;
    public List<Long> reportingEndTimes;
    public List<Long> roundEndTimes;

    public ServerTimeTracker() {
        roundStartTimes = new ArrayList<>();
        configurationEndTimes = new ArrayList<>();
        waitingEndTimes = new ArrayList<>();
        reportingEndTimes = new ArrayList<>();
        roundEndTimes = new ArrayList<>();
    }

    public void addRound(Long roundStartTime,
                         Long configurationEndTime,
                         Long waitingEndTime,
                         Long reportingEndTime,
                         Long roundEndTime) {
        roundStartTimes.add(roundStartTime);
        configurationEndTimes.add(configurationEndTime);
        waitingEndTimes.add(waitingEndTime);
        reportingEndTimes.add(reportingEndTime);
        roundEndTimes.add(roundEndTime);
    }

    public void toCSV(File file) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(file));

        writer.writeNext(new String[] {
                "round_start",
                "configuration_end",
                "waiting_end",
                "reporting_end",
                "round_end" });

        int rounds = roundStartTimes.size();
        for (int i = 0; i < rounds; ++i) {
            writer.writeNext(new String[] {
                    roundStartTimes.get(i).toString(),
                    configurationEndTimes.get(i).toString(),
                    waitingEndTimes.get(i).toString(),
                    reportingEndTimes.get(i).toString(),
                    roundEndTimes.get(i).toString() });

            writer.flush();
        }
        writer.close();
    }
}
