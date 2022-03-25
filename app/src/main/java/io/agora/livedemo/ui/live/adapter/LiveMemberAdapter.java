package io.agora.livedemo.ui.live.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.ui.base.EaseBaseRecyclerViewAdapter;
import io.hyphenate.easeui.widget.EaseImageView;

import androidx.annotation.NonNull;

public class LiveMemberAdapter extends EaseBaseRecyclerViewAdapter<String> {

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new LiveMemberViewHolder(LayoutInflater.from(mContext).inflate(R.layout.em_layout_live_member_item, parent, false));
    }

    private class LiveMemberViewHolder extends ViewHolder<String> {
        private EaseImageView imgAvatar;
        private TextView txtUsernick;
        private Button btnManager;

        public LiveMemberViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            imgAvatar = findViewById(R.id.img_avatar);
            txtUsernick = findViewById(R.id.txt_usernick);
            btnManager = findViewById(R.id.btn_manager);
        }

        @Override
        public void setData(String item, int position) {
            txtUsernick.setText(DemoHelper.getNickName(item));
            imgAvatar.setImageResource(DemoHelper.getAvatarResource(item, R.drawable.ease_default_avatar));
        }
    }
}
