package it.uniroma2.pjdm.bookapp.ui.annotation;

import androidx.cardview.widget.CardView;

import it.uniroma2.pjdm.bookapp.model.AnnotationModel;

public interface NotesClickListener {
    void onClick(AnnotationModel annotation);
    void onLongClick(AnnotationModel annotation, CardView cardView);
}
