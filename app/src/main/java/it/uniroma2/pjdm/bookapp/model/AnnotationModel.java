package it.uniroma2.pjdm.bookapp.model;

import java.io.Serializable;

public class AnnotationModel implements Serializable {
    private final int annotationId;
    private final String frase;

    public AnnotationModel(int annotationId, String frase) {
        this.annotationId = annotationId;
        this.frase = frase;
    }

    public String getFrase() {
        return frase;
    }

    public int getAnnotationId() {
        return annotationId;
    }
}