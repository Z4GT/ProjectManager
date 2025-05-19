package com.example.projectmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Iniciar Sesión");
        }

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = dbHelper.validateUser(username, password);
            if (userId != -1) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit().putInt("user_id", userId).apply();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Dialog to input username
            AlertDialog.Builder usernameDialog = new AlertDialog.Builder(this);
            usernameDialog.setTitle("Recuperar Contraseña");
            usernameDialog.setMessage("Ingrese su nombre de usuario");

            final EditText inputUsername = new EditText(this);
            inputUsername.setHint("Usuario");
            usernameDialog.setView(inputUsername);

            usernameDialog.setPositiveButton("Aceptar", (dialog, which) -> {
                String username = inputUsername.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(this, "Por favor, ingrese un usuario", Toast.LENGTH_SHORT).show();
                    return;
                }

                String recoveryCode = dbHelper.generateRecoveryCode(username);
                if (recoveryCode != null) {
                    // Show recovery code and prompt for reset
                    AlertDialog.Builder resetDialog = new AlertDialog.Builder(this);
                    resetDialog.setTitle("Restablecer Contraseña");
                    resetDialog.setMessage("Su código de recuperación es: " + recoveryCode +
                            "\nIngrese el código y su nueva contraseña.");

                    LinearLayout layout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(32, 16, 32, 16);

                    final EditText inputCode = new EditText(this);
                    inputCode.setHint("Código de recuperación");
                    layout.addView(inputCode);

                    final EditText inputPassword = new EditText(this);
                    inputPassword.setHint("Nueva contraseña");
                    inputPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    layout.addView(inputPassword);

                    resetDialog.setView(layout);

                    resetDialog.setPositiveButton("Restablecer", (d, w) -> {
                        String code = inputCode.getText().toString().trim();
                        String newPassword = inputPassword.getText().toString().trim();

                        if (code.isEmpty() || newPassword.isEmpty()) {
                            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int userId = dbHelper.validateRecoveryCode(code);
                        if (userId != -1) {
                            if (dbHelper.updatePassword(userId, newPassword)) {
                                Toast.makeText(this, "Contraseña restablecida exitosamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error al restablecer la contraseña", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Código de recuperación inválido o expirado", Toast.LENGTH_SHORT).show();
                        }
                    });

                    resetDialog.setNegativeButton("Cancelar", (d, w) -> d.cancel());
                    resetDialog.show();
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                }
            });

            usernameDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            usernameDialog.show();
        });
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