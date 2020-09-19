package io.github.thatkawaiisam.jedishelper;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {

    /**
     * Execute Command.
     *
     * @param redis instance.
     */
    T execute(Jedis redis);

}

