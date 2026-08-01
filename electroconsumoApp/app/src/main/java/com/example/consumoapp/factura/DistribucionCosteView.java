package com.example.consumoapp.factura;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.consumoapp.R;

/**
 * Barra horizontal que representa el reparto del coste de una factura.
 *
 * No recalcula importes. Recibe las cantidades guardadas en la simulación y
 * transforma cada grupo en una proporción visual del total representado.
 */
public class DistribucionCosteView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private double energia;
    private double potencia;
    private double impuestos;
    private double otros;

    public DistribucionCosteView(Context context) {
        super(context);
    }

    public DistribucionCosteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DistribucionCosteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Configura los cuatro bloques que se dibujarán.
     */
    public void establecerImportes(double energia, double potencia, double impuestos, double otros) {
        this.energia = Math.max(0, energia);
        this.potencia = Math.max(0, potencia);
        this.impuestos = Math.max(0, impuestos);
        this.otros = Math.max(0, otros);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double total = energia + potencia + impuestos + otros;
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = getWidth() - getPaddingRight();
        float bottom = getHeight() - getPaddingBottom();

        rect.set(left, top, right, bottom);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.fondo_secundario));
        canvas.drawRoundRect(rect, 18f, 18f, paint);

        if (total <= 0 || right <= left) {
            return;
        }

        float anchoDisponible = right - left;
        float cursor = left;

        cursor = dibujarSegmento(canvas, cursor, top, bottom,
                anchoDisponible * (float) (energia / total), R.color.azul_principal);
        cursor = dibujarSegmento(canvas, cursor, top, bottom,
                anchoDisponible * (float) (potencia / total), R.color.azul_claro);
        cursor = dibujarSegmento(canvas, cursor, top, bottom,
                anchoDisponible * (float) (impuestos / total), R.color.naranja_llano);
        dibujarSegmento(canvas, cursor, top, bottom,
                Math.max(0, right - cursor), R.color.gris_texto);
    }

    private float dibujarSegmento(Canvas canvas, float left, float top, float bottom,
                                  float ancho, int colorRes) {
        if (ancho <= 0) {
            return left;
        }
        paint.setColor(ContextCompat.getColor(getContext(), colorRes));
        rect.set(left, top, left + ancho, bottom);
        canvas.drawRect(rect, paint);
        return left + ancho;
    }
}
