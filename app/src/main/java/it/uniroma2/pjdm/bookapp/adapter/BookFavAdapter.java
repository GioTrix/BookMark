package it.uniroma2.pjdm.bookapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.model.BookFavModel;

public class BookFavAdapter extends RecyclerView.Adapter < BookFavAdapter.BookFavViewHolder > {

    private final Context context;
    private final ArrayList < BookFavModel > bookList;
    private final OnItemClickListener clickListener;

    public BookFavAdapter(Context context, ArrayList < BookFavModel > bookList, OnItemClickListener clickListener) {
        this.context = context;
        this.bookList = bookList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BookFavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_item, parent, false);
        return new BookFavViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BookFavViewHolder holder, int position) {
        final BookFavModel currentBook = bookList.get(position);

        //Imposta i dati nella vista
        holder.title.setText(currentBook.getTitle());
        holder.author.setText(currentBook.getAuthor());

        //Carica l'immagine utilizzando la libreria Glide
        Glide.with(context).load(currentBook.getImageUrl()).into(holder.urlImage);

        //Aggiunge un listener all'intera vista dell'elemento
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(currentBook, holder.getBindingAdapterPosition());
            }
        });
    }

    //Metodo per rimuovere un elemento dalla lista e notificare l'Adapter del cambiamento
    public void removeItem(int position) {
        if (isValidPosition(position)) {
            BookFavModel book = bookList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,getItemCount());

            if (this.clickListener != null) {
                this.clickListener.onItemSwipe(book, position);
            }
        }
    }

    //Metodo per aggiungere un elemento alla lista e notificare l'Adapter del cambiamento
    @SuppressLint("NotifyDataSetChanged")
    public void add(BookFavModel bookFavModel) {
        new Handler(Looper.getMainLooper()).post(() -> {
            bookList.add(new BookFavModel(bookFavModel.getTitle(), bookFavModel.getAuthor(), bookFavModel.getImageUrl(), bookFavModel.getBookId()));
            notifyDataSetChanged();
        });
    }

    //ViewHolder per gli elementi della lista
    public static class BookFavViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView author;
        private final ImageView urlImage;

        public BookFavViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inizializza le view all'interno dell'elemento della lista
            title = itemView.findViewById(R.id.tv_item_titolo);
            author = itemView.findViewById(R.id.tv_item_autore);
            urlImage = itemView.findViewById(R.id.iv_url_fav);
        }
    }

    //Interfaccia per la gestione degli eventi di click sugli elementi della lista
    public interface OnItemClickListener {
        void onItemClick(BookFavModel model, int position);

        void onItemSwipe(BookFavModel book, int position);
    }

    //Metodo per verificare se la posizione è valida nella lista
    private boolean isValidPosition(int position) {
        return position >= 0 && position < bookList.size();
    }
}