package com.eduscope.eduscope.notes;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduscope.eduscope.R;

public class NoteViewHolder extends RecyclerView.ViewHolder {

    private TextView noteTitle, noteContent;
    LinearLayout noteLinearLayout;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        noteTitle = itemView.findViewById(R.id.note_title);
        noteContent = itemView.findViewById(R.id.note_content);
        noteLinearLayout = itemView.findViewById(R.id.note_linear_layout);
    }

    public TextView getNoteTitle() {
        return noteTitle;
    }

    public TextView getNoteContent() {
        return noteContent;
    }

    public LinearLayout getNoteLinearLayout() {
        return noteLinearLayout;
    }

    public void setNoteTitle(TextView noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setNoteContent(TextView noteContent) {
        this.noteContent = noteContent;
    }

    public void setNoteLinearLayout(LinearLayout noteLinearLayout) {
        this.noteLinearLayout = noteLinearLayout;
    }
}
