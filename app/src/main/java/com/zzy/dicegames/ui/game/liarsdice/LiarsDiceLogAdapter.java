package com.zzy.dicegames.ui.game.liarsdice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;

import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 大话骰游戏日志适配器<br>
 * 部分日志的第一个参数是玩家编号，在显示时需要格式化为玩家名称
 *
 * @author 赵正阳
 */
class LiarsDiceLogAdapter extends RecyclerView.Adapter<LiarsDiceLogAdapter.LogViewHolder> {
    /** 第一个参数是玩家编号的日志资源id集合 */
    private static final Set<Integer> PLAYER_LOG_RES_IDS = Set.of(
            R.string.logPlayerTurn, R.string.logBidFei, R.string.logBidZhai,
            R.string.logBidTrue, R.string.logBidFalse, R.string.logLoseRound,
            R.string.logPlayerNWins, R.string.logOpen);

    private final Context mContext;

    /** 每条日志由字符串资源id和格式化参数组成 */
    private List<Pair<Integer, Object[]>> mLog;

    public LiarsDiceLogAdapter(Context context) {
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
                .inflate(R.layout.liars_dice_log_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        var log = mLog.get(position);
        Object[] args = log.second == null ? new Object[0] : log.second.clone();
        if (PLAYER_LOG_RES_IDS.contains(log.first) && args.length > 0 && args[0] instanceof Integer) {
            int p = (Integer) args[0];
            args[0] = mContext.getString(LiarsDiceGameViewModel.playerNameResId(p));
            // “X开了Y”这类日志的第二个参数也是玩家编号
            if (log.first == R.string.logOpen && args.length > 1 && args[1] instanceof Integer)
                args[1] = mContext.getString(
                        LiarsDiceGameViewModel.playerNameResId((Integer) args[1]));
        }
        holder.mTextView.setText(mContext.getString(log.first, args));
    }

    @Override
    public int getItemCount() {
        return mLog == null ? 0 : mLog.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        LogViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tvLog);
        }
    }
}
