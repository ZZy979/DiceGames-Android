package com.zzy.dicegames.ui.game.farkle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private Context mContext;

    /** 每条日志由字符串资源id和格式化参数组成 */
    private List<Pair<Integer, Object[]>> mLog;

    public LogAdapter(Context context) {
        mContext = context;
    }

    public void setLog(List<Pair<Integer, Object[]>> log) {
        mLog = log;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.farkle_log_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        var log = mLog.get(position);
        holder.mTextView.setText(mContext.getString(log.first, log.second));
    }

    @Override
    public int getItemCount() {
        return mLog == null ? 0 : mLog.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public LogViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tvLog);
        }
    }
}
