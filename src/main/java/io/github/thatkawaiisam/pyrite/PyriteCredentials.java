package io.github.thatkawaiisam.pyrite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class PyriteCredentials {

    private final String address, password;
    private final int port;

    /**
     * Check credential authentication.
     *
     * @return whether current credentials have authentication.
     */
    public boolean isAuth() {
        return password != null && !password.isEmpty();
    }

}
