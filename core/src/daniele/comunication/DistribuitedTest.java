package daniele.comunication;

import daniele.utils.IntMsg;
import org.junit.Test;

public class DistribuitedTest {

    @Test
    public void testComunication() {
        SystemCreator.createSystem("RemoteSystem");
        SystemCreator.getSystem("RemoteSystem").createActor("Remote");
        SystemCreator.createSystem("LocalSystem");
        SystemCreator.getSystem("LocalSystem").createActor("Local");
        SystemCreator.getSystem("LocalSystem").getActor("Local").sendData("Remote", new IntMsg(0));
        assertEquals(SystemCreator.getSystem("RemoteSystem").getActor("Remote").getData(), new Msg(1));
    }
}
