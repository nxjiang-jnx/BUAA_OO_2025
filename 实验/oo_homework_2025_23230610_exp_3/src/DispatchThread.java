import com.oocourse.exp.Order;

import java.util.HashMap;

public class DispatchThread extends Thread {
    private final OrderQueue orderQueue;
    private final HashMap<String, ProcessingQueue> queueMap;

    public DispatchThread(OrderQueue orderQueue,
                          HashMap<String, ProcessingQueue> queueMap) {
        this.orderQueue = orderQueue;
        this.queueMap = queueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (orderQueue.isEmpty() && orderQueue.isEnd()) {
                for (ProcessingQueue queue : queueMap.values()) {
                    // TODO: 向厨师线程发出结束信号
                    queue.setEnd();
                }
                System.out.println("DispatchThread ends");
                break;
            }
            // TODO: 从orderQueue中取出订单
            Order order = orderQueue.poll();
            if (order == null) {
                continue;
            }
            dispatch(order);
        }
    }

    private void dispatch(Order order) {
        String chef;
        switch (order.getDish()) {
            // TODO: 为每一个订单分配对应的厨师
            case "Appetizer":
                chef = "A";
                break;
            case "Main Course":
                chef = "B";
                break;
            case "Dessert":
                chef = "C";
                break;
            default:
                throw new IllegalArgumentException("Invalid order type");
        }
        // TODO: 将订单加入到对应厨师的处理队列中
        queueMap.get(chef).offer(order, chef);
    }
}
