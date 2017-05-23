/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.server;
 

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ClientData {

@SerializedName("username")
@Expose
private String username;
@SerializedName("password")
@Expose
private String password;

public String getUsername() {
return username;
}

public void setUsername(String username) {
this.username = username;
}

public String getPassword() {
return password;
}

public void setPassword(String password) {
this.password = password;
}

}
