package com.example.convo_z.Model;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Users implements Serializable {

    String profilePic;
    String userName;
    String email;
    String password;
    String userId;
    String lastSeen;
    String phoneNumber;
    String bio;
    ArrayList<HashMap<String,Object>> status;
    ArrayList<String> muted; //status muted
    ArrayList<String> blocked;
    ArrayList<String> hidden; //status hidden

    public Users(String profilePic, String userName, String email, String password, String userId, String lastSeen, String phoneNumber, String bio,ArrayList<HashMap<String,Object>> status,ArrayList<String> muted,ArrayList<String> blocked,ArrayList<String> hidden) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.lastSeen = lastSeen;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
        this.status = status;
        this.muted = muted;
        this.blocked = blocked;
        this.hidden = hidden;
    }

    public Users(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public ArrayList<String> getMuted() {
        return muted;
    }

    public ArrayList<String> getBlocked() {
        return blocked;
    }

    public ArrayList<String> getHidden() {
        return hidden;
    }

    public void setHidden(ArrayList<String> hidden) {
        this.hidden = hidden;
    }

    public void setBlocked(ArrayList<String> blocked) {
        this.blocked = blocked;
    }

    public void setMuted(ArrayList<String> muted) {
        this.muted = muted;
    }

    public ArrayList<HashMap<String,Object>> getStatus() {
        return status;
    }

    public void setStatus(ArrayList<HashMap<String,Object>> status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Users()
    {}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
