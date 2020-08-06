package ru.data;

import org.springframework.stereotype.Component;

@Component
public class UserData {
    private String login;

    private String password;

    public UserData(String login, String password){
        this.login = login;
        this.password = password;
    }

    public UserData(){}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
