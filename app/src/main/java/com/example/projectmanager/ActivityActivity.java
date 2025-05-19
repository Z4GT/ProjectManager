package com.example.projectmanager;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ActivityActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int projectId;
    private EditText etActivityName, etActivityDescription, etStartDate, etEndDate;
    private Spinner spStatus;
    private ListView lvActivities;
    private ArrayList<String> activityList;
    private ArrayList<Integer> activityIds;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Actividades");
        }

        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        etActivityName = findViewById(R.id.etActivityName);
        etActivityDescription = findViewById(R.id.etActivityDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        spStatus = findViewById(R.id.spStatus);
        Button btnAddActivity = findViewById(R.id.btnAddActivity);
        lvActivities = findViewById(R.id.lvActivities);

        activityList = new ArrayList<>();
        activityIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activityList);
        lvActivities.setAdapter(adapter);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.status_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(spinnerAdapter);

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        btnAddActivity.setOnClickListener(v -> addActivity());

        lvActivities.setOnItemClickListener((parent, view, position, id) -> {
            int activityId = activityIds.get(position);
            showEditActivityDialog(activityId);
        });

        lvActivities.setOnItemLongClickListener((parent, view, position, id) -> {
            int activityId = activityIds.get(position);
            if (dbHelper.deleteActivity(activityId)) {
                Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
                loadActivities();
            } else {
                Toast.makeText(this, "Error al eliminar actividad", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        loadActivities();
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

    private void loadActivities() {
        activityList.clear();
        activityIds.clear();
        Cursor cursor = dbHelper.getActivities(projectId);
        try {
            int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int statusIndex = cursor.getColumnIndexOrThrow("status");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String status = cursor.getString(statusIndex);
                activityList.add(name + " (" + status + ")");
                activityIds.add(id);
            }
        } finally {
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showEditActivityDialog(int activityId) {
        Cursor cursor = dbHelper.getActivities(projectId);
        String currentName = "";
        String currentDescription = "";
        String currentStartDate = "";
        String currentEndDate = "";
        String currentStatus = "";
        try {
            int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int descriptionIndex = cursor.getColumnIndexOrThrow("description");
            int startDateIndex = cursor.getColumnIndexOrThrow("start_date");
            int endDateIndex = cursor.getColumnIndexOrThrow("end_date");
            int statusIndex = cursor.getColumnIndexOrThrow("status");
            while (cursor.moveToNext()) {
                if (cursor.getInt(idIndex) == activityId) {
                    currentName = cursor.getString(nameIndex);
                    currentDescription = cursor.getString(descriptionIndex);
                    currentStartDate = cursor.getString(startDateIndex);
                    currentEndDate = cursor.getString(endDateIndex);
                    currentStatus = cursor.getString(statusIndex);
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        EditText etName = findViewById(R.id.etActivityName);
        EditText etDescription = findViewById(R.id.etActivityDescription);
        EditText etStart = findViewById(R.id.etStartDate);
        EditText etEnd = findViewById(R.id.etEndDate);
        Spinner spStatusEdit = findViewById(R.id.spStatus);

        etName.setText(currentName);
        etDescription.setText(currentDescription);
        etStart.setText(currentStartDate);
        etEnd.setText(currentEndDate);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.status_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusEdit.setAdapter(spinnerAdapter);
        int statusPosition = spinnerAdapter.getPosition(currentStatus);
        spStatusEdit.setSelection(statusPosition >= 0 ? statusPosition : 0);

        Button btnAddActivity = findViewById(R.id.btnAddActivity);
        btnAddActivity.setText("Actualizar Actividad");

        btnAddActivity.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String description = etDescription.getText().toString();
            String startDate = etStart.getText().toString();
            String endDate = etEnd.getText().toString();
            String status = spStatusEdit.getSelectedItem().toString();

            if (name.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.updateActivity(activityId, name, description, startDate, endDate, status)) {
                Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
                clearFields();
                loadActivities();
                btnAddActivity.setText("Agregar Actividad");
                btnAddActivity.setOnClickListener(v2 -> addActivity());
            } else {
                Toast.makeText(this, "Error al actualizar actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addActivity() {
        String name = etActivityName.getText().toString();
        String description = etActivityDescription.getText().toString();
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();
        String status = spStatus.getSelectedItem().toString();

        if (name.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.addActivity(projectId, name, description, startDate, endDate, status)) {
            Toast.makeText(this, "Actividad agregada", Toast.LENGTH_SHORT).show();
            clearFields();
            loadActivities();
        } else {
            Toast.makeText(this, "Error al agregar actividad", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etActivityName.setText("");
        etActivityDescription.setText("");
        etStartDate.setText("");
        etEndDate.setText("");
        spStatus.setSelection(0);
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