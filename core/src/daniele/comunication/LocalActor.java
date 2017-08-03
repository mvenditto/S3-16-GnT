package daniele.comunication;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import daniele.utils.IntMsg;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class LocalActor extends UntypedAbstractActor {

    @Override
    public void preStart() {
        //nel caso sulla rete non ci sia ip statico usare il commento che ha il localhost
        ActorSelection remoteActor = getContext().actorSelection("akka.tcp://RemoteSystem@192.168.1.12:2727/user/remote");
        System.out.println("That 's remote: " + remoteActor);
        remoteActor.tell(new IntMsg(0), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof IntMsg) {
            IntMsg m = (IntMsg) message;
            System.out.println("local: " + m.getVal() + " from: " + sender());
            m.inc();
            sender().tell(new IntMsg(m.getVal()), getSelf());
        } else {
            System.out.println("error");
        }
    }

    public static void main(String[] args) {
        try {
            //parte per ottenere dinamicamente l'ip
            Config customConf = ConfigFactory.parseString(
                    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                            "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                            ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                            ",\"netty\":{\"tcp\":{\"hostname\":\""+Inet4Address.getLocalHost().getHostAddress()+"\",\"port\":5050}}}}}"
            );
            ActorSystem system = ActorSystem.create("LocalActor", customConf);
            system.actorOf(Props.create(LocalActor.class), "local");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
