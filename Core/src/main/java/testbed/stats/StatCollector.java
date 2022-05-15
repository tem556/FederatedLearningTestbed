package testbed.stats;

public interface StatCollector {
    void addUplinkCommunicationStat(int bytes, double time);
    void addDownlinkCommunicationStat(int bytes, double time);
    void addComputingTime(double time);
    void addAccuracy(double accuracy);
    void print();
}
