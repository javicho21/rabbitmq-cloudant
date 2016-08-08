package com.github.javicho21.cloudant;

import com.github.javicho21.rabbitmq.Payload;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.common.collect.ImmutableMap;

/**
 * Publishes data to Cloudant.
 * 
 * @author aaronzhang
 */
public class Cloudant implements Observer {
    
    private final String account;
    private final String username;
    private final String password;
    private final CloudantClient client;
    
    private Cloudant(String account, String username, String password) {
        this.account = account;
        this.username = username;
        this.password = password;
        client = ClientBuilder
            .account(account)
            .username(username)
            .password(password)
            .build();
    }
    
    public static class Builder {
        
        private String account;
        private String username;
        private String password;
        
        public Builder setAccount(String account) {
            this.account = account;
            return this;
        }
        
        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }
        
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }
        
        public Cloudant build() {
            return new Cloudant(account, username, password);
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof Payload)) {
            throw new IllegalArgumentException(
                "Cloudant must be updated with payload");
        }
        Payload payload = (Payload) arg;
        Database db = client.database(payload.getMetric().toLowerCase(), true);
        Map<String, Object> document = ImmutableMap.<String, Object>builder()
            .putAll(payload.getFields())
            .put("timestamp", TimeUnit.NANOSECONDS.convert(
                payload.getTimestampValue(), payload.getTimestampUnit()))
            .putAll(payload.getTags())
            .build();
        db.save(document);
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}