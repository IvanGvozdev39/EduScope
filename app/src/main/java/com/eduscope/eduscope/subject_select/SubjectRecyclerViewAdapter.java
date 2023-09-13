package com.eduscope.eduscope.subject_select;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduscope.eduscope.R;

import java.util.List;

public class SubjectRecyclerViewAdapter extends RecyclerView.Adapter<SubjectRecyclerViewAdapter.SubjectViewHolder> {

    private static Context mContext;
    //private List<Integer> image;
    //private List<String> text;
    private boolean darktheme;
    private List<Subject> subjectList;
    private SubjectViewHolder.recyclerViewClickListener listener;


    public SubjectRecyclerViewAdapter(Context mContext, List<Subject> subjectList, boolean darktheme, SubjectViewHolder.recyclerViewClickListener listener) {
        this.mContext = mContext;
        this.subjectList = subjectList;
        this.darktheme = darktheme;
        this.listener = listener;
    }

    public static Context getmContext() {
        return mContext;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        if (darktheme)
            view = mInflater.inflate(R.layout.cardview_item_subject_darktheme, parent, false);
        else
            view = mInflater.inflate(R.layout.cardview_item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        holder.bind(subjectList.get(position), listener);
    }



    @Override
    public int getItemCount() {
        return subjectList.size();
    }


    public static class SubjectViewHolder extends RecyclerView.ViewHolder {

        TextView subject_tv;
        ImageView subject_thumbnail;


        public interface recyclerViewClickListener {
            public void onClick(Subject subject);
        }


        public void bind(Subject subject, recyclerViewClickListener listener) {
            subject_tv.setText(subject.text);
            subject_thumbnail.setImageResource(subject.image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(subject);
                }
            });
        }

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            subject_tv = (TextView) itemView.findViewById(R.id.subject_text);
            subject_thumbnail = (ImageView) itemView.findViewById(R.id.subject_image);
        }
    }
}
