package com.example.projectmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView lvProjects;
    private ArrayAdapter<ProjectItem> adapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Proyectos");
        }

        dbHelper = new DatabaseHelper(this);
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
        lvProjects = findViewById(R.id.lvProjects);
        Button btnAddProject = findViewById(R.id.btnAddProject);

        btnAddProject.setOnClickListener(v -> showAddProjectDialog());

        lvProjects.setOnItemClickListener((parent, view, position, id) -> {
            ProjectItem project = adapter.getItem(position);
            Intent intent = new Intent(this, ProjectDetailsActivity.class);
            intent.putExtra("PROJECT_ID", project.id);
            startActivity(intent);
        });

        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_project, null);
        builder.setView(dialogView);

        EditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        EditText etProjectDescription = dialogView.findViewById(R.id.etProjectDescription);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        builder.setTitle("Agregar Proyecto")
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String name = etProjectName.getText().toString();
                    String description = etProjectDescription.getText().toString();
                    String startDate = etStartDate.getText().toString();
                    String endDate = etEndDate.getText().toString();

                    if (name.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                        Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (dbHelper.addProject(name, description, startDate, endDate, userId)) {
                        Toast.makeText(this, "Proyecto agregado", Toast.LENGTH_SHORT).show();
                        loadProjects();
                    } else {
                        Toast.makeText(this, "Error al agregar proyecto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null);

        builder.create().show();
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadProjects() {
        Cursor cursor = dbHelper.getProjects(userId);
        ProjectItem[] projects = new ProjectItem[cursor.getCount()];
        int index = 0;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            int progress = dbHelper.getProjectProgress(id);
            projects[index++] = new ProjectItem(id, name, progress);
        }
        cursor.close();

        adapter = new ArrayAdapter<ProjectItem>(this, R.layout.list_item_project, projects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_project, parent, false);
                }
                ProjectItem project = getItem(position);
                TextView tvProjectName = convertView.findViewById(R.id.tvProjectName);
                TextView tvProjectProgress = convertView.findViewById(R.id.tvProjectProgress);
                tvProjectName.setText(project.name);
                tvProjectProgress.setText("Progreso: " + project.progress + "%");
                return convertView;
            }
        };
        lvProjects.setAdapter(adapter);
    }

    private static class ProjectItem {
        int id;
        String name;
        int progress;

        ProjectItem(int id, String name, int progress) {
            this.id = id;
            this.name = name;
            this.progress = progress;
        }
    }
}