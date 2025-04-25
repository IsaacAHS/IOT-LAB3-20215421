package com.example.teletrivia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText etQuantity;
    private RadioGroup rgDifficulty;
    private Button btnCheckConnection;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etQuantity = findViewById(R.id.etQuantity);
        rgDifficulty = findViewById(R.id.rgDifficulty);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);
        btnStart = findViewById(R.id.btnStart);

        // Configurar el Spinner de categorías
        String[] categories = {"Seleccione una categoría", "Cultura General", "Libros", "Películas", "Música",
                "Computación", "Matemática", "Deportes", "Historia"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // Configurar el botón de verificación de conexión
        btnCheckConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    checkInternetConnection();
                }
            }
        });

        // Configurar el botón de inicio
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private boolean validateInputs() {
        // Validar que se haya seleccionado una categoría
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor seleccione una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que la cantidad sea un número positivo
        String quantityStr = etQuantity.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una cantidad", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "La cantidad debe ser un número positivo", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que se haya seleccionado una dificultad
        if (rgDifficulty.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Por favor seleccione un nivel de dificultad", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            // Si hay conexión a internet
            Toast.makeText(this, "Conexión a internet exitosa", Toast.LENGTH_SHORT).show();
            btnStart.setEnabled(true);
        } else {
            // Si no hay conexión a internet
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show();
            btnStart.setEnabled(false);
        }
    }

    private String getDifficultyLevel() {
        int selectedId = rgDifficulty.getCheckedRadioButtonId();
        if (selectedId == R.id.rbEasy) {
            return "fácil";
        } else if (selectedId == R.id.rbMedium) {
            return "medio";
        } else {
            return "difícil";
        }
    }

    private void startGame() {
        // Crear intent para iniciar JuegoActivity
        Intent intent = new Intent(MainActivity.this, JuegoActivity.class);

        // Pasar datos necesarios
        intent.putExtra("category", spinnerCategory.getSelectedItem().toString());
        intent.putExtra("quantity", Integer.parseInt(etQuantity.getText().toString()));
        intent.putExtra("difficulty", getDifficultyLevel());

        // Iniciar la actividad
        startActivity(intent);
    }
}