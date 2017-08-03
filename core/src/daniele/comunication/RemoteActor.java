package daniele.comunication;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import daniele.utils.IntMsg;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class RemoteActor extends UntypedAbstractActor   {

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof IntMsg) {
            IntMsg m = (IntMsg) message;
            System.out.println("remote: " + m.getVal() + " from: " + sender());
            m.inc();
            sender().tell(new IntMsg(m.getVal()), getSelf());
        } else {
            System.out.println("error");
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("ip address: " + Inet4Address.getLocalHost().getHostAddress());
            //parte per ottenere dinamicamente l'ip
            Config customConf = ConfigFactory.parseString(
                    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                            "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                            ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                            ",\"netty\":{\"tcp\":{\"hostname\":\""+Inet4Address.getLocalHost().getHostAddress()+"\",\"port\":2727}}}}}"
            );
            ActorSystem system = ActorSystem.create("RemoteSystem", customConf);
            system.actorOf(Props.create(RemoteActor.class), "remote");
            System.out.println("remote is ready");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}


