package io.github.thatkawaiisam.jedis.helper;

import redis.clients.jedis.Jedis;

public interface IRedisCommand<T> {

    /**
     *
     * @param redis
     * @return
     */
    T execute(Jedis redis);

}

