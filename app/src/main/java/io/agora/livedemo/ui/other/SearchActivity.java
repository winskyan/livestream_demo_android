package io.agora.livedemo.ui.other;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.livedemo.R;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.ui.base.BaseActivity;
import io.agora.livedemo.ui.live.adapter.LiveListAdapter;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.edit_text)
    EditText editText;
    @BindView(R.id.recView)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.btn_cancel)
    TextView cancelView;
    List<LiveRoom> searchedList;

    LiveListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        searchedList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!TextUtils.isEmpty(v.getText())) {
                        searchLiveRoom(v.getText().toString());
                    } else {
                        Toast.makeText(SearchActivity.this, "请输入房间号", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void searchLiveRoom(final String searchText) {
//        executeTask(new ThreadPoolManager.Task<LiveRoom>() {
//            @Override public LiveRoom onRequest() throws HyphenateException {
//                return LiveManager.getInstance().getLiveRoomDetails(searchText);
//            }
//
//            @Override public void onSuccess(LiveRoom liveRoom) {
//                emptyView.setVisibility(View.INVISIBLE);
//                searchedList.clear();
//                searchedList.add(liveRoom);
//
//                if(adapter == null) {
//                    adapter = new LiveListAdapter();
//                    recyclerView.setAdapter(adapter);
//                }
//
//                adapter.setData(searchedList);
//            }
//
//            @Override public void onError(HyphenateException exception) {
//                emptyView.setVisibility(View.VISIBLE);
//            }
//        });

    }

}
