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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;

public class StartActivity extends Activity {
    static final String TAG = "BabyMonitor";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Baby monitor launched");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final Button monitorButton = findViewById(R.id.useChildDevice);
        monitorButton.setOnClickListener(v -> {
            Log.i(TAG, "Starting up monitor");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "Record audio permission not granted");

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

                return;
            }

            Intent i = new Intent(getApplicationContext(), MonitorActivity.class);
            startActivity(i);
        });

        final Button connectButton = findViewById(R.id.useParentDevice);
        connectButton.setOnClickListener(v -> {
            Log.i(TAG, "Starting connection activity");

            Intent i = new Intent(getApplicationContext(), DiscoverActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted! You can now start recording audio
                Log.d(TAG, "Record audio permission granted");

                Intent i = new Intent(getApplicationContext(), MonitorActivity.class);
                startActivity(i);

            } else {
                // Permission denied! Handle this gracefully
                Log.w(TAG, "Record audio permission denied");
                // ... inform the user and possibly disable audio recording features ...
            }
        }
    }
}
