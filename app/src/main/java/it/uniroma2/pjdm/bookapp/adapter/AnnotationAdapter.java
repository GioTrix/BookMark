package it.uniroma2.pjdm.bookapp.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.model.AnnotationModel;

public class AnnotationAdapter extends RecyclerView.Adapter < AnnotationAdapter.AnnotationViewHolder > {
    private final ArrayList < AnnotationModel > annotationList;
    private final OnItemClickListener clickListener;

    public AnnotationAdapter(ArrayList < AnnotationModel > annotationList, OnItemClickListener clickListener) {
        this.annotationList = annotationList;
        this.clickListener = clickListener;
    }

    //Metodo chiamato quando viene creato un nuovo ViewHolder
    @NonNull
    @Override
    public AnnotationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ann_item, parent, false);
        return new AnnotationViewHolder(view);
    }

    //Metodo chiamato per mostrare i dati nella posizione specificata
    @Override
    public void onBindViewHolder(@NonNull AnnotationViewHolder holder, int position) {
        AnnotationModel currentAnnotation = annotationList.get(position);
        holder.frase.setText(currentAnnotation.getFrase());

        //Imposta un listener per l'intera vista dell'elemento
        holder.notesContainer.setOnClickListener(v -> {
            if (clickListener != null) {
                //Richiama il listener quando l'elemento viene rimosso
                clickListener.onItemClick(currentAnnotation, position);
            }
        });
    }

    //Restituisce il numero totale di elemento nella lista
    @Override
    public int getItemCount() {
        return annotationList.size();
    }

    //Rimuove un elemento dalla posizione specificata
    public void removeItem(int position) {
        if (isValidPosition(position)) {
            AnnotationModel annotation = annotationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,getItemCount());

            //Richiama il metodo onItemSwipe quando l'elemento viene rimosso
            if (clickListener != null) {
                clickListener.onItemSwipe(annotation, position);
            }
        }
    }

    //Aggiunge un elemento alla lista e notifica l'Adapter del cambiamento
    @SuppressLint("NotifyDataSetChanged")
    public void add(AnnotationModel annotationModel) {
        new Handler(Looper.getMainLooper()).post(() -> {
            annotationList.add(annotationModel);
            notifyDataSetChanged();
        });
    }

    //Verifica se la posizione specificata è valida nell'intervallo della lista delle annotazioni
    private boolean isValidPosition(int position) {
        return position >= 0 && position < annotationList.size();
    }

    //ViewHolder per gli elementi della lista
    public static class AnnotationViewHolder extends RecyclerView.ViewHolder {
        private final TextView frase;
        private final CardView notesContainer;

        public AnnotationViewHolder(@NonNull View itemView) {
            super(itemView);
            frase = itemView.findViewById(R.id.tv_item_annotation);
            notesContainer = itemView.findViewById(R.id.cv_ann_item);
        }
    }

    //Interfaccia per la gestione degli eventi al click sugli elementi della lista
    public interface OnItemClickListener {
        void onItemClick(AnnotationModel model, int position);
        void onItemSwipe(AnnotationModel annotation, int position);
    }
}