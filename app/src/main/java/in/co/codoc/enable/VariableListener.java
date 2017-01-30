package in.co.codoc.enable;

/**
 * Created by ashik619 on 29-11-2016.
 */
public class VariableListener {
    int total;
    private ChangeListener listener;
    public ChangeListener getListener() {
        return listener;
    }
    public int getTotal(){
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
        if (listener != null) listener.onChange();
    }
    public void subtract(int total){
        this.total -= total;
        if (listener != null) listener.onChange();
    }
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }
    public interface ChangeListener {
        void onChange();
    }
}
