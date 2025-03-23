package com.example.verticalvideoplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.verticalvideoplayer.R;
import com.example.verticalvideoplayer.models.FileItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileItem> fileItems;
    private final OnFileItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnFileItemClickListener {
        void onFileItemClick(FileItem fileItem);
    }

    public FileAdapter(List<FileItem> fileItems, OnFileItemClickListener listener) {
        this.fileItems = fileItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileItems.get(position);
        holder.bind(fileItem, listener);
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    public void updateData(List<FileItem> newFileItems) {
        this.fileItems = newFileItems;
        notifyDataSetChanged();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final TextView fileName;
        private final TextView fileInfo;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileInfo = itemView.findViewById(R.id.fileInfo);
        }

        public void bind(final FileItem fileItem, final OnFileItemClickListener listener) {
            fileName.setText(fileItem.getName());
            
            String infoText;
            if (fileItem.isDirectory()) {
                infoText = "文件夹 | " + dateFormat.format(fileItem.getModifiedDate());
            } else {
                infoText = fileItem.getFormattedSize() + " | " + dateFormat.format(fileItem.getModifiedDate());
            }
            fileInfo.setText(infoText);
            
            // 设置图标或颜色标识不同类型的文件
            if (fileItem.isDirectory()) {
                fileName.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_more, 0, 0, 0);
            } else if (fileItem.isVideoFile()) {
                fileName.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
            } else {
                fileName.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_agenda, 0, 0, 0);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFileItemClick(fileItem);
                }
            });
        }
    }
}
