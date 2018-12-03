package io.github.thatkawaiisam.jedis.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class JedisCredentials {

    private final String address, password;
    private final int port;

    public boolean isAuth() {
        return password != null && !password.isEmpty();
    }

}
