package io.agora.livedemo.ui.other.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.livedemo.common.SingleSourceLiveData;
import io.agora.livedemo.common.reponsitories.EmClientRepository;
import io.agora.livedemo.common.reponsitories.Resource;
import io.agora.livedemo.data.UserRepository;
import io.agora.livedemo.data.model.User;

public class LoginViewModel extends AndroidViewModel {
    private final EmClientRepository repository;
    private SingleSourceLiveData<Resource<Boolean>> loginObservable;
    private SingleSourceLiveData<Resource<EaseUser>> loginObservableUser;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new EmClientRepository();
        loginObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<Boolean>> getLoginObservable() {
        return loginObservable;
    }

    public LiveData<Resource<EaseUser>> getLoginObservableUser() {
        return loginObservableUser;
    }

    public void login() {
        User user = UserRepository.getInstance().getCurrentUser();
        if (null == user) {
            user = UserRepository.getInstance().getRandomUser();
        }
        if (null != user) {
            //loginObservable.setSource(repository.loginByAppServer(user.getId(), user.getNickName()));
            loginObservableUser.setSource(repository.loginToServer(user.getId(), user.getNickName()));
        }
    }
}
