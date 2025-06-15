import com.oocourse.exp.Order;

import java.util.ArrayList;

public class ProcessingQueue {
    private final ArrayList<Order> takeAwayOrders = new ArrayList<>();
    private final ArrayList<Order> eatInOrders = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Order order, String chef) {
        System.out.printf("scheduled-%d-to-%s\n", order.getId(), chef);
        if (order.getType().equals("Eat In")) {
            eatInOrders.add(order);
        } else {
            takeAwayOrders.add(order);
        }
        notifyAll();
    }

    public synchronized Order poll(String type) {
        if (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
        // TODO：按照优先级取出需要处理的订单，在这里输出 working
        if (!eatInOrders.isEmpty()) {
            System.out.println("working-" + eatInOrders.get(0).getId() + "-by-" + type);
            return eatInOrders.remove(0);
        }
        else if (!takeAwayOrders.isEmpty()) {
            System.out.println("working-" + takeAwayOrders.get(0).getId() + "-by-" + type);
            return takeAwayOrders.remove(0);
        }
        else {
            return null;
        }
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return takeAwayOrders.isEmpty() && eatInOrders.isEmpty();
    }
}
