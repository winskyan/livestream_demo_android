package io.agora.livedemo.ui.live.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import io.agora.livedemo.common.reponsitories.EmClientRepository;
import io.agora.livedemo.common.reponsitories.Resource;

public class LiveMemberListViewModel extends AndroidViewModel {
    private EmClientRepository repository;
    private MediatorLiveData<Resource<List<String>>> membersObservable;

    public LiveMemberListViewModel(@NonNull Application application) {
        super(application);
        repository = new EmClientRepository();
        membersObservable = new MediatorLiveData<>();
    }

    public MediatorLiveData<Resource<List<String>>> getMembersObservable() {
        return membersObservable;
    }

    public void getMembers(String chatRoomId) {
        membersObservable.addSource(repository.getOnlyMembers(chatRoomId), response -> membersObservable.postValue(response));
    }
}
