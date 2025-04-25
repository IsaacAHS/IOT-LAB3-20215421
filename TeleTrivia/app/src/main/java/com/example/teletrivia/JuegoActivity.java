package com.example.teletrivia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JuegoActivity extends AppCompatActivity {

    // Constantes para tiempo por pregunta según dificultad (en segundos)
    private static final int TIME_PER_QUESTION_EASY = 5;
    private static final int TIME_PER_QUESTION_MEDIUM = 7;
    private static final int TIME_PER_QUESTION_HARD = 10;

    // Constantes para API
    private static final String API_BASE_URL = "https://opentdb.com/api.php";
    private static final String TAG = "JuegoActivity";

    // Mapeo de categorías a sus IDs en la API
    private static final Map<String, Integer> CATEGORY_MAP = new HashMap<String, Integer>() {{
        put("Cultura General", 9);
        put("Libros", 10);
        put("Películas", 11);
        put("Música", 12);
        put("Computación", 18);
        put("Matemática", 19);
        put("Deportes", 21);
        put("Historia", 23);
    }};

    // UI Components
    private TextView tvTimeRemaining;
    private TextView tvQuestionCount;
    private TextView tvCategoryName;
    private TextView tvQuestion;
    private RadioGroup rgAnswers;
    private RadioButton rbAnswer1;
    private RadioButton rbAnswer2;
    private RadioButton rbAnswer3;
    private RadioButton rbAnswer4;
    private Button btnNext;

    // Variables del juego
    private String category;
    private int quantity;
    private String difficulty;
    private int timePerQuestion;
    private long totalTimeMillis;
    private CountDownTimer countDownTimer;
    private long timeLeftMillis;
    private boolean timerRunning = false;

    // Variables para preguntas
    private ArrayList<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;

    // Cliente para peticiones HTTP
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        // Inicializar vistas
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbAnswer1 = findViewById(R.id.rbAnswer1);
        rbAnswer2 = findViewById(R.id.rbAnswer2);
        rbAnswer3 = findViewById(R.id.rbAnswer3);
        rbAnswer4 = findViewById(R.id.rbAnswer4);
        btnNext = findViewById(R.id.btnNext);

        // Obtener datos del intent
        Intent intent = getIntent();
        category = intent.getStringExtra("category");
        quantity = intent.getIntExtra("quantity", 10);
        difficulty = intent.getStringExtra("difficulty");

        // Determinar el tiempo según la dificultad
        switch (difficulty) {
            case "fácil":
                timePerQuestion = TIME_PER_QUESTION_EASY;
                break;
            case "medio":
                timePerQuestion = TIME_PER_QUESTION_MEDIUM;
                break;
            case "difícil":
                timePerQuestion = TIME_PER_QUESTION_HARD;
                break;
            default:
                timePerQuestion = TIME_PER_QUESTION_MEDIUM;
        }

        // Calcular tiempo total en milisegundos
        totalTimeMillis = quantity * timePerQuestion * 1000L;
        timeLeftMillis = totalTimeMillis;

        // Establecer evento del botón siguiente
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
                moveToNextQuestion();
            }
        });

        // Restaurar estado si existe
        if (savedInstanceState != null) {
            timeLeftMillis = savedInstanceState.getLong("timeLeftMillis");
            currentQuestionIndex = savedInstanceState.getInt("currentQuestionIndex");
            correctAnswers = savedInstanceState.getInt("correctAnswers");
            questions = savedInstanceState.getParcelableArrayList("questions");

            if (questions != null && !questions.isEmpty()) {
                displayQuestion(currentQuestionIndex);
            } else {
                fetchQuestions();
            }
        } else {
            // Cargar preguntas desde la API
            fetchQuestions();
        }

        // Iniciar temporizador
        startTimer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeftMillis", timeLeftMillis);
        outState.putInt("currentQuestionIndex", currentQuestionIndex);
        outState.putInt("correctAnswers", correctAnswers);
        outState.putParcelableArrayList("questions", questions);

        // Pausar el temporizador
        if (countDownTimer != null) {
            countDownTimer.cancel();
            timerRunning = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reiniciar el temporizador si no está corriendo
        if (!timerRunning) {
            startTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar el temporizador
        if (countDownTimer != null) {
            countDownTimer.cancel();
            timerRunning = false;
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateTimerText();
                finishGame();
            }
        }.start();

        timerRunning = true;
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftMillis / 1000) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimeRemaining.setText(timeFormatted);
    }

    private void fetchQuestions() {
        // Mostrar indicador de carga
        setLoading(true);

        // Construir URL
        String difficultyParam;
        switch (difficulty) {
            case "fácil":
                difficultyParam = "easy";
                break;
            case "medio":
                difficultyParam = "medium";
                break;
            case "difícil":
                difficultyParam = "hard";
                break;
            default:
                difficultyParam = "medium";
        }

        int categoryId = CATEGORY_MAP.getOrDefault(category, 9); // Valor por defecto: 9 (General Knowledge)

        String url = API_BASE_URL +
                "?amount=" + quantity +
                "&category=" + categoryId +
                "&difficulty=" + difficultyParam +
                "&type=multiple";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(JuegoActivity.this, "Error al cargar preguntas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(JuegoActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    });
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    int responseCode = jsonObject.getInt("response_code");

                    if (responseCode != 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(JuegoActivity.this, "Error en la API: código " + responseCode, Toast.LENGTH_LONG).show();
                            setLoading(false);
                        });
                        return;
                    }

                    JSONArray resultsArray = jsonObject.getJSONArray("results");
                    questions.clear();

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject questionObj = resultsArray.getJSONObject(i);

                        // Obtener datos de la pregunta
                        String questionCategory = questionObj.getString("category");
                        String questionText = questionObj.getString("question");
                        String correctAnswer = questionObj.getString("correct_answer");
                        JSONArray incorrectAnswers = questionObj.getJSONArray("incorrect_answers");

                        // Crear lista de todas las respuestas
                        List<String> allAnswers = new ArrayList<>();
                        allAnswers.add(correctAnswer);

                        for (int j = 0; j < incorrectAnswers.length(); j++) {
                            allAnswers.add(incorrectAnswers.getString(j));
                        }

                        // Mezclar respuestas
                        Collections.shuffle(allAnswers);

                        // Crear objeto de pregunta
                        Question question = new Question(
                                questionCategory,
                                questionText,
                                correctAnswer,
                                allAnswers
                        );

                        questions.add(question);
                    }

                    runOnUiThread(() -> {
                        setLoading(false);
                        if (!questions.isEmpty()) {
                            displayQuestion(currentQuestionIndex);
                        } else {
                            Toast.makeText(JuegoActivity.this, "No se pudieron cargar preguntas", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    runOnUiThread(() -> {
                        Toast.makeText(JuegoActivity.this, "Error al procesar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    });
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            tvQuestion.setText("Cargando preguntas...");
            rbAnswer1.setVisibility(View.GONE);
            rbAnswer2.setVisibility(View.GONE);
            rbAnswer3.setVisibility(View.GONE);
            rbAnswer4.setVisibility(View.GONE);
            btnNext.setEnabled(false);
        } else {
            rbAnswer1.setVisibility(View.VISIBLE);
            rbAnswer2.setVisibility(View.VISIBLE);
            rbAnswer3.setVisibility(View.VISIBLE);
            rbAnswer4.setVisibility(View.VISIBLE);
            btnNext.setEnabled(true);
        }
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }

        Question question = questions.get(index);

        // Actualizar interfaz
        tvQuestionCount.setText((index + 1) + "/" + questions.size());
        tvCategoryName.setText(question.getCategory());
        tvQuestion.setText(fromHtml(question.getQuestion()));

        List<String> answers = question.getAllAnswers();
        rbAnswer1.setText(fromHtml(answers.get(0)));
        rbAnswer2.setText(fromHtml(answers.get(1)));
        rbAnswer3.setText(fromHtml(answers.get(2)));
        rbAnswer4.setText(fromHtml(answers.get(3)));

        // Limpiar selección
        rgAnswers.clearCheck();
    }

    private void checkAnswer() {
        // Si no hay selección, no hacer nada
        if (rgAnswers.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Por favor selecciona una respuesta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la respuesta seleccionada
        RadioButton selectedRb = findViewById(rgAnswers.getCheckedRadioButtonId());
        String selectedAnswer = selectedRb.getText().toString();

        // Verificar si es correcta
        Question currentQuestion = questions.get(currentQuestionIndex);
        if (selectedAnswer.equals(fromHtml(currentQuestion.getCorrectAnswer()))) {
            correctAnswers++;
        }
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex < questions.size()) {
            displayQuestion(currentQuestionIndex);
        } else {
            finishGame();
        }
    }

    private void finishGame() {
        // Detener el temporizador
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Enviar resultados a la actividad de estadísticas
        Intent intent = new Intent(this, EstadisticasActivity.class);
        intent.putExtra("totalQuestions", questions.size());
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("category", category);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("timeUsed", totalTimeMillis - timeLeftMillis);
        intent.putExtra("lastQuestionIndex", currentQuestionIndex);
        startActivity(intent);
        finish();
    }

    // Método auxiliar para manejar texto HTML
    private String fromHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return android.text.Html.fromHtml(html).toString();
        }
    }

}