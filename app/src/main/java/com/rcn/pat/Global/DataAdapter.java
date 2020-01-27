package com.rcn.pat.Global;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rcn.pat.R;
import com.rcn.pat.ViewModels.PausaReasons;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.CuasePauseViewHolder>  {
    private ArrayList<PausaReasons> mDataset;
    RecyclerViewItemClickListener recyclerViewItemClickListener;

    public DataAdapter(ArrayList<PausaReasons> myDataset, RecyclerViewItemClickListener listener) {
        mDataset = myDataset;
        this.recyclerViewItemClickListener = listener;
    }

    @NonNull
    @Override
    public CuasePauseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pause_cause_item, parent, false);

        CuasePauseViewHolder vh = new CuasePauseViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(@NonNull CuasePauseViewHolder cuasePauseViewHolder, int i) {
        cuasePauseViewHolder.mTextView.setText(mDataset.get(i).getNombre());
    }

    @Override
    public int getItemCount() {

        return mDataset.size();
    }



    public  class CuasePauseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextView;

        public CuasePauseViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.textView);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recyclerViewItemClickListener.clickOnItem(mDataset.get(this.getAdapterPosition()));

        }
    }

    public interface RecyclerViewItemClickListener {
        void clickOnItem(PausaReasons data);
    }
}
