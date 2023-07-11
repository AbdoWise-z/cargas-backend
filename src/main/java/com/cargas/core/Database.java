package com.cargas.core;

public interface Database {

    void init();

    String login(String username , String password);

    int REGISTER_SUCCESS = 0;
    int REGISTER_INVALID_USER = 1;
    int REGISTER_TAKEN_USER = 3;

    int REGISTER_INVALID_PASSWORD = 2;
    int REGISTER_INVALID_DATA = 4;

    int register(String username , String password , String name , String phone_number , String address);

}
