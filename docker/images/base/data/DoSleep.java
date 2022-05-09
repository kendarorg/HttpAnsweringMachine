public class DoSleep {
    public static void main(String[] args) throws  InterruptedException  {
        Object obj = new Object();
        synchronized (obj) {
            obj.wait();
        }
    }
}