package it.uniroma2.pjdm.bookapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.model.BookCatalogoModel;

public class BookCatalogoAdapter extends RecyclerView.Adapter < BookCatalogoAdapter.BookCatalogoViewHolder > {
    private final Context context;
    private final ArrayList < BookCatalogoModel > catalogo;
    private OnFavButtonClickListener favButtonClickListener;
    private OnBookClickListener bookClickListener;

    public BookCatalogoAdapter(Context context, ArrayList < BookCatalogoModel > catalogo) {
        this.context = context;
        this.catalogo = catalogo;
    }

    //Metodo chiamato quando viene creato un nuovo ViewHolder
    @NonNull
    @Override
    public BookCatalogoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalogo_item, parent, false);
        return new BookCatalogoViewHolder(view);
    }

    //Restituisce il numero totale di elementi nella lista
    @Override
    public int getItemCount() {
        return catalogo.size();
    }

    //Metodo chiamato per mostrare i dati nella posizione specificata
    @Override
    public void onBindViewHolder(@NonNull BookCatalogoViewHolder holder, int position) {
        final BookCatalogoModel currentBook = catalogo.get(position);

        // Imposta i dati nella vista
        holder.title.setText(currentBook.getTitle());
        holder.author.setText(currentBook.getAuthor());
        holder.genere.setText(currentBook.getGenere());

        //Carica l'immagine utilizzando la libreria Glide
        Glide.with(context)
                .load(currentBook.getUrlImage())
                .encodeQuality(70)
                .error(R.drawable.not_found_icon)
                .into(holder.urlImage);

        //Imposta l'aspetto del pulsante in base allo stato dei preferiti
        if (currentBook.isInFav()) {
            holder.btFav.setBackgroundResource(R.drawable.baseline_favorite_red_24);
        } else {
            holder.btFav.setBackgroundResource(R.drawable.baseline_favorite_border_24);
        }

        //Aggiunge un listener al pulsante dei preferiti
        holder.btFav.setOnClickListener(view -> {
            currentBook.setInFav(!currentBook.isInFav()); //Inverte lo stato dei preferiti

            //Richiama il metodo onFavButtonClick nel listener
            if (favButtonClickListener != null) {
                favButtonClickListener.onFavButtonClick(currentBook.getIdBook(), currentBook.isInFav());
            }

            //Aggiorna l'aspetto del pulsante
            holder.btFav.setBackgroundResource(currentBook.isInFav() ? R.drawable.baseline_favorite_red_24 : R.drawable.baseline_favorite_border_24);
        });

        //Aggiunge un listener all'intera vista dell'elemento
        holder.itemView.setOnClickListener(view -> {
            //Richiama il metodo onBookClick nel listener
            if (bookClickListener != null) {
                bookClickListener.onBookClick(currentBook.getTitle(), currentBook.getAuthor(), currentBook.getUrlImage(), currentBook.getIdBook());
            }
        });
    }

    //Metodo per aggiungere un elemento alla lista e notificare l'Adapter del cambiamento
    @SuppressLint("NotifyDataSetChanged")
    public void add(BookCatalogoModel bookCatalogoModel) {
        this.catalogo.add(bookCatalogoModel);
        notifyDataSetChanged();
    }

    //ViewHolder per gli elementi della lista
    public static class BookCatalogoViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView author;
        private final TextView genere;
        private final Button btFav;
        private final ImageView urlImage;

        public BookCatalogoViewHolder(@NonNull View itemView) {
            super(itemView);
            //Inizializza le view all'interno dell'elemento della lista
            title = itemView.findViewById(R.id.tv_item_titolo);
            author = itemView.findViewById(R.id.tv_item_autore);
            genere = itemView.findViewById(R.id.tv_c_item_genere);
            urlImage = itemView.findViewById(R.id.iv_url_fav);
            btFav = itemView.findViewById(R.id.bt_fav);
        }
    }

    //Interfaccia per la gestione degli eventi di click sui pulsanti preferiti
    public interface OnFavButtonClickListener {
        void onFavButtonClick(int bookId, boolean isInFav);
    }

    //Metodo per impostare il listener per i click sui pulsanti dei preferiti
    public void setFavButtonClickListener(OnFavButtonClickListener listener) {
        this.favButtonClickListener = listener;
    }

    //Interfaccia per la gestione degli eventi di click sugli elementi della lista
    public interface OnBookClickListener {
        void onBookClick(String titolo, String autore, String url, int bookId);
    }

    // Metodo per impostare il listener per i click sugli elementi della lista
    public void setOnBookClickListener(OnBookClickListener listener) {
        this.bookClickListener = listener;
    }
}