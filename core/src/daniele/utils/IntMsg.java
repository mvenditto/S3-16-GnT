package daniele.utils;

import java.io.Serializable;

public class IntMsg implements Serializable {

    private int val = 0;

    public IntMsg(int val) {
        this.val = val;
    }

    public void inc() {
        this.val++;
    }

    public int getVal() {
        return this.val;
    }
}
