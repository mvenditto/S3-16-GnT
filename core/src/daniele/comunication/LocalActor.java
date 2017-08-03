package daniele.comunication;

import akka.actor.*;
import daniele.utils.IntMsg;

public class LocalActor extends UntypedAbstractActor {

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
}
