package com.example.projectmanager;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        dbHelper = new DatabaseHelper(this);
        TextView tvDetails = findViewById(R.id.tvActivityDetails);
        Button btnEdit = findViewById(R.id.btnEditActivity);
        Button btnDelete = findViewById(R.id.btnDeleteActivity);
        activityId = getIntent().getIntExtra("activityId", -1);

        Cursor cursor = dbHelper.getActivities(-1); // Adjust query to get single activity
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String desc = cursor.getString(cursor.getColumnIndex("description"));
            String start = cursor.getString(cursor.getColumnIndex("start_date"));
            String end = cursor.getString(cursor.getColumnIndex("end_date"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            tvDetails.setText("Nombre: " + name + "\nDescripción: " + desc + "\nInicio: " + start +
                    "\nFin: " + end + "\nEstado: " + status);
        }
        cursor.close();

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de edición no implementada", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            if (dbHelper.deleteActivity(activityId)) {
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al eliminar la actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }
}