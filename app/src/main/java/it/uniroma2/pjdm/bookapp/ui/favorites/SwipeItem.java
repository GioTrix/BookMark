package it.uniroma2.pjdm.bookapp.ui.favorites;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import it.uniroma2.pjdm.bookapp.adapter.BookFavAdapter;

public class SwipeItem extends ItemTouchHelper.SimpleCallback {

    BookFavAdapter mItemAdapter;

    SwipeItem(BookFavAdapter adapter){
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mItemAdapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        Log.d("SwipeItem", "onSwiped: Position=" + position);

        mItemAdapter.removeItem(position);
        mItemAdapter.notifyItemRemoved(position);
    }

}
