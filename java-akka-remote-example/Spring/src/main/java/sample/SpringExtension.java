package sample;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;

public class SpringExtension extends AbstractExtensionId<SpringExt> {

    public static final SpringExtension SPRING_EXTENSION_PROVIDER = new SpringExtension();

    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    public SpringExtension lookup() {
        return SpringExtension.SPRING_EXTENSION_PROVIDER; // The public static final
    }
}