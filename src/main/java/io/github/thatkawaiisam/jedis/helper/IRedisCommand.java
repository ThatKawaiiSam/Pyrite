package io.github.thatkawaiisam.jedis.helper;

import redis.clients.jedis.Jedis;

public interface IRedisCommand<T> {

    T execute(Jedis redis);

}

