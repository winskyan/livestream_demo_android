package io.agora.livedemo.data.model;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String nickName;
    private int avatarResource;
    private int avatarResourceIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAvatarResource() {
        return avatarResource;
    }

    public void setAvatarResource(int avatarResource) {
        this.avatarResource = avatarResource;
    }

    public int getAvatarResourceIndex() {
        return avatarResourceIndex;
    }

    public void setAvatarResourceIndex(int avatarResourceIndex) {
        this.avatarResourceIndex = avatarResourceIndex;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", nickName='" + nickName + '\'' +
                ", avatarResource=" + avatarResource +
                ", avatarResourceIndex=" + avatarResourceIndex +
                '}';
    }
}
