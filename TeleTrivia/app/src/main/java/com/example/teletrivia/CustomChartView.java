package com.example.teletrivia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomChartView extends View {
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private int unansweredQuestions = 0;

    public CustomChartView(Context context) {
        super(context);
        init();
    }

    public CustomChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);
    }

    public void setStatistics(int totalQuestions, int correctAnswers, int incorrectAnswers, int unansweredQuestions) {
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unansweredQuestions = unansweredQuestions;
        invalidate(); // Fuerza el redibujado
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (totalQuestions <= 0) {
            return; // No hay datos para mostrar
        }

        // Establecer dimensiones del gráfico
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2 * 0.7f;
        float centerX = width / 2;
        float centerY = height / 2;

        // Definir colores
        int colorCorrect = Color.parseColor("#4CAF50"); // Verde
        int colorIncorrect = Color.parseColor("#F44336"); // Rojo
        int colorUnanswered = Color.parseColor("#9E9E9E"); // Gris

        // Crear Paint para el gráfico
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        // Crear el rectángulo para el gráfico circular
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Dibujar secciones del gráfico de pastel
        float startAngle = 0;
        float sweepAngle;

        // Sección de respuestas correctas
        if (correctAnswers > 0) {
            paint.setColor(colorCorrect);
            sweepAngle = (float) correctAnswers / totalQuestions * 360;
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        // Sección de respuestas incorrectas
        if (incorrectAnswers > 0) {
            paint.setColor(colorIncorrect);
            sweepAngle = (float) incorrectAnswers / totalQuestions * 360;
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        // Sección de preguntas sin responder
        if (unansweredQuestions > 0) {
            paint.setColor(colorUnanswered);
            sweepAngle = (float) unansweredQuestions / totalQuestions * 360;
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
        }

        // Dibujar leyenda
        paint.setTextSize(30);
        paint.setStyle(Paint.Style.FILL);
        float legendY = centerY + radius + 60;

        // Leyenda para correctas
        paint.setColor(colorCorrect);
        canvas.drawCircle(width * 0.25f, legendY, 15, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText("Correctas", width * 0.25f + 30, legendY + 10, paint);

        // Leyenda para incorrectas
        paint.setColor(colorIncorrect);
        canvas.drawCircle(width * 0.25f, legendY + 50, 15, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText("Incorrectas", width * 0.25f + 30, legendY + 60, paint);

        // Leyenda para sin responder (si hay)
        if (unansweredQuestions > 0) {
            paint.setColor(colorUnanswered);
            canvas.drawCircle(width * 0.25f, legendY + 100, 15, paint);
            paint.setColor(Color.BLACK);
            canvas.drawText("Sin responder", width * 0.25f + 30, legendY + 110, paint);
        }
    }
}