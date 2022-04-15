package io.agora.livedemo.ui.live.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.Map;

import io.agora.chat.UserInfo;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.livedemo.R;

public class MemberAvatarAdapter extends EaseBaseRecyclerViewAdapter<String> {

    private static Map<String, UserInfo> stringUserInfoMap;

    public MemberAvatarAdapter() {
    }

    public void setStringUserInfoMap(Map<String, UserInfo> stringUserInfoMap) {
        MemberAvatarAdapter.stringUserInfoMap = stringUserInfoMap;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.avatar_list_item, parent, false);
        return new AvatarViewHolder(view);
    }

    private class AvatarViewHolder extends ViewHolder<String> {
        private EaseImageView avatar;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            avatar = findViewById(R.id.avatar);
        }

        @Override
        public void setData(String item, int position) {
            try {
                Glide.with(mContext).load(stringUserInfoMap.get(item).getAvatarUrl()).placeholder(R.drawable.avatar_default).error(R.drawable.avatar_default).into(avatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
