package it.uniroma2.pjdm.bookapp.model;

import java.io.Serializable;

public class BookFavModel implements Serializable {
    private final String title;
    private final String author;
    private final String imageUrl;
    private final int bookId;

    public BookFavModel(String title, String author, String imageUrl, int bookId) {
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getBookId() {
        return bookId;
    }
}