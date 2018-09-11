package nz.ac.auckland.concert.common;

public class Config {
    // Currently Set to 5 seconds
    // This should probably be increased in the future so less requests are made
    // It's value will depend on how often the db is updated
    public static int CACHE_EXPIRY_SECONDS = 5;
}
