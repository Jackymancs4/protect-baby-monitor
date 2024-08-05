/**
 * This file is part of the Protect Baby Monitor.
 * <p>
 * Protect Baby Monitor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Protect Baby Monitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Protect Baby Monitor. If not, see <http://www.gnu.org/licenses/>.
 */
package protect.babymonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class DiscoverActivity extends Activity {
    final String TAG = "BabyMonitor";

    private static final int DEFAULT_FREQUENCY = 11025;

    NsdManager _nsdManager;

    NsdManager.DiscoveryListener _discoveryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Baby monitor start");

        _nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        final Button discoverChildButton = (Button) findViewById(R.id.discoverChildButton);
        discoverChildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDiscoveryViaMdns();
            }
        });

        final Button enterChildAddressButton = (Button) findViewById(R.id.enterChildAddressButton);
        enterChildAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDiscoveryViaAddress();
            }
        });
    }

    private void loadDiscoveryViaMdns() {
        setContentView(R.layout.activity_discover_mdns);
        startServiceDiscovery("_babymonitor._tcp.");
    }

    private void loadDiscoveryViaAddress() {
        setContentView(R.layout.activity_discover_address);

        final Button connectButton = (Button) findViewById(R.id.connectViaAddressButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Connecting to child device via address");

                final EditText addressField = (EditText) findViewById(R.id.ipAddressField);
                final EditText portField = (EditText) findViewById(R.id.portField);

                final String addressString = addressField.getText().toString();
                final String portString = portField.getText().toString();

                if (addressString.isEmpty()) {
                    Toast.makeText(DiscoverActivity.this, R.string.invalidAddress, Toast.LENGTH_LONG).show();
                    return;
                }

                int port = 0;

                try {
                    port = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    Toast.makeText(DiscoverActivity.this, R.string.invalidPort, Toast.LENGTH_LONG).show();
                    return;
                }

                connectToChild(addressString, port, addressString, DEFAULT_FREQUENCY);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Baby monitoring stop");

        if (_discoveryListener != null) {
            Log.i(TAG, "Unregistering monitoring service");

            _nsdManager.stopServiceDiscovery(_discoveryListener);
            _discoveryListener = null;
        }

        super.onDestroy();
    }

    public void startServiceDiscovery(final String serviceType) {
        final NsdManager nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

        final ListView serviceTable = findViewById(R.id.ServiceTable);

        final ArrayAdapter<ServiceInfoWrapperr> availableServicesAdapter = new ArrayAdapter<>(this,
                R.layout.available_children_list);

        serviceTable.setAdapter(availableServicesAdapter);

        serviceTable.setOnItemClickListener((parent, view, position, id) -> {
            final ServiceInfoWrapperr info = (ServiceInfoWrapperr) parent.getItemAtPosition(position);
            connectToChild(info.getAddress(), info.getPort(), info.getName(), info.getFrequency());
        });

        // Instantiate a new DiscoveryListener
        _discoveryListener = new NsdManager.DiscoveryListener() {
            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success: " + service);

                if (!service.getServiceType().equals(serviceType)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().contains("ProtectBabyMonitor")) {
                    NsdManager.ResolveListener resolver = new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            // Called when the resolve fails.  Use the error code to debug.
                            Log.e(TAG, "Resolve failed: error " + errorCode + " for service: " + serviceInfo);
                        }

                        @Override
                        public void onServiceResolved(final NsdServiceInfo serviceInfo) {
                            Log.i(TAG, "Resolve Succeeded: " + serviceInfo);

                            DiscoverActivity.this.runOnUiThread(() -> availableServicesAdapter.add(new ServiceInfoWrapperr(serviceInfo)));
                        }
                    };

                    _nsdManager.resolveService(service, resolver);
                } else {
                    Log.d(TAG, "Unknown Service name: " + service.getServiceName());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "Service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };

        nsdManager.discoverServices(
                serviceType, NsdManager.PROTOCOL_DNS_SD, _discoveryListener);
    }

    /**
     * Launch the ListenActivity to connect to the given child device
     *
     * @param address
     * @param port
     * @param name
     */
    private void connectToChild(final String address, final int port, final String name, final int frequency) {
        final Intent i = new Intent(getApplicationContext(), ListenActivity.class);
        final Bundle b = new Bundle();
        b.putString("address", address);
        b.putInt("port", port);
        b.putString("name", name);
        b.putInt("frequency", frequency);

        i.putExtras(b);
        startActivity(i);
    }
}

