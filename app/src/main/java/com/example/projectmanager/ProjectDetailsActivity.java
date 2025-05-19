package com.example.projectmanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProjectDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Proyecto");
        }

        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        TextView tvProjectName = findViewById(R.id.tvProjectName);
        TextView tvProjectDescription = findViewById(R.id.tvProjectDescription);
        TextView tvStartDate = findViewById(R.id.tvStartDate);
        TextView tvEndDate = findViewById(R.id.tvEndDate);
        TextView tvProgress = findViewById(R.id.tvProgress);
        ProgressBar pbProgress = findViewById(R.id.pbProgress);
        Button btnViewActivities = findViewById(R.id.btnViewActivities);
        Button btnDeleteProject = findViewById(R.id.btnDeleteProject);

        loadProjectDetails(tvProjectName, tvProjectDescription, tvStartDate, tvEndDate);
        updateProgress(tvProgress, pbProgress);

        btnViewActivities.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivity(intent);
        });

        btnDeleteProject.setOnClickListener(v -> {
            if (dbHelper.deleteProject(projectId)) {
                Toast.makeText(this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al eliminar proyecto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tvProgress = findViewById(R.id.tvProgress);
        ProgressBar pbProgress = findViewById(R.id.pbProgress);
        updateProgress(tvProgress, pbProgress);
    }

    private void loadProjectDetails(TextView tvName, TextView tvDescription, TextView tvStart, TextView tvEnd) {
        Cursor cursor = dbHelper.getProjects(getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1));
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndex("id")) == projectId) {
                tvName.setText(cursor.getString(cursor.getColumnIndex("name")));
                tvDescription.setText(cursor.getString(cursor.getColumnIndex("description")));
                tvStart.setText("Inicio: " + cursor.getString(cursor.getColumnIndex("start_date")));
                tvEnd.setText("Fin: " + cursor.getString(cursor.getColumnIndex("end_date")));
                break;
            }
        }
        cursor.close();
    }

    private void updateProgress(TextView tvProgress, ProgressBar pbProgress) {
        int progress = dbHelper.getProjectProgress(projectId);
        tvProgress.setText("Progreso: " + progress + "%");
        pbProgress.setProgress(progress);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}