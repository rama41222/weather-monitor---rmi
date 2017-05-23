/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.server;

import java.io.FileReader;
import java.util.Scanner;

/**
 *
 * @author Rama41222
 */
public class Authentication {
    private static Authentication auth;
    private Authentication(){}
    public static Authentication getInstance(){
        return auth;
    }
    
    String validateUser(String username, String password){
        try{
        Scanner input = new Scanner(new FileReader("/Users/Rama41222/NetBeansProjects/Storm/src/com/rama41222/storm/server/sensors.txt"));
        
        while(input.hasNext()){
            String line = input.nextLine();
            String authUser[] = line.split(":");
            System.out.println(authUser);
            
            if(username.equals(authUser[0])){
                
                if(password.equals(authUser[1])){
                    return "authenticated";
                }else{
                    return "user_found_password_mismatch";
                }
            }
            
        }
            return "user_not_found";
        }catch(Exception e){
            return "user_not_found";
        }
    
    }
    
    
}
