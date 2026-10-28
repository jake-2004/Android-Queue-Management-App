
package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.database.Cursor;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText edtName;
    private EditText edtService;
    private TextView txtCurrent;
    private TextView txtQueue;
    private Button btnGenerate;
    private Button btnServed;

    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect XML views
        edtName = findViewById(R.id.edtName);
        edtService = findViewById(R.id.edtService);
        txtCurrent = findViewById(R.id.txtCurrent);
        txtQueue = findViewById(R.id.txtQueue);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnServed = findViewById(R.id.btnServed);

        // Initialize SQLite
        database = new DatabaseHelper(this);

        // Button click events
        btnGenerate.setOnClickListener(v -> generateToken());
        btnServed.setOnClickListener(v -> serveNextCustomer());

        // Display saved queue
        refreshQueue();
    }

    private void generateToken() {
        String name = edtName.getText().toString().trim();
        String service = edtService.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Please enter customer name");
            edtName.requestFocus();
            return;
        }

        if (service.isEmpty()) {
            edtService.setError("Please enter service name");
            edtService.requestFocus();
            return;
        }

        try {
            long tokenId = database.addCustomer(name, service);

            if (tokenId != -1) {
                Toast.makeText(
                        this,
                        "Token generated: #" + tokenId,
                        Toast.LENGTH_LONG
                ).show();

                edtName.setText("");
                edtService.setText("");

                refreshQueue();
            } else {
                Toast.makeText(
                        this,
                        "Could not generate token",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {
            Log.e("QueueApp", "Token generation failed", e);

            Toast.makeText(
                    this,
                    "Database error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void serveNextCustomer() {
        Cursor cursor = null;

        try {
            cursor = database.getWaitingCustomers();

            if (cursor.moveToFirst()) {
                int tokenId = cursor.getInt(0);
                String name = cursor.getString(1);
                String service = cursor.getString(2);

                boolean served = database.markServed(tokenId);

                if (served) {
                    Toast.makeText(
                            this,
                            "Served #" + tokenId + ": " + name,
                            Toast.LENGTH_LONG
                    ).show();
                }

                refreshQueue();

            } else {
                Toast.makeText(
                        this,
                        "No customers waiting",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {
            Log.e("QueueApp", "Serving customer failed", e);

            Toast.makeText(
                    this,
                    "Could not serve customer",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void refreshQueue() {
        Cursor cursor = null;

        try {
            cursor = database.getWaitingCustomers();
            StringBuilder queue = new StringBuilder();

            if (cursor.moveToFirst()) {
                int firstId = cursor.getInt(0);
                String firstName = cursor.getString(1);
                String firstService = cursor.getString(2);

                txtCurrent.setText(
                        "Token #" + firstId +
                                "\n" + firstName +
                                "\n" + firstService
                );

                do {
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    String service = cursor.getString(2);

                    queue.append("🎟️ TOKEN #")
                            .append(id)
                            .append("\n👤 ")
                            .append(name)
                            .append("\n🛎️ ")
                            .append(service)
                            .append("\n\n");

                } while (cursor.moveToNext());

            } else {
                txtCurrent.setText("No customers waiting");
                queue.append("Your queue is empty!");
            }

            txtQueue.setText(queue.toString());

        } catch (Exception e) {
            Log.e("QueueApp", "Queue refresh failed", e);
            txtCurrent.setText("Queue unavailable");
            txtQueue.setText("Unable to load customers.");

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }
}
