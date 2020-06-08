package ru.hse.cs.java2020.task03;

import java.util.Objects;

public class UserData {
    private String token;
    private String org;
    private String login;

    public UserData(String newToken, String newOrg, String newLogin) {
        this.token = newToken;
        this.org = newOrg;
        this.login = newLogin;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserData userInfo = (UserData) obj;
        return token.equals(userInfo.token) && org.equals(userInfo.org) && login.equals(userInfo.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, org, login);
    }

    public String getToken() {
        return token;
    }

    public String getOrg() {
        return org;
    }

    public String getLogin() {
        return login;
    }

}
