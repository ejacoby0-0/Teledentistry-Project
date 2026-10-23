package com.example.teledentistry_project.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeethOverlayView extends View {

    public interface OnToothClickListener {
        void onToothClicked(String toothNumber, String newCondition, Map<String, String> allConditions);
    }

    private Paint borderPaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint legendPaint;
    private Paint titlePaint;

    private boolean isPractitionerMode = true;
    private String practitionerName = "Dr. Emily Watson";
    private String selectedToothNumber = "14";

    // FDI Tooth Numbers:
    // Upper Arch: 18..11, 21..28
    // Lower Arch: 48..41, 31..38
    private final String[] upperTeeth = {"18", "17", "16", "15", "14", "13", "12", "11", "21", "22", "23", "24", "25", "26", "27", "28"};
    private final String[] lowerTeeth = {"48", "47", "46", "45", "44", "43", "42", "41", "31", "32", "33", "34", "35", "36", "37", "38"};

    private final Map<String, String> toothConditions = new HashMap<>();
    private final List<ToothRect> toothBoundingBoxes = new ArrayList<>();

    private OnToothClickListener toothClickListener;

    private static class ToothRect {
        String toothNumber;
        RectF rect;

        ToothRect(String toothNumber, RectF rect) {
            this.toothNumber = toothNumber;
            this.rect = rect;
        }
    }

    public TeethOverlayView(Context context) {
        super(context);
        init();
    }

    public TeethOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TeethOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(24f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setTextSize(22f);
        legendPaint.setColor(Color.LTGRAY);
        legendPaint.setTextAlign(Paint.Align.LEFT);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(28f);
        titlePaint.setColor(Color.CYAN);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Default initial conditions for visual demonstration
        toothConditions.put("14", "decay");
        toothConditions.put("19", "treated");
        toothConditions.put("30", "focus");
    }

    public void setPractitionerMode(boolean isPractitioner) {
        this.isPractitionerMode = isPractitioner;
        invalidate();
    }

    public void setPractitionerName(String name) {
        if (name != null && !name.isEmpty()) {
            this.practitionerName = name;
            invalidate();
        }
    }

    public void setToothClickListener(OnToothClickListener listener) {
        this.toothClickListener = listener;
    }

    public void updateConditions(Map<String, String> newConditions, String selectedTooth) {
        if (newConditions != null) {
            toothConditions.clear();
            toothConditions.putAll(newConditions);
        }
        if (selectedTooth != null) {
            this.selectedToothNumber = selectedTooth;
        }
        invalidate();
    }

    public Map<String, String> getToothConditions() {
        return toothConditions;
    }

    public String getSelectedToothNumber() {
        return selectedToothNumber;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        // Background semi-transparent dark container overlay
        RectF bgRect = new RectF(16, 16, width - 16, height - 16);
        fillPaint.setColor(Color.parseColor("#CC121C24"));
        canvas.drawRoundRect(bgRect, 24, 24, fillPaint);

        borderPaint.setColor(Color.parseColor("#4000FFFF"));
        borderPaint.setStrokeWidth(3f);
        canvas.drawRoundRect(bgRect, 24, 24, borderPaint);

        // Header Title
        String title = isPractitionerMode ? "Interactive Dental Map (Tap to mark)" : "Live Doctor Dental Overlay (" + practitionerName + ")";
        canvas.drawText(title, width / 2f, 54f, titlePaint);

        toothBoundingBoxes.clear();

        float startYUpper = 90f;
        float toothWidth = (width - 64f) / 16f;
        float toothHeight = (height - 200f) / 2.8f;

        // Draw Upper Arch (18..28)
        for (int i = 0; i < upperTeeth.length; i++) {
            String toothNum = upperTeeth[i];
            float left = 32f + (i * toothWidth);
            float top = startYUpper + 24f;
            float right = left + toothWidth - 6f;
            float bottom = top + toothHeight;

            RectF rect = new RectF(left, top, right, bottom);
            toothBoundingBoxes.add(new ToothRect(toothNum, rect));

            drawSingleTooth(canvas, toothNum, rect, true);
        }

        // Draw Lower Arch (48..38)
        float startYLower = startYUpper + toothHeight + 50f;
        for (int i = 0; i < lowerTeeth.length; i++) {
            String toothNum = lowerTeeth[i];
            float left = 32f + (i * toothWidth);
            float top = startYLower;
            float right = left + toothWidth - 6f;
            float bottom = top + toothHeight;

            RectF rect = new RectF(left, top, right, bottom);
            toothBoundingBoxes.add(new ToothRect(toothNum, rect));

            drawSingleTooth(canvas, toothNum, rect, false);
        }

        // Draw Legend at bottom
        drawLegend(canvas, width, height);
    }

    private void drawSingleTooth(Canvas canvas, String toothNum, RectF rect, boolean isUpper) {
        String condition = toothConditions.getOrDefault(toothNum, "healthy");

        int fillColor;
        int strokeColor;

        switch (condition) {
            case "decay":
                fillColor = Color.parseColor("#80D32F2F"); // Red
                strokeColor = Color.parseColor("#FFD32F2F");
                break;
            case "treated":
                fillColor = Color.parseColor("#801976D2"); // Blue
                strokeColor = Color.parseColor("#FF42A5F5");
                break;
            case "focus":
                fillColor = Color.parseColor("#A0FFA000"); // Amber/Yellow
                strokeColor = Color.parseColor("#FFFFD54F");
                break;
            case "healthy":
            default:
                fillColor = Color.parseColor("#302E7D32"); // Translucent Teal/Green
                strokeColor = Color.parseColor("#8081C784");
                break;
        }

        if (toothNum.equals(selectedToothNumber)) {
            strokeColor = Color.CYAN;
            borderPaint.setStrokeWidth(6f);
        } else {
            borderPaint.setStrokeWidth(3f);
        }

        fillPaint.setColor(fillColor);
        canvas.drawRoundRect(rect, 12, 12, fillPaint);

        borderPaint.setColor(strokeColor);
        canvas.drawRoundRect(rect, 12, 12, borderPaint);

        // Draw Tooth Number
        float textY = isUpper ? rect.top - 6f : rect.bottom + 22f;
        canvas.drawText(toothNum, rect.centerX(), textY, textPaint);
    }

    private void drawLegend(Canvas canvas, int width, int height) {
        float legendY = height - 36f;
        float startX = 32f;

        drawLegendItem(canvas, startX, legendY, Color.parseColor("#8081C784"), "Healthy");
        drawLegendItem(canvas, startX + 160f, legendY, Color.parseColor("#FFD32F2F"), "Decay");
        drawLegendItem(canvas, startX + 300f, legendY, Color.parseColor("#FF42A5F5"), "Treated");
        drawLegendItem(canvas, startX + 440f, legendY, Color.parseColor("#FFFFD54F"), "Focus");
    }

    private void drawLegendItem(Canvas canvas, float x, float y, int color, String label) {
        fillPaint.setColor(color);
        canvas.drawCircle(x, y - 6f, 10f, fillPaint);
        canvas.drawText(label, x + 18f, y, legendPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPractitionerMode) {
            return super.onTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (ToothRect tr : toothBoundingBoxes) {
                if (tr.rect.contains(x, y)) {
                    selectedToothNumber = tr.toothNumber;
                    String current = toothConditions.getOrDefault(tr.toothNumber, "healthy");

                    String nextCondition;
                    switch (current) {
                        case "healthy":
                            nextCondition = "decay";
                            break;
                        case "decay":
                            nextCondition = "treated";
                            break;
                        case "treated":
                            nextCondition = "focus";
                            break;
                        default:
                            nextCondition = "healthy";
                            break;
                    }

                    toothConditions.put(tr.toothNumber, nextCondition);
                    invalidate();

                    if (toothClickListener != null) {
                        toothClickListener.onToothClicked(tr.toothNumber, nextCondition, toothConditions);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
