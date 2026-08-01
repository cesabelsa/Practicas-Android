package com.example.consumoapp.factura;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView preparado para estar dentro de un ScrollView.
 *
 * Un RecyclerView normal intenta ocupar únicamente el espacio visible y reciclar
 * sus filas. Dentro de un ScrollView esto puede provocar que se mida con la altura
 * de una sola tarjeta. Esta clase calcula la altura necesaria para mostrar todos
 * los elementos de la lista.
 */
public class FullyExpandedRecyclerView extends RecyclerView {

    public FullyExpandedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FullyExpandedRecyclerView(@NonNull Context context,
                                     @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FullyExpandedRecyclerView(@NonNull Context context,
                                     @Nullable AttributeSet attrs,
                                     int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Permitimos que RecyclerView mida toda la altura que necesitan sus filas.
        int expandedHeightSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST
        );

        super.onMeasure(widthSpec, expandedHeightSpec);

        // Conservamos exactamente la altura calculada por RecyclerView.
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }
}
