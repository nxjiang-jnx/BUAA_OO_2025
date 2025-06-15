import com.oocourse.exp.Order;
import com.oocourse.exp.OrderInput;

public class InputThread extends Thread {
    private final OrderQueue orderQueue;

    public InputThread(OrderQueue orderQueue) {
        this.orderQueue = orderQueue;
    }

    @Override
    public void run() {
        OrderInput orderInput = new OrderInput(System.in);
        while (true) {
            Order order = orderInput.nextOrder();
            // TODO: 将foo替换为合适的内容
            if (order == null) {
                orderQueue.setEnd();
                System.out.println("InputThread ends");
                break;
            }
            orderQueue.offer(order);
        }
    }
}
