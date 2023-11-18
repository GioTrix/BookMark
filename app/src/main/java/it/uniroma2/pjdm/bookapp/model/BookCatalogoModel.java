package it.uniroma2.pjdm.bookapp.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class BookCatalogoModel implements Serializable {
    private final int idBook;
    private final String title;
    private final String author;
    private final String genere;
    private final String urlImage;
    private Boolean isInFav;

    public BookCatalogoModel(int idBook, String title, String author, String genere, String urlImage) {
        this.idBook = idBook;
        this.title = title;
        this.author = author;
        this.genere = genere;
        this.urlImage = urlImage;
    }

    public int getIdBook() {
        return idBook;
    }

    public void setInFav(Boolean inFav) {
        isInFav = inFav;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenere() {
        return genere;
    }

    public String getUrlImage() {
        return urlImage;
    }

    @NonNull
    @Override
    public String toString() {
        return "BookCatalogoModel{" +
                "idUser='" + idBook + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genere='" + genere + '\'' +
                ", urlImage='" + urlImage + '\'' +
                '}';
    }

    public boolean isInFav() {
        return isInFav != null && isInFav;
    }
}