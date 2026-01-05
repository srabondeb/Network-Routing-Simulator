
import java.io.Serializable;

public class LSAWrapper implements Serializable {

    LSA LSAmessage;
    int imidiateSender;
    public  LSAWrapper(LSA message, int imidiateSender) {
        this.LSAmessage=message;
        this.imidiateSender=imidiateSender;
    }

    public int getImidiateSender() {
        return imidiateSender;
    }

    public LSA getLSAmessage() {
        return LSAmessage;
    }

    public void setImidiateSender(int sender) {
        this.imidiateSender=sender;
    }

}

