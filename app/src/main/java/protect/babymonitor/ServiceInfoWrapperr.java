package protect.babymonitor;

import android.net.nsd.NsdServiceInfo;

import java.util.Map;

public class ServiceInfoWrapperr {
    private static final int DEFAULT_FREQUENCY = 11025;

    private final NsdServiceInfo _info;

    public ServiceInfoWrapperr(NsdServiceInfo info) {
        _info = info;
    }

    public String getAddress() {
        return _info.getHost().getHostAddress();
    }

    public int getPort() {
        return _info.getPort();
    }

    public int getFrequency() {

        Map<String, byte[]> attributes = _info.getAttributes();

        if (attributes.containsKey("_babymonitor.frequency")) {
            return Integer.parseInt(new String(attributes.get("_babymonitor.frequency")));
        }

        return DEFAULT_FREQUENCY;
    }

    public String getName() {
        // If there is more than one service on the network, it will
        // have a number at the end, but will appear as the following:
        //   "ProtectBabyMonitor\\032(number)
        // or
        //   "ProtectBabyMonitor\032(number)
        // Replace \\032 and \032 with a " "
        String serviceName = _info.getServiceName();
        serviceName = serviceName.replace("\\\\032", " ");
        serviceName = serviceName.replace("\\032", " ");
        return serviceName;
    }

    @Override
    public String toString() {
        return getName();
    }
}
