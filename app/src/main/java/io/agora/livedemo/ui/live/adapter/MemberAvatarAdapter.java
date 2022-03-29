package io.agora.livedemo.ui.live.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.data.TestAvatarRepository;

public class MemberAvatarAdapter extends EaseBaseRecyclerViewAdapter<String> {
    TestAvatarRepository avatarRepository;

    public MemberAvatarAdapter() {
        avatarRepository = new TestAvatarRepository();
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.avatar_list_item, parent, false);
        return new AvatarViewHolder(view);
    }

    private class AvatarViewHolder extends ViewHolder<String> {
        private EaseImageView avatar;
        private TextView tvGiftNum;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            avatar = findViewById(R.id.avatar);
            tvGiftNum = findViewById(R.id.tv_gift_num);
        }

        @Override
        public void setData(String item, int position) {
            int avatarResource = DemoHelper.getAvatarResource(item, R.drawable.ease_default_avatar);
            //暂时使用测试数据
            Glide.with(mContext)
                    .load(avatarResource)
                    .placeholder(R.drawable.ease_default_avatar)
                    .into(avatar);
        }
    }
}
