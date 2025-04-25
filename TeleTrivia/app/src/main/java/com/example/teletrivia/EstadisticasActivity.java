package com.example.teletrivia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EstadisticasActivity extends AppCompatActivity {

    // UI Components
    private TextView tvCategoryResult;
    private TextView tvDifficultyResult;
    private TextView tvTimeUsed;
    private TextView tvCorrectAnswers;
    private TextView tvIncorrectAnswers;
    private TextView tvUnanswered;
    private TextView tvScore;
    private CustomChartView chartView;
    private Button btnPlayAgain;

    // Variables para estadísticas
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int unansweredQuestions;
    private String category;
    private String difficulty;
    private long timeUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        // Inicializar vistas
        tvCategoryResult = findViewById(R.id.tvCategoryResult);
        tvDifficultyResult = findViewById(R.id.tvDifficultyResult);
        tvTimeUsed = findViewById(R.id.tvTimeUsed);
        tvCorrectAnswers = findViewById(R.id.tvCorrectAnswers);
        tvIncorrectAnswers = findViewById(R.id.tvIncorrectAnswers);
        tvUnanswered = findViewById(R.id.tvUnanswered);
        tvScore = findViewById(R.id.tvScore);
        chartView = findViewById(R.id.chartView);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);

        // Obtener datos del intent
        Intent intent = getIntent();
        totalQuestions = intent.getIntExtra("totalQuestions", 0);
        correctAnswers = intent.getIntExtra("correctAnswers", 0);
        category = intent.getStringExtra("category");
        difficulty = intent.getStringExtra("difficulty");
        timeUsed = intent.getLongExtra("timeUsed", 0);

        // JuegoActivity envía el índice actual de la pregunta cuando finaliza el juego
        // Esto nos ayuda a saber cuántas preguntas se respondieron en total
        int lastQuestionIndex = intent.getIntExtra("lastQuestionIndex", totalQuestions);

        // Calcular estadísticas
        // Si no pudieron responder todas las preguntas (tiempo agotado)
        if (lastQuestionIndex < totalQuestions) {
            unansweredQuestions = totalQuestions - lastQuestionIndex;
            incorrectAnswers = lastQuestionIndex - correctAnswers;
        } else {
            // Respondieron todas las preguntas
            unansweredQuestions = 0;
            incorrectAnswers = totalQuestions - correctAnswers;
        }

        // Mostrar resultados en la UI
        displayResults();

        // Configurar el gráfico
        setupChart();

        // Configurar el botón para volver a jugar
        btnPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir al menú principal
                Intent mainIntent = new Intent(EstadisticasActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Limpiar actividades anteriores
                startActivity(mainIntent);
                finish();
            }
        });
    }

    private void displayResults() {
        // Mostrar categoría y dificultad
        tvCategoryResult.setText("Categoría: " + category);
        tvDifficultyResult.setText("Dificultad: " + difficulty);

        // Mostrar tiempo utilizado
        int minutes = (int) (timeUsed / 1000) / 60;
        int seconds = (int) (timeUsed / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimeUsed.setText(timeFormatted);

        // Mostrar resultados de respuestas
        tvCorrectAnswers.setText(String.valueOf(correctAnswers));
        tvIncorrectAnswers.setText(String.valueOf(incorrectAnswers));
        tvUnanswered.setText(String.valueOf(unansweredQuestions));

        // Mostrar puntuación final
        tvScore.setText(correctAnswers + "/" + totalQuestions);
    }

    private void setupChart() {
        // Configurar datos para el gráfico personalizado
        chartView.setStatistics(totalQuestions, correctAnswers, incorrectAnswers, unansweredQuestions);
    }

}