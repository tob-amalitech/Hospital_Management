package com.hospital.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    // Standard local MongoDB port
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "hospital_logs";

    private static MongoClient mongoClient;

    static {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            System.out.println("Connected to MongoDB at " + CONNECTION_STRING);
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            // We don't throw exception here to allow app to run even if Mongo is down
            // (Hybrid safety)
            mongoClient = null;
        }
    }

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            return null;
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static boolean isConnected() {
        return mongoClient != null;
    }
}
