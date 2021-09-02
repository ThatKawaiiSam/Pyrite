# Pyrite
> Jedis Packet Library.

---

#### Setup Pyrite + Send Packet
```java
// Setup
Pyrite pyrite = new Pyrite(
    new PyriteCredentials("localhost", "", 6379)
);

// Register Container
pyrite.registerContainer(new ExamplePacketContainer());

// Send Packet
pyrite.sendPacket(new ExamplePacket(), "Channel");
```

#### Example Packet
```java
public class ExamplePacket extends Packet {

    private int field1 = 1;
    private String field2 = "Howdy there!";

}
```

#### Example Packet Container
```java
import io.github.thatkawaiisam.pyrite.packet.PacketContainer;
import io.github.thatkawaiisam.pyrite.packet.PacketListener;

public class ExamplePacketContainer implements PacketContainer {

    @PacketListener
    public void onTestPacket(ExamplePacket packet) {
        System.out.println("Packet Recieved!");
        System.out.println("====================");
        System.out.println("Field1: " + packet.getField1());
        System.out.println("Field2: " + packet.getField2());
    }

}
```

---

## Contributing
When contributing, please create a pull request with the branch named as follows ``<feature/fix>/<title>``.

To compile, run the maven command: ``mvn clean install``

To run unit tests, run the maven command ``mvn test``

---

## Contact

- Discord: ThatKawaiiSam#2882
- Telegram: ThatKawaiiSam