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
}


