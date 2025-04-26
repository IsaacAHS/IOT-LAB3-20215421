package com.example.teletrivia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView actvCategory;
    private AutoCompleteTextView actvDifficulty;
    private TextInputEditText etQuantity;
    private MaterialButton btnCheckConnection;
    private MaterialButton btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        actvCategory = findViewById(R.id.actvCategory);
        actvDifficulty = findViewById(R.id.actvDifficulty);
        etQuantity = findViewById(R.id.etQuantity);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);
        btnStart = findViewById(R.id.btnStart);

        // Configurar el dropdown de categorías
        String[] categories = {"Seleccione una categoría", "Cultura General", "Libros", "Películas", "Música",
                "Computación", "Matemática", "Deportes", "Historia"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(categoryAdapter);

        // Configurar el dropdown de dificultad
        String[] difficulties = {"Seleccione dificultad", "Fácil", "Medio", "Difícil"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, difficulties);
        actvDifficulty.setAdapter(difficultyAdapter);

        // Establecer valores iniciales para los dropdowns
        actvCategory.setText(categories[0], false);
        actvDifficulty.setText(difficulties[0], false);

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
        String selectedCategory = actvCategory.getText().toString();
        if (selectedCategory.equals("Seleccione una categoría") || selectedCategory.isEmpty()) {
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
        String selectedDifficulty = actvDifficulty.getText().toString();
        if (selectedDifficulty.equals("Seleccione dificultad") || selectedDifficulty.isEmpty()) {
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
            btnStart.setBackgroundTintList(getResources().getColorStateList(R.color.boton));
        } else {
            // Si no hay conexión a internet
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show();
            btnStart.setEnabled(false);
            btnStart.setBackgroundTintList(getResources().getColorStateList(R.color.adicional));
        }
    }

    private String getDifficultyLevel() {
        String selectedDifficulty = actvDifficulty.getText().toString();
        switch (selectedDifficulty) {
            case "Fácil":
                return "fácil";
            case "Medio":
                return "medio";
            case "Difícil":
                return "difícil";
            default:
                return "medio"; // Valor por defecto
        }
    }

    private void startGame() {
        // Crear intent para iniciar JuegoActivity
        Intent intent = new Intent(MainActivity.this, JuegoActivity.class);

        // Pasar datos necesarios
        intent.putExtra("category", actvCategory.getText().toString());
        intent.putExtra("quantity", Integer.parseInt(etQuantity.getText().toString().trim()));
        intent.putExtra("difficulty", getDifficultyLevel());

        // Iniciar la actividad
        startActivity(intent);
    }
}